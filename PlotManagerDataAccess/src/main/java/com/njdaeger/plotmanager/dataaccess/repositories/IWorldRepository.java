package com.njdaeger.plotmanager.dataaccess.repositories;

import com.njdaeger.plotmanager.dataaccess.IRepository;
import com.njdaeger.plotmanager.dataaccess.models.PlotWorldEntity;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IWorldRepository extends IRepository {

    /**
     * Get all worlds from the database.
     * @return A list of all worlds in the database.
     */
    CompletableFuture<List<PlotWorldEntity>> getWorlds();

    /**
     * Get a world by its id.
     * @param id The id of the world.
     * @return The world with the given id.
     */
    CompletableFuture<PlotWorldEntity> getWorldById(int id);

    /**
     * Get a world by its name.
     * @param uuid The uuid of the world.
     * @return The world with the given name.
     */
    CompletableFuture<PlotWorldEntity> getWorldByUuid(UUID uuid);

    /**
     * Insert a world into the database.
     * @param createdBy The id of the user who created this world.
     * @param worldId The uuid of the world.
     * @param worldName The name of the world.
     * @return The newly created world, or null if the world could not be created.
     */
    CompletableFuture<PlotWorldEntity> insertWorld(int createdBy, UUID worldId, String worldName);

    /**
     * Update a world in the database.
     * @param modifiedBy The id of the user who modified this world.
     * @param worldId The id of the world to update
     * @param newUuid The new uuid of the world, or null to not update the uuid.
     * @param newWorldName The new name of the world, or null to not update the name.
     * @return The updated world, or null if the world could not be updated.
     */
    CompletableFuture<PlotWorldEntity> updateWorld(int modifiedBy, int worldId, UUID newUuid, String newWorldName);

    /**
     * Delete a world from the database.
     * @param deletedBy The id of the user who deleted this world.
     * @param worldId The world to delete.
     * @return The id of the deleted world, or -1 if the world could not be deleted.
     */
    CompletableFuture<Integer> deleteWorld(int deletedBy, int worldId);

}
