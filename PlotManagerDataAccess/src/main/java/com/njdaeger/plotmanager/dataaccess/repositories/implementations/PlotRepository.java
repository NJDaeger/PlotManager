package com.njdaeger.plotmanager.dataaccess.repositories.implementations;

import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.models.PlotAttributeEntity;
import com.njdaeger.plotmanager.dataaccess.models.PlotEntity;
import com.njdaeger.plotmanager.dataaccess.models.PlotUserEntity;
import com.njdaeger.plotmanager.dataaccess.repositories.IPlotRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import com.njdaeger.plotmanager.dataaccess.transactional.ExecutionConstants;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class PlotRepository implements IPlotRepository {

    private final IPluginLogger logger;
    private final AbstractDatabaseTransaction<?> transaction;
    private final IProcedure procedures;

    public PlotRepository(IPluginLogger logger, IProcedure procedures, AbstractDatabaseTransaction<?> transaction) {
        this.logger = logger;
        this.transaction = transaction;
        this.procedures = procedures;
        await(initializeRepository());
    }

    @Override
    public CompletableFuture<Boolean> initializeRepository() {
        return CompletableFuture.supplyAsync(() -> true);
    }

    @Override
    public CompletableFuture<List<PlotEntity>> getPlots() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectPlots();
                return transaction.query(proc.getFirst(), PlotEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<PlotEntity> getPlotById(int plotId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectPlotById(plotId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), PlotEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotEntity> insertPlot(int createdBy, int worldId, int x, int y, int z) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertPlot(createdBy, worldId, x, y, z);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotEntity> updatePlotLocation(int updatedBy, int plotId, Integer newWorldId, Integer newX, Integer newY, Integer newZ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updatePlotLocation(updatedBy, plotId, newWorldId, newX, newY, newZ);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotById(plotId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotEntity> updatePlotParent(int updatedBy, int plotId, Integer newParentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updatePlotParent(updatedBy, plotId, newParentId);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotById(plotId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotEntity> updatePlotGroup(int updatedBy, int plotId, Integer newGroupId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updatePlotGroup(updatedBy, plotId, newGroupId);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotById(plotId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotUserEntity> insertPlotUser(int insertedBy, int plotId, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertPlotUser(insertedBy, plotId, userId);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotUserById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotUserEntity> restorePlotUser(int restoredBy, int plotId, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.restorePlotUser(restoredBy, plotId, userId);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotUser(plotId, userId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotUserEntity> deletePlotUser(int deletedBy, int plotId, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deletePlotUser(deletedBy, plotId, userId);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotUser(plotId, userId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotUserEntity> getPlotUser(int plotId, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectPlotUser(plotId, userId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), PlotUserEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotUserEntity> getPlotUserById(int plotUserId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectPlotUserById(plotUserId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), PlotUserEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<List<PlotUserEntity>> getPlotUsersForPlot(int plotId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectPlotUsersForPlot(plotId);
                return transaction.query(proc.getFirst(), proc.getSecond(), PlotUserEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<PlotEntity> deletePlot(int deletedBy, int plotId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deletePlot(deletedBy, plotId);
                var plot = await(getPlotById(plotId));
                var res = transaction.execute(proc.getFirst(), proc.getSecond());
                if (res == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return plot;
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<List<PlotAttributeEntity>> getPlotAttributesForPlot(int plotId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectPlotAttributesForPlot(plotId);
                return transaction.query(proc.getFirst(), proc.getSecond(), PlotAttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<PlotAttributeEntity> getPlotAttributeForPlotByAttributeId(int plotId, int attributeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectPlotAttributeForPlotByAttributeId(plotId, attributeId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), PlotAttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotAttributeEntity> getPlotAttributeForPlotById(int plotAttributeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectPlotAttributeForPlotById(plotAttributeId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), PlotAttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotAttributeEntity> insertPlotAttribute(int createdBy, int plotId, int attributeId, String value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertPlotAttribute(createdBy, plotId, attributeId, value);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotAttributeForPlotById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotAttributeEntity> updatePlotAttribute(int updatedBy, int plotId, Integer attributeId, String value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updatePlotAttribute(updatedBy, plotId, attributeId, value);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return await(getPlotAttributeForPlotByAttributeId(plotId, attributeId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<PlotAttributeEntity> deletePlotAttribute(int deletedBy, int plotId, int attributeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deletePlotAttribute(deletedBy, plotId, attributeId);
                var existing = await(getPlotAttributeForPlotByAttributeId(plotId, attributeId));
                var res = transaction.execute(proc.getFirst(), proc.getSecond());
                if (res == ExecutionConstants.NO_ROWS_AFFECTED) return null;
                return existing;
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }
}
