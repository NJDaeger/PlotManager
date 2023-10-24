package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.Util;
import com.njdaeger.plotmanager.dataaccess.repositories.IPlotRepository;
import com.njdaeger.plotmanager.servicelibrary.PlotBuilder;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotAttribute;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.models.World;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IPlotService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.services.IWorldService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class PlotService implements IPlotService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final IWorldService worldService;
    private final IConfigService configService;
    private final ICacheService cacheService;
    private final IAttributeService attributeService;
    private final IPluginLogger logger;

    public PlotService(IServiceTransaction transaction, IUserService userService, IAttributeService attributeService, IWorldService worldService, IConfigService configService, ICacheService cacheService, IPluginLogger logger) {
        this.transaction = transaction;
        this.userService = userService;
        this.worldService = worldService;
        this.configService = configService;
        this.cacheService = cacheService;
        this.attributeService = attributeService;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<Result<PlotBuilder>> createPlotBuilder(UUID creator, Location location) {
        transaction.use();

        return Util.<Result<PlotBuilder>>async(() -> {
            if (creator == null) return Result.bad("Creator cannot be null.");
            if (location == null) return Result.bad("Location cannot be null.");
            if (cacheService.getPlotBuilderCache().get(creator) != null) return Result.bad("Plot builder already exists for this creator.");

            var userId = await(userService.getUserByUuid(creator)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + creator + ".");

            var worldId = await(worldService.getWorldByUuid(location.getWorld().getUID())).getOr(World::getId, -1);
            if (worldId == -1) return Result.bad("Failed to find world with uuid " + location.getWorld().getUID() + ".");

            var plotBuilder = new PlotBuilder(location);
            cacheService.getPlotBuilderCache().put(creator, plotBuilder);
            return Result.good(plotBuilder);
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<PlotBuilder>> getPlotBuilder(UUID creator) {
        transaction.use();
        return Util.<Result<PlotBuilder>>async(() -> {
            if (creator == null) return Result.bad("Creator cannot be null.");
            var plotBuilder = cacheService.getPlotBuilderCache().get(creator);
            if (plotBuilder == null) return Result.bad("Failed to find plot builder for creator " + creator + ".");
            return Result.good(plotBuilder);
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<PlotBuilder>> cancelPlotBuilder(UUID creator) {
        transaction.use();
        return Util.<Result<PlotBuilder>>async(() -> {
            if (creator == null) return Result.bad("Creator cannot be null.");
            var plotBuilder = cacheService.getPlotBuilderCache().remove(creator);
            if (plotBuilder == null) return Result.bad("Failed to find plot builder for creator " + creator + ".");
            return Result.good(plotBuilder);
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> finishPlotBuilder(UUID creator) {
        transaction.use();

        return Util.<Result<Plot>>async(() -> {
            if (creator == null) return Result.bad("Creator cannot be null.");

            var user = await(userService.getUserByUuid(creator)).getOr(null);
            if (user == null) return Result.bad("Failed to find user with uuid " + creator + ".");
            var userId = user.getId();

            var plotBuilder = cacheService.getPlotBuilderCache().get(creator);
            if (plotBuilder == null) return Result.bad("Failed to find plot builder for creator " + user.getLastKnownName() + ".");

            var required = new ArrayList<>(configService.getRequiredPlotAttributes());

            if (!required.stream().allMatch(plotBuilder::hasAttribute)) {
                var missing = String.join(", ", configService.getRequiredPlotAttributes());
                return Result.bad("Missing required attributes: " + missing);
            }

            var location = plotBuilder.getLocation();
            var worldId = await(worldService.getWorldByUuid(location.getWorld().getUID())).getOr(World::getId, -1);
            if (worldId == -1) return Result.bad("Failed to find world with uuid " + plotBuilder.getLocation().getWorld().getUID() + ".");

            var plotRepo = transaction.getUnitOfWork().repo(IPlotRepository.class);
            var plotEntity = await(plotRepo.insertPlot(userId, worldId, location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            if (plotEntity == null) return Result.bad("Failed to create plot.");

            for (var builderPlotAttribute : plotBuilder.getAttributes()) {
                var attribute = await(attributeService.getAttribute(builderPlotAttribute.getAttribute())).getOr(null);
                if (attribute == null) return Result.bad("Failed to find attribute with name " + builderPlotAttribute.getAttribute() + ".");

                var type = configService.getAttributeType(attribute.getType());
                if (type == null) return Result.bad("Failed to find attribute type with name " + attribute.getType() + ".");

                var value = builderPlotAttribute.getValue();
                if (!type.isValidValue(value)) return Result.bad("Value " + value + " is not valid for attribute type " + type.getName() + ".");

                if (required.contains(attribute.getName()) && value.isBlank()) return Result.bad("Attribute " + attribute.getName() + " is required.");
                await(plotRepo.insertPlotAttribute(userId, plotEntity.getId(), attribute.getId(), value));
            }

            if (plotBuilder.getParent() != null) {
                await(plotRepo.updatePlotParent(userId, plotEntity.getId(), plotBuilder.getParent().getId()));
            }

            var createdPlot = new Plot(plotEntity.getId(), location, plotBuilder.getAttributes(), List.of(), plotBuilder.getParent(), null);
            cacheService.getPlotBuilderCache().remove(creator);
            cacheService.getPlotCache().put(plotEntity.getId(), createdPlot);

            return Result.good(createdPlot);

        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<List<Plot>>> getPlots() {
        transaction.use();

        return Util.<Result<List<Plot>>>async(() -> {
            if (!cacheService.getPlotCache().isEmpty()) return Result.good(List.copyOf(cacheService.getPlotCache().values()));

            var plotRepo = transaction.getUnitOfWork().repo(IPlotRepository.class);

            var plotEntities = await(plotRepo.getPlots());
            if (plotEntities == null) return Result.bad("Failed to get plots.");

            var plots = new ArrayList<Plot>();
            for (var plotEntity : plotEntities) {
                var worldId = await(worldService.getWorldById(plotEntity.getWorld())).getOr(World::getWorldUuid, null);
                if (worldId == null) continue;
                var world = Bukkit.getWorld(worldId);

                var attributes = await(plotRepo.getPlotAttributesForPlot(plotEntity.getId()).thenApply(attrEnts -> {
                    if (attrEnts == null || attrEnts.isEmpty()) return List.<PlotAttribute>of();

                    return attrEnts.stream().map(attrEnt -> {
                        var attr = await(attributeService.getAttributeById(attrEnt.getAttribute())).getOr(Attribute::getName, null);
                        if (attr == null) return null;
                        return new PlotAttribute(attr, attrEnt.getValue());
                    }).filter(Objects::nonNull).toList();
                }));

                Plot parent = null;
                if (plotEntity.getParent() != null) parent = await(getPlot(plotEntity.getParent())).getOr(null);

                //todo: get plot group and users
                var plot = new Plot(plotEntity.getId(), new Location(world, plotEntity.getX(), plotEntity.getY(), plotEntity.getZ()), attributes, List.of(), parent, null);
                cacheService.getPlotCache().put(plotEntity.getId(), plot);
            }
            return Result.good(plots);
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> getPlot(int plotId) {
        transaction.use();

        return Util.<Result<Plot>>async(() -> {
            if (cacheService.getPlotCache().containsKey(plotId)) return Result.good(cacheService.getPlotCache().get(plotId));

            var plotRepo = transaction.getUnitOfWork().repo(IPlotRepository.class);

            var plotEntity = await(plotRepo.getPlotById(plotId));
            if (plotEntity == null) return Result.bad("Failed to get plot.");

            var worldId = await(worldService.getWorldById(plotEntity.getWorld())).getOr(World::getWorldUuid, null);
            if (worldId == null) return Result.bad("Failed to get world with id " + plotEntity.getWorld() + ".");
            var bukkitWorld = Bukkit.getWorld(worldId);

            var attributes = await(plotRepo.getPlotAttributesForPlot(plotId).thenApply(attrEnts -> {
                if (attrEnts == null || attrEnts.isEmpty()) return List.<PlotAttribute>of();

                return attrEnts.stream().map(attrEnt -> {
                    var attr = await(attributeService.getAttributeById(attrEnt.getAttribute())).getOr(Attribute::getName, null);
                    if (attr == null) return null;
                    return new PlotAttribute(attr, attrEnt.getValue());
                }).filter(Objects::nonNull).toList();
            }));

            Plot parent = null;
            if (plotEntity.getParent() != null) parent = await(getPlot(plotEntity.getParent())).getOr(null);

            //todo: get plot group and users
            var plot = new Plot(plotEntity.getId(), new Location(bukkitWorld, plotEntity.getX(), plotEntity.getY(), plotEntity.getZ()), attributes, List.of(), parent, null);
            cacheService.getPlotCache().put(plotEntity.getId(), plot);
            return Result.good(plot);
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> setPlotLocation(UUID modifiedBy, int plotId, Location newLocation) {
        transaction.use();

        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");
            if (newLocation == null) return Result.bad("Location cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            var worldId = await(worldService.getWorldByUuid(newLocation.getWorld().getUID())).getOr(World::getId, -1);
            if (worldId == -1) return Result.bad("Failed to find world with uuid " + newLocation.getWorld().getUID() + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotLocation(userId, plotId, worldId, newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ()).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newPlot = new Plot(plot.getId(), newLocation, plot.getAttributes(), plot.getUsers(), plot.getParent(), plot.getPlotGroup());
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> setPlotParent(UUID modifiedBy, int plotId, int parentId) {
        transaction.use();

        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var parent = await(getPlot(parentId)).getOr(null);
            if (parent == null) return Result.bad("Failed to find plot with id " + parentId + ".");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotParent(userId, plotId, parentId).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), plot.getUsers(), parent, plot.getPlotGroup());
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> removePlotParent(UUID modifiedBy, int plotId) {
        transaction.use();

        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotParent(userId, plotId, null).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), plot.getUsers(), null, plot.getPlotGroup());
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> setPlotGroup(UUID modifiedBy, int plotId, String groupName) {
        transaction.use();

        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var group = cacheService.getGroupCache().get(groupName);
            if (group == null) return Result.bad("Failed to find group with name " + groupName + ".");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotGroup(userId, plotId, group.getId()).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), plot.getUsers(), plot.getParent(), group);
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> removePlotGroup(UUID modifiedBy, int plotId) {
        transaction.use();

        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotGroup(userId, plotId, null).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), plot.getUsers(), plot.getParent(), null);
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> setPlotAttribute(UUID modifiedBy, int plotId, String attributeName, String value) {
        transaction.use();
        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var attribute = await(attributeService.getAttribute(attributeName)).getOr(null);
            if (attribute == null) return Result.bad("Failed to find attribute with name " + attributeName + ".");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            var type = configService.getAttributeType(attribute.getType());
            if (type == null) return Result.bad("Failed to find attribute type with name " + attribute.getType() + ".");

            if (!type.isValidValue(value)) return Result.bad("Value " + value + " is not valid for attribute type " + type.getName() + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).insertPlotAttribute(userId, plotId, attribute.getId(), value).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newAttributes = new ArrayList<>(plot.getAttributes());
                newAttributes.removeIf(attr -> attr.getAttribute().equalsIgnoreCase(attributeName));
                newAttributes.add(new PlotAttribute(attributeName, value));
                var newPlot = new Plot(plot.getId(), plot.getLocation(), newAttributes, plot.getUsers(), plot.getParent(), plot.getPlotGroup());
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> removePlotAttribute(UUID modifiedBy, int plotId, String attributeName) {
        transaction.use();

        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var attribute = cacheService.getAttributeCache().get(attributeName);
            if (attribute == null) return Result.bad("Failed to find attribute with name " + attributeName + ".");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).deletePlotAttribute(userId, plotId, attribute.getId()).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newAttributes = new ArrayList<>(plot.getAttributes());
                var res = newAttributes.removeIf(attr -> attr.getAttribute().equalsIgnoreCase(attributeName));
                if (!res) return Result.bad("Failed to remove attribute " + attributeName + " from plot. Did not find attribute in plot attribute list.");

                var newPlot = new Plot(plot.getId(), plot.getLocation(), newAttributes, plot.getUsers(), plot.getParent(), plot.getPlotGroup());
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<List<PlotAttribute>>> getPlotAttributes(int plotId) {
        transaction.use();

        return transaction.getUnitOfWork().repo(IPlotRepository.class).getPlotAttributesForPlot(plotId).<Result<List<PlotAttribute>>>thenApply(attributes -> {
            if (attributes == null) return Result.bad("Failed to get plot attributes.");

            return Result.good(attributes.stream().map(attr -> {
                var attribute = await(attributeService.getAttributeById(attr.getAttribute())).getOr(null);
                if (attribute == null) return null;
                return new PlotAttribute(attribute.getName(), attr.getValue());
            }).filter(Objects::nonNull).toList());
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> addPlotUser(UUID modifiedBy, int plotId, UUID userId) {
        transaction.use();
        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var modifiedUser = await(userService.getUserByUuid(modifiedBy)).get();
            if (modifiedUser == null) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            var addedUser = await(userService.getUserByUuid(userId)).get();
            if (addedUser == null) return Result.bad("Failed to find user with uuid " + userId + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).insertPlotUser(modifiedUser.getId(), plotId, addedUser.getId()).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newUsers = new ArrayList<>(plot.getUsers());
                newUsers.removeIf(user -> user.getUserId().equals(userId));
                newUsers.add(new User(addedUser.getId(), addedUser.getUserId(), addedUser.getLastKnownName()));
                var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), newUsers, plot.getParent(), plot.getPlotGroup());
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> removePlotUser(UUID modifiedBy, int plotId, UUID userId) {
        transaction.use();
        return Util.<Result<Plot>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var modifiedUser = await(userService.getUserByUuid(modifiedBy)).get();
            if (modifiedUser == null) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            var removedUser = await(userService.getUserByUuid(userId)).get();
            if (removedUser == null) return Result.bad("Failed to find user with uuid " + userId + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).deletePlotUser(modifiedUser.getId(), plotId, removedUser.getId()).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update plot.");

                var newUsers = new ArrayList<>(plot.getUsers());
                var removed = newUsers.removeIf(user -> user.getUserId().equals(userId));
                if (!removed) return Result.bad("Failed to remove user from plot. Did not find user with uuid " + userId + " in plot user list.");

                var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), newUsers, plot.getParent(), plot.getPlotGroup());
                cacheService.getPlotCache().put(plot.getId(), newPlot);
                return Result.good(newPlot);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Plot>> deletePlot(UUID deletedBy, int plotId) {
        transaction.use();
        return Util.<Result<Plot>>async(() -> {
            if (deletedBy == null) return Result.bad("Deleter cannot be null.");

            var plot = await(getPlot(plotId)).getOr(null);
            if (plot == null) return Result.bad("Failed to find plot with id " + plotId + ".");

            var deletedUser = await(userService.getUserByUuid(deletedBy)).get();
            if (deletedUser == null) return Result.bad("Failed to find user with uuid " + deletedBy + ".");

            return await(transaction.getUnitOfWork().repo(IPlotRepository.class).deletePlot(deletedUser.getId(), plotId).thenApply(success -> {
                if (success == -1) return Result.bad("Failed to delete plot.");

                cacheService.getPlotCache().remove(plotId);
                return Result.good(plot);
            }));
        }).whenComplete(finishTransaction());

    }

    private <T extends Result<?>> BiConsumer<? super T, ? super Throwable> finishTransaction() {
        return (r, t) -> {
            if (t != null) logger.exception(new Exception(t));
            if (!r.successful()) transaction.abort();
            transaction.release();
        };
    }
}
