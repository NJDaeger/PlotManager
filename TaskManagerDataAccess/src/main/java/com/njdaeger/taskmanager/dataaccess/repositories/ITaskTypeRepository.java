package com.njdaeger.taskmanager.dataaccess.repositories;

import com.njdaeger.taskmanager.dataaccess.IRepository;
import com.njdaeger.taskmanager.dataaccess.models.TaskTypeEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the interactions with the following tables:
 * * TaskType
 * * TaskTypeAttributes
 */
public interface ITaskTypeRepository extends IRepository {

    /**
     * Get all task types
     * @return A list of task types
     */
    CompletableFuture<List<TaskTypeEntity>> getTaskTypes();

    /**
     * Get a task type by id
     * @param taskTypeId The id of the task type
     * @return The task type
     */
    CompletableFuture<TaskTypeEntity> getTaskTypeById(int taskTypeId);

    /**
     * Get a task type by name
     * @param taskTypeName The name of the task type
     * @return The task type
     */
    CompletableFuture<TaskTypeEntity> getTaskTypeByName(String taskTypeName);

    /**
     * Get a task type by prefix
     * @param taskTypePrefix The prefix of the task type
     * @return The task type
     */
    CompletableFuture<TaskTypeEntity> getTaskTypeByPrefix(String taskTypePrefix);

    /**
     * Insert a task type
     * @param createdBy The id of the user who created the task type
     * @param taskTypeName The name of the task type
     * @param prefix The prefix of the task type
     * @return The inserted task type, or null if the task type was not inserted
     */
    CompletableFuture<TaskTypeEntity> insertTaskType(int createdBy, String taskTypeName, String prefix);

    /**
     * Update a task type
     * @param modifiedBy The id of the user who updated the task type
     * @param taskTypeId The id of the task type to update
     * @param taskTypeName The new name of the task type, or null to keep the old name
     * @param taskTypeVersion The new version of the task type, or null to keep the old version
     * @param prefix The new prefix of the task type, or null to keep the old prefix
     * @return The updated task type, or null if the task type was not updated
     */
    CompletableFuture<TaskTypeEntity> updateTaskType(int modifiedBy, int taskTypeId, Integer taskTypeVersion, String taskTypeName, String prefix);

    /**
     * Delete a task type
     * @param deletedBy The id of the user who deleted the task type
     * @param taskTypeId The id of the task type to delete
     * @return The deleted task type id, or -1 if the task type was not deleted
     */
    CompletableFuture<Integer> deleteTaskType(int deletedBy, int taskTypeId);

}
