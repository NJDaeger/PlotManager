package com.njdaeger.plotmanager.dataaccess.repositories;

import com.njdaeger.plotmanager.dataaccess.IRepository;
import com.njdaeger.plotmanager.dataaccess.models.PlotWorldEntity;

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
     * @param world The world to insert.
     * @return True if the world was inserted, false otherwise.
     */
    CompletableFuture<Boolean> insertWorld(PlotWorldEntity world);

    /**
     * Update a world in the database.
     * @param world The world to update.
     * @return True if the world was updated, false otherwise.
     */
    CompletableFuture<Boolean> updateWorld(PlotWorldEntity world);

    /**
     * Delete a world from the database.
     * @param worldId The world to delete.
     * @return True if the world was deleted, false otherwise.
     */
    CompletableFuture<Boolean> deleteWorld(int worldId);

}
