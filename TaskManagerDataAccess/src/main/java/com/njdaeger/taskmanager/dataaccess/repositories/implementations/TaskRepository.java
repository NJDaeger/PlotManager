package com.njdaeger.taskmanager.dataaccess.repositories.implementations;

import com.njdaeger.taskmanager.dataaccess.IProcedure;
import com.njdaeger.taskmanager.dataaccess.models.TaskAttributeEntity;
import com.njdaeger.taskmanager.dataaccess.models.TaskEntity;
import com.njdaeger.taskmanager.dataaccess.repositories.ITaskRepository;
import com.njdaeger.taskmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.taskmanager.dataaccess.Util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TaskRepository implements ITaskRepository {

    private final IPluginLogger logger;
    private final AbstractDatabaseTransaction<?> transaction;
    private final IProcedure procedures;

    public TaskRepository(IPluginLogger logger, IProcedure procedures, AbstractDatabaseTransaction<?> transaction) {
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
    public CompletableFuture<List<TaskEntity>> getTasks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTasks();
                return transaction.query(proc.getFirst(), TaskEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<TaskEntity> getTaskById(int taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskById(taskId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), TaskEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<TaskEntity> getTaskByTaskKey(int projectId, int taskTypeId, int taskKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskByProjectAndTypeAndKey(projectId, taskTypeId, taskKey);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), TaskEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<List<TaskEntity>> getTasksOfProject(int projectId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTasksByProject(projectId);
                return transaction.query(proc.getFirst(), TaskEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<List<TaskEntity>> getTasksOfProjectAndType(int projectId, int typeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTasksByProjectAndType(projectId, typeId);
                return transaction.query(proc.getFirst(), TaskEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<TaskEntity> insertTask(int createdBy, int typeId, Integer parentId, int projectId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertTask(createdBy, typeId, parentId, projectId);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getTaskById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<TaskEntity> updateTask(int modifiedBy, int taskId, Integer newParentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updateTask(modifiedBy, taskId, newParentId);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getTaskById(taskId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteTask(int deletedBy, int taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deleteTask(deletedBy, taskId);
                return transaction.execute(proc.getFirst(), proc.getSecond());
            } catch (Exception e) {
                logger.exception(e);
                return -1;
            }
        });
    }

    @Override
    public CompletableFuture<List<TaskAttributeEntity>> getTaskAttributesOfTask(int taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskAttributesForTask(taskId);
                return transaction.query(proc.getFirst(), TaskAttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<TaskAttributeEntity> getTaskAttributeById(int taskAttributeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskAttributeById(taskAttributeId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), TaskAttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<List<TaskAttributeEntity>> getTaskAttributesByAttributeId(int taskId, int attributeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectTaskAttributesForTaskByAttributeId(taskId, attributeId);
                return transaction.query(proc.getFirst(), TaskAttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<TaskAttributeEntity> insertTaskAttribute(int createdBy, int taskId, int attributeId, String valueJson) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertTaskAttribute(createdBy, taskId, attributeId, valueJson);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getTaskAttributeById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<TaskAttributeEntity> updateTaskAttribute(int modifiedBy, int taskAttributeId, String valueJson) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updateTaskAttribute(modifiedBy, taskAttributeId, valueJson);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getTaskAttributeById(taskAttributeId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteTaskAttribute(int deletedBy, int taskAttributeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deleteTaskAttribute(deletedBy, taskAttributeId);
                return transaction.execute(proc.getFirst(), proc.getSecond());
            } catch (Exception e) {
                logger.exception(e);
                return -1;
            }
        });
    }
}
