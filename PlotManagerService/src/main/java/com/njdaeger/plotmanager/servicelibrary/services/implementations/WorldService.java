package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.repositories.IWorldRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.services.IWorldService;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.models.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class WorldService implements IWorldService {

    private final IUnitOfWork uow;
    private final IUserService userService;
    private static final Map<UUID, World> worldCache = new ConcurrentHashMap<>();

    public WorldService(IUnitOfWork uow, IUserService userService) {
        this.uow = uow;
        this.userService = userService;
    }

    @Override
    public CompletableFuture<Result<List<World>>> getWorlds() {
        if (!worldCache.isEmpty()) return CompletableFuture.completedFuture(Result.good(List.copyOf(worldCache.values())));

        return uow.repo(IWorldRepository.class).getWorlds().thenApply(worlds -> {
            worlds.forEach(world -> {
                var uuid = UUID.fromString(world.getUuid());
                worldCache.put(uuid, new World(world.getId(), uuid, world.getName()));
            });
            return Result.good(List.copyOf(worldCache.values()));
        });
    }

    @Override
    public CompletableFuture<Result<World>> getWorldByUuid(UUID worldUuid) {
        if (worldCache.containsKey(worldUuid)) return CompletableFuture.completedFuture(Result.good(worldCache.get(worldUuid)));

        return uow.repo(IWorldRepository.class).getWorldByUuid(worldUuid).thenApply(world -> {
            if (world == null) return Result.bad("World not found.");
            var uuid = UUID.fromString(world.getUuid());
            var newWorld = new World(world.getId(), uuid, world.getName());
            worldCache.put(uuid, newWorld);
            return Result.good(newWorld);
        });
    }

    @Override
    public CompletableFuture<Result<World>> createWorld(UUID createdBy, org.bukkit.World world) {
        if (worldCache.containsKey(world.getUID())) return CompletableFuture.completedFuture(Result.bad("A world with that uuid already exists."));

        var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
        if (userId == -1) return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + createdBy + "."));

        return uow.repo(IWorldRepository.class).insertWorld(userId, world.getUID(), world.getName()).thenApply(newWorld -> {
            if (newWorld == null) return Result.bad("Failed to create world.");
            var uuid = UUID.fromString(newWorld.getUuid());
            var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
            worldCache.put(uuid, newWorldModel);
            return Result.good(newWorldModel);
        });
    }

    @Override
    public CompletableFuture<Result<World>> updateWorld(UUID updatedBy, UUID worldUuid, String newWorldName) {
        var oldWorld = await(getWorldByUuid(worldUuid)).getOr(null);
        if (oldWorld == null) return CompletableFuture.completedFuture(Result.bad("Failed to find world with uuid " + worldUuid + "."));

        var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
        if (userId == -1) return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + updatedBy + "."));

        return uow.repo(IWorldRepository.class).updateWorld(userId, oldWorld.getId(), oldWorld.getWorldUuid(), newWorldName).thenApply(newWorld -> {
            if (newWorld == null) return Result.bad("Failed to update world.");
            var uuid = UUID.fromString(newWorld.getUuid());
            var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
            worldCache.put(uuid, newWorldModel);
            return Result.good(newWorldModel);
        });
    }

    @Override
    public CompletableFuture<Result<World>> updateWorld(UUID updatedBy, UUID oldWorldUuid, UUID newWorldUuid) {
        var oldWorld = await(getWorldByUuid(oldWorldUuid)).getOr(null);
        if (oldWorld == null)
            return CompletableFuture.completedFuture(Result.bad("Failed to find world with uuid " + oldWorldUuid + "."));

        var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
        if (userId == -1)
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + updatedBy + "."));

        return uow.repo(IWorldRepository.class).updateWorld(userId, oldWorld.getId(), newWorldUuid, oldWorld.getWorldName()).thenApply(newWorld -> {
            if (newWorld == null) return Result.bad("Failed to update world.");
            var uuid = UUID.fromString(newWorld.getUuid());
            var newWorldModel = new World(newWorld.getId(), uuid, newWorld.getName());
            worldCache.put(uuid, newWorldModel);
            return Result.good(newWorldModel);
        });
    }


    @Override
    public CompletableFuture<Result<World>> deleteWorld(UUID deletedBy, UUID worldUuid) {
        var oldWorld = await(getWorldByUuid(worldUuid)).getOr(null);
        if (oldWorld == null) return CompletableFuture.completedFuture(Result.bad("Failed to find world with uuid " + worldUuid + "."));

        var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
        if (userId == -1) return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + deletedBy + "."));

        return uow.repo(IWorldRepository.class).deleteWorld(userId, oldWorld.getId()).thenApply(deletedWorld -> {
            if (deletedWorld == -1) return Result.bad("Failed to delete world.");
            worldCache.remove(worldUuid);
            return Result.good(oldWorld);
        });
    }
}
