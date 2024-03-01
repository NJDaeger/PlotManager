package com.njdaeger.taskmanager.dataaccess.repositories;

import com.njdaeger.taskmanager.dataaccess.IRepository;
import com.njdaeger.taskmanager.dataaccess.models.TaskAttributeEntity;
import com.njdaeger.taskmanager.dataaccess.models.TaskEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the interactions with the following tables:
 * * Task
 * * TaskAttributes
 */
public interface ITaskRepository extends IRepository {

    /**
     * Get all tasks
     * @return A list of tasks
     */
    CompletableFuture<List<TaskEntity>> getTasks();

    /**
     * Get a task by id
     * @param taskId The id of the task
     * @return The task
     */
    CompletableFuture<TaskEntity> getTaskById(int taskId);

    /**
     * Get all tasks of a project
     * @param projectId The id of the project
     * @return A list of tasks
     */
    CompletableFuture<List<TaskEntity>> getTasksOfProject(int projectId);

    /**
     * Get all tasks of a type
     * @param projectId The id of the project
     * @param typeId The id of the task type
     * @return A list of tasks
     */
    CompletableFuture<List<TaskEntity>> getTasksOfProjectAndType(int projectId, int typeId);


    /**
     * Insert a task
     * @param createdBy The id of the user who created the task
     * @param typeId The id of the task type
     * @param parentId The id of the parent task, or null if this task has no parent
     * @return The inserted task, or null if the task was not inserted
     */
    CompletableFuture<TaskEntity> insertTask(int createdBy, int typeId, Integer parentId, int projectId);

    /**
     * Update a task
     * @param modifiedBy The id of the user who updated the task
     * @param taskId The id of the task to update
     * @param newParentId The new parent id of the task, or null to not update the parent id
     * @return The updated task, or null if the task was not updated
     */
    CompletableFuture<TaskEntity> updateTask(int modifiedBy, int taskId, Integer newParentId);

    /**
     * Delete a task
     * @param deletedBy The id of the user who deleted the task
     * @param taskId The id of the task to delete
     * @return The deleted task id, or -1 if the task was not deleted
     */
    CompletableFuture<Integer> deleteTask(int deletedBy, int taskId);

    /**
     * Get all task attributes of a task
     * @param taskId The id of the task
     * @return A list of task attributes
     */
    CompletableFuture<List<TaskAttributeEntity>> getTaskAttributesOfTask(int taskId);

    /**
     * Get a task attribute by id
     * @param taskAttributeId The id of the task attribute
     * @return The task attribute
     */
    CompletableFuture<TaskAttributeEntity> getTaskAttributeById(int taskAttributeId);

    /**
     * Get the task attributes of a task by its TaskTypeAttributeId. For example, if the TaskTypeAttribute can take a list of values, this will return the values for the task
     * @param taskId The id of the task
     * @param attributeId The type of task attributes to grab
     * @return A list of task attributes
     */
    CompletableFuture<List<TaskAttributeEntity>> getTaskAttributesByAttributeId(int taskId, int attributeId);

    /**
     * Insert a task attribute
     * @param createdBy The id of the user who created the task attribute
     * @param taskId The id of the task
     * @param attributeId The id of the attribute type
     * @param valueJson The value of the task attribute
     * @return The inserted task attribute, or null if the task attribute was not inserted
     */
    CompletableFuture<TaskAttributeEntity> insertTaskAttribute(int createdBy, int taskId, int attributeId, String valueJson);

    /**
     * Update a task attribute
     * @param modifiedBy The id of the user who updated the task attribute
     * @param taskAttributeId The id of the task attribute to update
     * @param valueJson The new value of the task attribute
     * @return The updated task attribute, or null if the task attribute was not updated
     */
    CompletableFuture<TaskAttributeEntity> updateTaskAttribute(int modifiedBy, int taskAttributeId, String valueJson);

    /**
     * Delete a task attribute
     * @param deletedBy The id of the user who deleted the task attribute
     * @param taskAttributeId The id of the task attribute to delete
     * @return The deleted task attribute id, or -1 if the task attribute was not deleted
     */
    CompletableFuture<Integer> deleteTaskAttribute(int deletedBy, int taskAttributeId);

}
