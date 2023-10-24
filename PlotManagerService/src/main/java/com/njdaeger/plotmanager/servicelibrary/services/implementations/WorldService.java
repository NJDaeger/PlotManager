package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.repositories.IWorldRepository;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.models.World;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.services.IWorldService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class WorldService implements IWorldService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final ICacheService cacheService;

    public WorldService(IServiceTransaction transaction, IUserService userService, ICacheService cacheService) {
        this.transaction = transaction;
        this.userService = userService;
        this.cacheService = cacheService;
    }

    @Override
    public CompletableFuture<Result<List<World>>> getWorlds() {
        transaction.use();
        if (!cacheService.getWorldCache().isEmpty()) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(List.copyOf(cacheService.getWorldCache().values())));
        }

        return transaction.getUnitOfWork().repo(IWorldRepository.class).getWorlds().thenApply(worlds -> {
            transaction.release();
            worlds.forEach(world -> {
                var uuid = UUID.fromString(world.getUuid());
                cacheService.getWorldCache().put(uuid, new World(world.getId(), uuid, world.getName()));
            });
            return Result.good(List.copyOf(cacheService.getWorldCache().values()));
        });
    }

    @Override
    public CompletableFuture<Result<World>> getWorldByUuid(UUID worldUuid) {
        transaction.use();
        if (cacheService.getWorldCache().containsKey(worldUuid)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(cacheService.getWorldCache().get(worldUuid)));
        }

        return transaction.getUnitOfWork().repo(IWorldRepository.class).getWorldByUuid(worldUuid).thenApply(world -> {
            transaction.release();
            if (world == null) return Result.bad("World not found.");
            var uuid = UUID.fromString(world.getUuid());
            var newWorld = new World(world.getId(), uuid, world.getName());
            cacheService.getWorldCache().put(uuid, newWorld);
            return Result.good(newWorld);
        });
    }

    @Override
    public CompletableFuture<Result<World>> getWorldById(int worldId) {
        transaction.use();
        if (cacheService.getWorldCache().values().stream().anyMatch(world -> world.getId() == worldId)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(cacheService.getWorldCache().values().stream().filter(world -> world.getId() == worldId).findFirst().orElse(null)));
        }

        return transaction.getUnitOfWork().repo(IWorldRepository.class).getWorldById(worldId).thenApply(world -> {
            transaction.release();
            if (world == null) return Result.bad("World not found.");
            var uuid = UUID.fromString(world.getUuid());
            var newWorld = new World(world.getId(), uuid, world.getName());
            cacheService.getWorldCache().put(uuid, newWorld);
            return Result.good(newWorld);
        });
    }

    @Override
    public CompletableFuture<Result<World>> createWorld(UUID createdBy, org.bukkit.World world) {
        transaction.use();
        if (cacheService.getWorldCache().containsKey(world.getUID())) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("A world with that uuid already exists."));
        }

        var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + createdBy + "."));
        }

        return transaction.getUnitOfWork().repo(IWorldRepository.class).insertWorld(userId, world.getUID(), world.getName()).thenApply(newWorld -> {
            if (newWorld == null) {
                transaction.release();
                transaction.abort();
                return Result.bad("Failed to create world.");
            }
            transaction.release();
            var uuid = UUID.fromString(newWorld.getUuid());
            var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
            cacheService.getWorldCache().put(uuid, newWorldModel);
            return Result.good(newWorldModel);
        });
    }

    @Override
    public CompletableFuture<Result<World>> updateWorld(UUID updatedBy, UUID worldUuid, String newWorldName) {
        transaction.use();
        var oldWorld = await(getWorldByUuid(worldUuid)).getOr(null);
        if (oldWorld == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find world with uuid " + worldUuid + "."));
        }

        var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + updatedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IWorldRepository.class).updateWorld(userId, oldWorld.getId(), oldWorld.getWorldUuid(), newWorldName).thenApply(newWorld -> {
            if (newWorld == null) {
                transaction.release();
                transaction.abort();
                return Result.bad("Failed to update world.");
            }
            transaction.release();
            var uuid = UUID.fromString(newWorld.getUuid());
            var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
            cacheService.getWorldCache().put(uuid, newWorldModel);
            return Result.good(newWorldModel);
        });
    }

    @Override
    public CompletableFuture<Result<World>> updateWorld(UUID updatedBy, UUID oldWorldUuid, UUID newWorldUuid) {
        transaction.use();
        var oldWorld = await(getWorldByUuid(oldWorldUuid)).getOr(null);
        if (oldWorld == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find world with uuid " + oldWorldUuid + "."));
        }

        var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + updatedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IWorldRepository.class).updateWorld(userId, oldWorld.getId(), newWorldUuid, oldWorld.getWorldName()).thenApply(newWorld -> {
            if (newWorld == null) {
                transaction.release();
                transaction.abort();
                return Result.bad("Failed to update world.");
            }
            transaction.release();
            var uuid = UUID.fromString(newWorld.getUuid());
            var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
            cacheService.getWorldCache().put(uuid, newWorldModel);
            return Result.good(newWorldModel);
        });
    }


    @Override
    public CompletableFuture<Result<World>> deleteWorld(UUID deletedBy, UUID worldUuid) {
        transaction.use();
        var oldWorld = await(getWorldByUuid(worldUuid)).getOr(null);
        if (oldWorld == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find world with uuid " + worldUuid + "."));
        }

        var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + deletedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IWorldRepository.class).deleteWorld(userId, oldWorld.getId()).thenApply(deletedWorld -> {
            if (deletedWorld == -1) {
                transaction.release();
                transaction.abort();
                return Result.bad("Failed to delete world.");
            }
            transaction.release();
            cacheService.getWorldCache().remove(worldUuid);
            return Result.good(oldWorld);
        });
    }
}
