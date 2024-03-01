package com.njdaeger.taskmanager.dataaccess.repositories.implementations;

import com.njdaeger.taskmanager.dataaccess.IProcedure;
import com.njdaeger.taskmanager.dataaccess.models.TaskTypeEntity;
import com.njdaeger.taskmanager.dataaccess.repositories.ITaskTypeRepository;
import com.njdaeger.taskmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.taskmanager.dataaccess.Util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TaskTypeRepository implements ITaskTypeRepository {

    private final IPluginLogger logger;
    private final AbstractDatabaseTransaction<?> transaction;
    private final IProcedure procedures;

    public TaskTypeRepository(IPluginLogger logger, IProcedure procedures, AbstractDatabaseTransaction<?> transaction) {
        this.logger = logger;
        this.transaction = transaction;
        this.procedures = procedures;
        Util.await(initializeRepository());
    }

    @Override
    public CompletableFuture<Boolean> initializeRepository() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<List<TaskTypeEntity>> getTaskTypes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskTypes();
                return transaction.query(proc.getFirst(), TaskTypeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<TaskTypeEntity> getTaskTypeById(int taskTypeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskTypeById(taskTypeId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), TaskTypeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<TaskTypeEntity> getTaskTypeByName(String taskTypeName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskTypeByName(taskTypeName);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), TaskTypeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<TaskTypeEntity> getTaskTypeByPrefix(String taskTypePrefix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskTypeByPrefix(taskTypePrefix);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), TaskTypeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<TaskTypeEntity> insertTaskType(int createdBy, String taskTypeName, String prefix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertTaskType(createdBy, taskTypeName, prefix);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getTaskTypeById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<TaskTypeEntity> updateTaskType(int modifiedBy, int taskTypeId, Integer taskTypeVersion, String taskTypeName, String prefix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updateTaskType(modifiedBy, taskTypeId, taskTypeName, taskTypeVersion, prefix);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getTaskTypeById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteTaskType(int deletedBy, int taskTypeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deleteTaskType(deletedBy, taskTypeId);
                return transaction.execute(proc.getFirst(), proc.getSecond());
            } catch (Exception e) {
                logger.exception(e);
                return -1;
            }
        });
    }
}
