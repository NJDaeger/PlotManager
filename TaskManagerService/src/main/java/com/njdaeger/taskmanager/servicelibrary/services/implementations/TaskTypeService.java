package com.njdaeger.taskmanager.servicelibrary.services.implementations;

import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.taskmanager.dataaccess.repositories.ITaskTypeRepository;
import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.models.User;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskTypeService;
import com.njdaeger.taskmanager.servicelibrary.services.IUserService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class TaskTypeService implements ITaskTypeService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final ICacheService cacheService;
    private final IPluginLogger logger;

    public TaskTypeService(IServiceTransaction transaction, IUserService userService, ICacheService cacheService, IPluginLogger logger) {
        this.transaction = transaction;
        this.userService = userService;
        this.cacheService = cacheService;
        this.logger = logger;
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
        cacheService.getTaskTypeCache().clear();
    }

    @Override
    public CompletableFuture<Void> initializeCache() {
        transaction.use();

        return transaction.getUnitOfWork().repo(ITaskTypeRepository.class).getTaskTypes().thenApply(taskTypes -> {
            taskTypes.forEach(taskType -> cacheService.getTaskTypeCache().put(taskType.getPrefix(), new TaskType(taskType.getId(), taskType.getName(), taskType.getPrefix(), taskType.getTaskTypeVersion())));
            return Result.good(List.copyOf(cacheService.getTaskTypeCache().values()));
        }).whenComplete(finishTransaction()).thenAccept(r -> {});
    }

    @Override
    public CompletableFuture<Result<TaskType>> createTaskType(UUID createdBy, String taskTypeName, String taskTypePrefix) {
        transaction.use();

        return Util.<Result<TaskType>>async(() -> {
            if (taskTypeName == null || taskTypeName.isBlank()) return Result.bad("Task type name cannot be null or blank.");
            if (taskTypePrefix == null || taskTypePrefix.isBlank()) return Result.bad("Task type prefix cannot be null or blank.");
            if (cacheService.getTaskTypeCache().containsKey(taskTypePrefix)) return Result.bad("Task type with that prefix already exists.");

            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + createdBy);

            return await(transaction.getUnitOfWork().repo(ITaskTypeRepository.class).insertTaskType(userId, taskTypeName, taskTypePrefix).thenApply(taskType -> {
                if (taskType == null) return Result.bad("Failed to insert task type.");

                var createdType = new TaskType(taskType.getId(), taskType.getName(), taskType.getPrefix(), taskType.getTaskTypeVersion());

                cacheService.getTaskTypeCache().put(taskType.getPrefix(),createdType);
                return Result.good(createdType);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<TaskType>> getTaskType(int taskTypeId) {
        transaction.use();

        return Util.<Result<TaskType>>async(() -> {
            var taskType = cacheService.getTaskTypeCache().values().stream().filter(t -> t.getTaskTypeId() == taskTypeId).findFirst().orElse(null);
            if (taskType != null) return Result.good(taskType);

            return await(transaction.getUnitOfWork().repo(ITaskTypeRepository.class).getTaskTypeById(taskTypeId).thenApply(t -> {
                if (t == null) return Result.bad("Task type with id " + taskTypeId + " does not exist.");

                var foundType = new TaskType(t.getId(), t.getName(), t.getPrefix(), t.getTaskTypeVersion());
                cacheService.getTaskTypeCache().put(t.getPrefix(), foundType);
                return Result.good(foundType);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<TaskType>> getTaskType(String taskTypePrefix) {
        transaction.use();

        return Util.<Result<TaskType>>async(() -> {
            var taskType = cacheService.getTaskTypeCache().get(taskTypePrefix);
            if (taskType != null) return Result.good(taskType);

            return await(transaction.getUnitOfWork().repo(ITaskTypeRepository.class).getTaskTypeByPrefix(taskTypePrefix).thenApply(t -> {
                if (t == null) return Result.bad("Task type with prefix " + taskTypePrefix + " does not exist.");

                var foundType = new TaskType(t.getId(), t.getName(), t.getPrefix(), t.getTaskTypeVersion());
                cacheService.getTaskTypeCache().put(t.getPrefix(), foundType);
                return Result.good(foundType);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<TaskType>> updateTaskType(UUID updatedBy, int taskTypeId, String taskTypeName, String taskTypePrefix) {
        transaction.use();

        return Util.<Result<TaskType>>async(() -> {
            if (taskTypeName == null || taskTypeName.isBlank()) return Result.bad("Task type name cannot be null or blank.");
            if (taskTypePrefix == null || taskTypePrefix.isBlank()) return Result.bad("Task type prefix cannot be null or blank.");

            var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + updatedBy);

            return await(transaction.getUnitOfWork().repo(ITaskTypeRepository.class).updateTaskType(userId, taskTypeId, null, taskTypeName, taskTypePrefix).thenApply(taskType -> {
                if (taskType == null) return Result.bad("Failed to update task type.");

                var updatedType = new TaskType(taskType.getId(), taskType.getName(), taskType.getPrefix(), taskType.getTaskTypeVersion());
                cacheService.getTaskTypeCache().put(taskType.getPrefix(), updatedType);
                return Result.good(updatedType);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<TaskType>> updateTaskTypeVersion(UUID updatedBy, int taskTypeId, int taskTypeVersion) {
        transaction.use();

        return Util.<Result<TaskType>>async(() -> {
            var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + updatedBy);

            return await(transaction.getUnitOfWork().repo(ITaskTypeRepository.class).updateTaskType(userId, taskTypeId, taskTypeVersion, null, null).thenApply(taskType -> {
                if (taskType == null) return Result.bad("Failed to update task type version.");

                var updatedType = new TaskType(taskType.getId(), taskType.getName(), taskType.getPrefix(), taskType.getTaskTypeVersion());
                cacheService.getTaskTypeCache().put(taskType.getPrefix(), updatedType);
                return Result.good(updatedType);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<TaskType>> deleteTaskType(UUID deletedBy, int taskTypeId) {
        transaction.use();

        return Util.<Result<TaskType>>async(() -> {
            var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + deletedBy);

            return await(transaction.getUnitOfWork().repo(ITaskTypeRepository.class).deleteTaskType(userId, taskTypeId).thenApply(deleted -> {
                if (deleted == -1) return Result.bad("Failed to delete task type.");

                var taskType = cacheService.getTaskTypeCache().values().stream().filter(t -> t.getTaskTypeId() == taskTypeId).findFirst().orElse(null);
                if (taskType != null) cacheService.getTaskTypeCache().remove(taskType.getTaskTypePrefix());
                return Result.good(taskType);
            }));
        }).whenComplete(finishTransaction());
    }
}
