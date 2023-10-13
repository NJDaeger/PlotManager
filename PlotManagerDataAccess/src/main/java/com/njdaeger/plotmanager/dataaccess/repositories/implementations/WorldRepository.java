package com.njdaeger.plotmanager.dataaccess.repositories.implementations;

import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.models.PlotWorldEntity;
import com.njdaeger.plotmanager.dataaccess.repositories.IWorldRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.AbstractDatabaseTransaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class WorldRepository implements IWorldRepository {

    private final IPluginLogger logger;
    private final AbstractDatabaseTransaction<?> transaction;
    private final IProcedure procedures;

    public WorldRepository(IPluginLogger logger, IProcedure procedures, AbstractDatabaseTransaction<?> transaction) {
        this.logger = logger;
        this.transaction = transaction;
        this.procedures = procedures;
        await(initializeRepository());
    }

    @Override
    public CompletableFuture<Boolean> initializeRepository() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<List<PlotWorldEntity>> getWorlds() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectWorlds();
                return transaction.query(proc.getFirst(), PlotWorldEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<PlotWorldEntity> getWorldById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectWorldById(id);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), PlotWorldEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotWorldEntity> getWorldByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectWorldByUuid(uuid);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), PlotWorldEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotWorldEntity> insertWorld(int createdBy, UUID worldId, String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertWorld(createdBy, worldId, worldName);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return await(getWorldById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotWorldEntity> updateWorld(int modifiedBy, int worldId, UUID newUuid, String newWorldName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updateWorld(modifiedBy, worldId, newUuid, newWorldName);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return await(getWorldById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteWorld(int deletedBy, int worldId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deleteWorld(deletedBy, worldId);
                return transaction.execute(proc.getFirst(), proc.getSecond());
            } catch (Exception e) {
                logger.exception(e);
                return -1;
            }
        });
    }
}
