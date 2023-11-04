package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.Util;
import com.njdaeger.plotmanager.dataaccess.repositories.IWorldRepository;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.models.World;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.services.IWorldService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class WorldService implements IWorldService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final ICacheService cacheService;
    private final IPluginLogger logger;

    public WorldService(IServiceTransaction transaction, IUserService userService, ICacheService cacheService, IPluginLogger logger) {
        this.transaction = transaction;
        this.userService = userService;
        this.cacheService = cacheService;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<Result<List<World>>> getWorlds() {
        transaction.use();

        return Util.async(() -> {
            if (!cacheService.getWorldCache().isEmpty()) return Result.good(List.copyOf(cacheService.getWorldCache().values()));

            return await(transaction.getUnitOfWork().repo(IWorldRepository.class).getWorlds().thenApply(worlds -> {
                worlds.forEach(world -> {
                    var uuid = UUID.fromString(world.getUuid());
                    cacheService.getWorldCache().put(uuid, new World(world.getId(), uuid, world.getName()));
                });
                return Result.good(List.copyOf(cacheService.getWorldCache().values()));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<World>> getWorldByUuid(UUID worldUuid) {
        transaction.use();

        return Util.<Result<World>>async(() -> {
            if (cacheService.getWorldCache().containsKey(worldUuid)) return Result.good(cacheService.getWorldCache().get(worldUuid));

            return await(transaction.getUnitOfWork().repo(IWorldRepository.class).getWorldByUuid(worldUuid).thenApply(world -> {
                if (world == null) return Result.bad("World not found.");
                var uuid = UUID.fromString(world.getUuid());
                var newWorld = new World(world.getId(), uuid, world.getName());
                cacheService.getWorldCache().put(uuid, newWorld);
                return Result.good(newWorld);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<World>> getWorldById(int worldId) {
        transaction.use();

        return Util.<Result<World>>async(() -> {
            if (cacheService.getWorldCache().values().stream().anyMatch(world -> world.getId() == worldId)) return Result.good(cacheService.getWorldCache().values().stream().filter(world -> world.getId() == worldId).findFirst().orElse(null));

            return await(transaction.getUnitOfWork().repo(IWorldRepository.class).getWorldById(worldId).thenApply(world -> {
                if (world == null) return Result.bad("World not found.");
                var uuid = UUID.fromString(world.getUuid());
                var newWorld = new World(world.getId(), uuid, world.getName());
                cacheService.getWorldCache().put(uuid, newWorld);
                return Result.good(newWorld);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<World>> createWorld(UUID createdBy, org.bukkit.World world) {
        transaction.use();

        return Util.<Result<World>>async(() -> {
            if (cacheService.getWorldCache().containsKey(world.getUID())) return Result.bad("A world with that uuid already exists.");

            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + createdBy + ".");

            return await(transaction.getUnitOfWork().repo(IWorldRepository.class).insertWorld(userId, world.getUID(), world.getName()).thenApply(newWorld -> {
                if (newWorld == null) return Result.bad("Failed to create world.");
                var uuid = UUID.fromString(newWorld.getUuid());
                var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
                cacheService.getWorldCache().put(uuid, newWorldModel);
                return Result.good(newWorldModel);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<World>> updateWorld(UUID updatedBy, UUID worldUuid, String newWorldName) {
        transaction.use();

        return Util.<Result<World>>async(() -> {
            var oldWorld = await(getWorldByUuid(worldUuid)).getOr(null);
            if (oldWorld == null) return Result.bad("Failed to find world with uuid " + worldUuid + ".");

            var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + updatedBy + ".");

            return await(transaction.getUnitOfWork().repo(IWorldRepository.class).updateWorld(userId, oldWorld.getId(), oldWorld.getWorldUuid(), newWorldName).thenApply(newWorld -> {
                if (newWorld == null) return Result.bad("Failed to update world.");
                var uuid = UUID.fromString(newWorld.getUuid());
                var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
                cacheService.getWorldCache().put(uuid, newWorldModel);
                return Result.good(newWorldModel);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<World>> updateWorld(UUID updatedBy, UUID oldWorldUuid, UUID newWorldUuid) {
        transaction.use();

        return Util.<Result<World>>async(() -> {
            var oldWorld = await(getWorldByUuid(oldWorldUuid)).getOr(null);
            if (oldWorld == null) return Result.bad("Failed to find world with uuid " + oldWorldUuid + ".");

            var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + updatedBy + ".");

            return await(transaction.getUnitOfWork().repo(IWorldRepository.class).updateWorld(userId, oldWorld.getId(), newWorldUuid, oldWorld.getWorldName()).thenApply(newWorld -> {
                if (newWorld == null) return Result.bad("Failed to update world.");
                var uuid = UUID.fromString(newWorld.getUuid());
                var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
                cacheService.getWorldCache().put(uuid, newWorldModel);
                return Result.good(newWorldModel);
            }));
        }).whenComplete(finishTransaction());
    }


    @Override
    public CompletableFuture<Result<World>> deleteWorld(UUID deletedBy, UUID worldUuid) {
        transaction.use();

        return Util.<Result<World>>async(() -> {
            var oldWorld = await(getWorldByUuid(worldUuid)).getOr(null);
            if (oldWorld == null) return Result.bad("Failed to find world with uuid " + worldUuid + ".");

            var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + deletedBy + ".");

            return await(transaction.getUnitOfWork().repo(IWorldRepository.class).deleteWorld(userId, oldWorld.getId()).thenApply(deletedWorld -> {
                if (deletedWorld == -1) return Result.bad("Failed to delete world.");
                cacheService.getWorldCache().remove(worldUuid);
                return Result.good(oldWorld);
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

    @Override
    public void clearCache() {
        cacheService.getWorldCache().clear();
    }

    @Override
    public CompletableFuture<Void> initializeCache() {
        transaction.use();

        return transaction.getUnitOfWork().repo(IWorldRepository.class).getWorlds().thenApply(worlds -> {
            worlds.forEach(world -> {
                var uuid = UUID.fromString(world.getUuid());
                cacheService.getWorldCache().put(uuid, new World(world.getId(), uuid, world.getName()));
            });
            return Result.good(List.copyOf(cacheService.getWorldCache().values()));
        }).whenComplete(finishTransaction()).thenAccept(r -> {});
    }
}
