package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.repositories.IPlotRepository;
import com.njdaeger.plotmanager.servicelibrary.PlotBuilder;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotAttribute;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.models.World;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IPlotService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.services.IWorldService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class PlotService implements IPlotService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final IWorldService worldService;
    private final IConfigService configService;
    private final ICacheService cacheService;

    public PlotService(IServiceTransaction transaction, IUserService userService, IWorldService worldService, IConfigService configService, ICacheService cacheService) {
        this.transaction = transaction;
        this.userService = userService;
        this.worldService = worldService;
        this.configService = configService;
        this.cacheService = cacheService;
    }

    @Override
    public CompletableFuture<Result<PlotBuilder>> createPlotBuilder(UUID creator, Location location) {
        transaction.use();
        if (creator == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Creator cannot be null."));
        }
        if (location == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Location cannot be null."));
        }
        if (cacheService.getPlotBuilderCache().get(creator) != null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Plot builder already exists for this creator."));
        }

        var userId = await(userService.getUserByUuid(creator)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + creator + "."));
        }

        transaction.release();
        var plotBuilder = new PlotBuilder(location);
        cacheService.getPlotBuilderCache().put(creator, plotBuilder);
        return CompletableFuture.completedFuture(Result.good(plotBuilder));
    }

    @Override
    public CompletableFuture<Result<PlotBuilder>> getPlotBuilder(UUID creator) {
        transaction.use();
        if (creator == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Creator cannot be null."));
        }
        var plotBuilder = cacheService.getPlotBuilderCache().get(creator);
        transaction.release();
        if (plotBuilder == null) return CompletableFuture.completedFuture(Result.bad("Failed to find plot builder for creator " + creator + "."));
        return CompletableFuture.completedFuture(Result.good(plotBuilder));
    }

    @Override
    public CompletableFuture<Result<PlotBuilder>> cancelPlotBuilder(UUID creator) {
        transaction.use();
        if (creator == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Creator cannot be null."));
        }
        var plotBuilder = cacheService.getPlotBuilderCache().remove(creator);
        transaction.release();
        if (plotBuilder == null) return CompletableFuture.completedFuture(Result.bad("Failed to find plot builder for creator " + creator + "."));
        return CompletableFuture.completedFuture(Result.good(plotBuilder));
    }

    @Override
    public CompletableFuture<Result<Plot>> finishPlotBuilder(UUID creator) {
        transaction.use();
        if (creator == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Creator cannot be null."));
        }
        var plotBuilder = cacheService.getPlotBuilderCache().remove(creator);
        if (plotBuilder == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot builder for creator " + creator + "."));
        }

        var userId = await(userService.getUserByUuid(creator)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + creator + "."));
        }

        var location = plotBuilder.getLocation();
        var worldId = await(worldService.getWorldByUuid(location.getWorld().getUID())).getOr(World::getId, -1);
        if (worldId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find world with uuid " + plotBuilder.getLocation().getWorld().getUID() + "."));
        }

        return CompletableFuture.completedFuture(await(transaction.getUnitOfWork().repo(IPlotRepository.class).insertPlot(userId, worldId, location.getBlockX(), location.getBlockY(), location.getBlockZ()).thenApply(plot -> {
            if (plot == null) {
                transaction.release();
                return Result.bad("Failed to create plot.");
            }
            for (var attr : plotBuilder.getAttributes()) {
                var res = await(setPlotAttribute(creator, plot.getId(), attr.getAttribute(), attr.getValue()));
                if (!res.successful()) {
                    transaction.release();
                    return Result.bad("Failed to set attribute " + attr.getAttribute() + " to " + attr.getValue() + ". Reason: " + res.message());
                }
            }
            if (plotBuilder.getParent() != null) {
                var res = await(setPlotParent(creator, plot.getId(), plotBuilder.getParent().getId()));
                if (!res.successful()) {
                    transaction.release();
                    return Result.bad("Failed to set plot parent to " + plotBuilder.getParent().getId() + ". Reason: " + res.message());
                }
            }
            transaction.release();
            var createdPlot = new Plot(plot.getId(), location, plotBuilder.getAttributes(), List.of(), plotBuilder.getParent(), null);
            cacheService.getPlotCache().put(plot.getId(), createdPlot);
            return Result.good(createdPlot);
        })));
    }

    @Override
    public CompletableFuture<Result<List<Plot>>> getPlots() {
        transaction.use();
        if (!cacheService.getPlotCache().isEmpty()) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(List.copyOf(cacheService.getPlotCache().values())));
        }
        return transaction.getUnitOfWork().repo(IPlotRepository.class).getPlots().thenApply(plots -> {
            if (plots == null) {
                transaction.release();
                return Result.bad("Failed to get plots.");
            }
            transaction.release();
            return Result.good(plots.stream().map(pe -> {
                var worldId = await(worldService.getWorldById(pe.getWorld())).getOr(World::getWorldUuid, null);
                if (worldId == null) return null;
                var world = Bukkit.getWorld(worldId);

                var attributes = await(getPlotAttributes(pe.getId())).getOr(List.of());
                Plot parent = null;
                if (pe.getParent() != null) parent = await(getPlot(pe.getParent())).getOr(null);
                var plot = new Plot(pe.getId(), new Location(world, pe.getX(), pe.getY(), pe.getZ()), attributes, List.of(), parent, null);
                cacheService.getPlotCache().put(plot.getId(), plot);
                return plot;
            }).filter(Objects::nonNull).toList());
        });
    }

    @Override
    public CompletableFuture<Result<Plot>> getPlot(int plotId) {
        transaction.use();
        if (cacheService.getPlotCache().containsKey(plotId)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(cacheService.getPlotCache().get(plotId)));
        }
        return transaction.getUnitOfWork().repo(IPlotRepository.class).getPlotById(plotId).thenApply(plot -> {
            if (plot == null) {
                transaction.release();
                return Result.bad("Failed to get plot.");
            }
            var worldId = await(worldService.getWorldById(plot.getWorld())).getOr(World::getWorldUuid, null);
            if (worldId == null) {
                transaction.release();
                return Result.bad("Failed to get world.");
            }
            var world = Bukkit.getWorld(worldId);

            var attributes = await(getPlotAttributes(plot.getId())).getOr(List.of());
            Plot parent = null;
            if (plot.getParent() != null) parent = await(getPlot(plot.getParent())).getOr(null);
            var newPlot = new Plot(plot.getId(), new Location(world, plot.getX(), plot.getY(), plot.getZ()), attributes, List.of(), parent, null);
            cacheService.getPlotCache().put(newPlot.getId(), newPlot);
            transaction.release();
            return Result.good(newPlot);
        });
    }

    @Override
    public CompletableFuture<Result<Plot>> setPlotLocation(UUID modifiedBy, int plotId, Location newLocation) {
        transaction.use();
        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }
        if (newLocation == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Location cannot be null."));
        }
        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        var worldId = await(worldService.getWorldByUuid(newLocation.getWorld().getUID())).getOr(World::getId, -1);
        if (worldId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find world with uuid " + newLocation.getWorld().getUID() + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotLocation(userId, plotId, worldId, newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ()).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newPlot = new Plot(plot.getId(), newLocation, plot.getAttributes(), plot.getUsers(), plot.getParent(), plot.getPlotGroup());
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });
    }

    @Override
    public CompletableFuture<Result<Plot>> setPlotParent(UUID modifiedBy, int plotId, int parentId) {
        transaction.use();
        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var parent = cacheService.getPlotCache().get(parentId);
        if (parent == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + parentId + "."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotParent(userId, plotId, parentId).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), plot.getUsers(), parent, plot.getPlotGroup());
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });

    }

    @Override
    public CompletableFuture<Result<Plot>> removePlotParent(UUID modifiedBy, int plotId) {
        transaction.use();
        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotParent(userId, plotId, null).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), plot.getUsers(), null, plot.getPlotGroup());
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });
    }

    @Override
    public CompletableFuture<Result<Plot>> setPlotGroup(UUID modifiedBy, int plotId, String groupName) {
        transaction.use();
        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var group = cacheService.getGroupCache().get(groupName);
        if (group == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find group with name " + groupName + "."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotGroup(userId, plotId, group.getId()).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), plot.getUsers(), plot.getParent(), group);
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });

    }

    @Override
    public CompletableFuture<Result<Plot>> removePlotGroup(UUID modifiedBy, int plotId) {
        transaction.use();

        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotGroup(userId, plotId, null).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), plot.getUsers(), plot.getParent(), null);
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });

    }

    @Override
    public CompletableFuture<Result<Plot>> setPlotAttribute(UUID modifiedBy, int plotId, String attributeName, String value) {
        transaction.use();
        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var attribute = cacheService.getAttributeCache().get(attributeName);
        if (attribute == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find attribute with name " + attributeName + "."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        var type = configService.getAttributeType(attribute.getType());
        if (type == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find attribute type with name " + attribute.getType() + "."));
        }

        if (!type.isValidValue(value)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Value " + value + " is not valid for attribute type " + type.getName() + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotAttribute(userId, plotId, attribute.getId(), value).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newAttributes = plot.getAttributes();
            newAttributes.removeIf(attr -> attr.getAttribute().equalsIgnoreCase(attributeName));
            newAttributes.add(new PlotAttribute(attributeName, value));
            var newPlot = new Plot(plot.getId(), plot.getLocation(), newAttributes, plot.getUsers(), plot.getParent(), plot.getPlotGroup());
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });
    }

    @Override
    public CompletableFuture<Result<Plot>> removePlotAttribute(UUID modifiedBy, int plotId, String attributeName) {
        transaction.use();

        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var attribute = cacheService.getAttributeCache().get(attributeName);
        if (attribute == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find attribute with name " + attributeName + "."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).updatePlotAttribute(userId, plotId, attribute.getId(), null).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newAttributes = plot.getAttributes();
            var res = newAttributes.removeIf(attr -> attr.getAttribute().equalsIgnoreCase(attributeName));
            if (!res) return Result.bad("Failed to remove attribute " + attributeName + " from plot. Did not find attribute in plot attribute list.");
            var newPlot = new Plot(plot.getId(), plot.getLocation(), newAttributes, plot.getUsers(), plot.getParent(), plot.getPlotGroup());
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });

    }

    @Override
    public CompletableFuture<Result<Plot>> addPlotUser(UUID modifiedBy, int plotId, UUID userId) {
        transaction.use();

        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var modifiedUser = await(userService.getUserByUuid(modifiedBy)).get();
        if (modifiedUser == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        var addedUser = await(userService.getUserByUuid(userId)).get();
        if (addedUser == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + userId + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).insertPlotUser(modifiedUser.getId(), plotId, addedUser.getId()).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newUsers = plot.getUsers();
            newUsers.removeIf(user -> user.getUserId().equals(userId));
            newUsers.add(new User(addedUser.getId(), addedUser.getUserId(), addedUser.getLastKnownName()));
            var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), newUsers, plot.getParent(), plot.getPlotGroup());
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });

    }

    @Override
    public CompletableFuture<Result<Plot>> removePlotUser(UUID modifiedBy, int plotId, UUID userId) {
        transaction.use();

        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var modifiedUser = await(userService.getUserByUuid(modifiedBy)).get();
        if (modifiedUser == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        var removedUser = await(userService.getUserByUuid(userId)).get();
        if (removedUser == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + userId + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).deletePlotUser(modifiedUser.getId(), plotId, removedUser.getId()).thenApply(success -> {
            transaction.release();
            if (success == null) return Result.bad("Failed to update plot.");
            var newUsers = plot.getUsers();
            var removed = newUsers.removeIf(user -> user.getUserId().equals(userId));
            if (!removed) return Result.bad("Failed to remove user from plot. Did not find user with uuid " + userId + " in plot user list.");
            var newPlot = new Plot(plot.getId(), plot.getLocation(), plot.getAttributes(), newUsers, plot.getParent(), plot.getPlotGroup());
            cacheService.getPlotCache().put(plot.getId(), newPlot);
            return Result.good(newPlot);
        });

    }

    @Override
    public CompletableFuture<Result<Plot>> deletePlot(UUID deletedBy, int plotId) {
        transaction.use();

        if (deletedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Deleter cannot be null."));
        }

        var plot = cacheService.getPlotCache().get(plotId);
        if (plot == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find plot with id " + plotId + "."));
        }

        var deletedUser = await(userService.getUserByUuid(deletedBy)).get();
        if (deletedUser == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + deletedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IPlotRepository.class).deletePlot(deletedUser.getId(), plotId).thenApply(success -> {
            transaction.release();
            if (success == -1) return Result.bad("Failed to delete plot.");
            cacheService.getPlotCache().remove(plotId);
            return Result.good(plot);
        });
    }
}
