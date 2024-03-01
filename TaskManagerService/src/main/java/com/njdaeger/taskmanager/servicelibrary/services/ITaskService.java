package com.njdaeger.taskmanager.servicelibrary.services;

import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.Attribute;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.models.Task;
import com.njdaeger.taskmanager.servicelibrary.models.TaskAttribute;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.transactional.ITransactionalService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ITaskService extends ITransactionalService, ICached {

    /**
     * Gets all tasks.
     * @return A future containing the result of the task retrieval.
     */
    CompletableFuture<Result<List<Task>>> getTasks();

    /**
     * Gets a task by its id.
     * @param taskId The id of the task to get.
     * @return A future containing the result of the task retrieval.
     */
    CompletableFuture<Result<Task>> getTaskById(int taskId);

    /**
     * Gets all tasks in the given project.
     * @param project The project to get the tasks from.
     * @return A future containing the result of the task retrieval.
     */
    CompletableFuture<Result<List<Task>>> getTasksOfProject(Project project);

    /**
     * Gets all tasks in the given project of the given type.
     * @param project The project to get the tasks from.
     * @param taskType The type of task to get.
     * @return A future containing the result of the task retrieval.
     */
    CompletableFuture<Result<List<Task>>> getTasksOfProjectAndType(Project project, TaskType taskType);

    /**
     * Creates an orphaned task with the given task type.
     * @param createdBy The id of the user who created the task.
     * @param project The project to create the task in.
     * @param taskType The type of task to create.
     * @return A future containing the result of the task creation.
     */
    CompletableFuture<Result<Task>> createTask(UUID createdBy, Project project, TaskType taskType);

    /**
     * Creates an orphaned task with the given task type and attributes.
     * @param createdBy The id of the user who created the task.
     * @param project The project to create the task in.
     * @param taskType The type of task to create.
     * @param taskAttributes The attributes to give the task.
     * @return A future containing the result of the task creation.
     */
    CompletableFuture<Result<Task>> createTask(UUID createdBy, Project project, TaskType taskType, List<Pair<Attribute, String>> taskAttributes);

    /**
     * Creates an orphaned task with the given task type and parent task.
     * @param createdBy The id of the user who created the task.
     * @param project The project to create the task in.
     * @param taskType The type of task to create.
     * @param parentTask The parent task of the task to create.
     * @return A future containing the result of the task creation.
     */
    CompletableFuture<Result<Task>> createTask(UUID createdBy, Project project, TaskType taskType, Task parentTask);

    /**
     * Creates an orphaned task with the given task type, parent task, and attributes.
     * @param createdBy The id of the user who created the task.
     * @param project The project to create the task in.
     * @param taskType The type of task to create.
     * @param parentTask The parent task of the task to create.
     * @param taskAttributes The attributes to give the task.
     * @return A future containing the result of the task creation.
     */
    CompletableFuture<Result<Task>> createTask(UUID createdBy, Project project, TaskType taskType, Task parentTask, List<Pair<Attribute, String>> taskAttributes);

    /**
     * Updates the parent of the given task.
     * @param updatedBy The id of the user who updated the task.
     * @param taskId The id of the task to update.
     * @param parentTask The new parent of the task, or null if the task should be orphaned.
     * @return A future containing the result of the task update.
     */
    CompletableFuture<Result<Task>> updateTaskParent(UUID updatedBy, int taskId, Task parentTask);

    /**
     * Deletes the given task.
     * @param deletedBy The id of the user who deleted the task.
     * @param taskId The id of the task to delete.
     * @return A future containing the result of the task deletion.
     */
    CompletableFuture<Result<Task>> deleteTask(UUID deletedBy, int taskId);

    CompletableFuture<Result<List<TaskAttribute>>> getTaskAttributes(int taskId);

    CompletableFuture<Result<TaskAttribute>> getTaskAttributeById(int taskAttributeId);

    CompletableFuture<Result<List<TaskAttribute>>> getTaskAttributesByAttribute(int taskId, Attribute attribute);

    /**
     * Adds the given attributes to the task.
     * @param createdBy The id of the user who added the attributes.
     * @param taskId The id of the task to update.
     * @param taskAttributes The attributes to add to the task.
     * @return A future containing the result of the task update.
     */
    CompletableFuture<Result<Task>> addTaskAttributes(UUID createdBy, int taskId, List<Pair<Attribute, String>> taskAttributes);

    /**
     * Removes the given attributes from the task.
     * @param removedBy The id of the user who removed the attributes.
     * @param taskId The id of the task to update.
     * @param taskAttributeIds The attributes to remove from the task.
     * @return A future containing the result of the task update.
     */
    CompletableFuture<Result<Task>> removeTaskAttributes(UUID removedBy, int taskId, int... taskAttributeIds);



}
