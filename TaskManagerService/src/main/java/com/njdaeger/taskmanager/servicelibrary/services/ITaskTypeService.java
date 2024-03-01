package com.njdaeger.taskmanager.servicelibrary.services;

import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.transactional.ITransactionalService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ITaskTypeService extends ITransactionalService, ICached {

    CompletableFuture<Result<TaskType>> createTaskType(UUID createdBy, String taskTypeName, String taskTypePrefix);

    CompletableFuture<Result<TaskType>> getTaskType(int taskTypeId);

    CompletableFuture<Result<TaskType>> getTaskType(String taskTypePrefix);

    CompletableFuture<Result<TaskType>> updateTaskType(UUID updatedBy, int taskTypeId, String taskTypeName, String taskTypePrefix);

    CompletableFuture<Result<TaskType>> updateTaskTypeVersion(UUID updatedBy, int taskTypeId, int taskTypeVersion);

    CompletableFuture<Result<TaskType>> deleteTaskType(UUID deletedBy, int taskTypeId);

}
