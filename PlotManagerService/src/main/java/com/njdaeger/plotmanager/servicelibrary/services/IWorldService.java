package com.njdaeger.plotmanager.servicelibrary.services;


import com.njdaeger.plotmanager.servicelibrary.transactional.ITransactionalService;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.World;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IWorldService extends ITransactionalService, ICached {

    /**
     * Get all worlds
     * @return A result with a list of worlds if successful, or a result with an empty list if the world retrieval was unsuccessful.
     */
    CompletableFuture<Result<List<World>>> getWorlds();

    /**
     * Get a world by its uuid
     * @param worldUuid The uuid of the world to get
     * @return A result with the world if successful, or a result with null if the world retrieval was unsuccessful.
     */
    CompletableFuture<Result<World>> getWorldByUuid(UUID worldUuid);

    /**
     * Get a world by its id
     * @param worldId The id of the world to get
     * @return A result with the world if successful, or a result with null if the world retrieval was unsuccessful.
     */
    CompletableFuture<Result<World>> getWorldById(int worldId);


    /**
     * Create a world
     * @param createdBy The creator of the world
     * @param world The world to create
     * @return A result with the created world if successful, or a result with null if the world creation was unsuccessful.
     */
    CompletableFuture<Result<World>> createWorld(UUID createdBy, org.bukkit.World world);

    /**
     * Update a world name
     * @param updatedBy The updater of the world
     * @param worldUuid The uuid of the world to update
     * @param newWorldName The new name of the world
     * @return A result with the updated world if successful, or a result with null if the world update was unsuccessful.
     */
    CompletableFuture<Result<World>> updateWorld(UUID updatedBy, UUID worldUuid, String newWorldName);

    /**
     * Update a world uuid
     * @param updatedBy The updater of the world
     * @param oldWorldUuid The old uuid of the world
     * @param newWorldUuid The new uuid of the world
     * @return A result with the updated world if successful, or a result with null if the world update was unsuccessful.
     */
    CompletableFuture<Result<World>> updateWorld(UUID updatedBy, UUID oldWorldUuid, UUID newWorldUuid);

    /**
     * Delete a world
     * @param deletedBy The deleter of the world
     * @param worldUuid The uuid of the world to delete
     * @return A result with the deleted world if successful, or a result with null if the world deletion was unsuccessful.
     */
    CompletableFuture<Result<World>> deleteWorld(UUID deletedBy, UUID worldUuid);

}
