package com.njdaeger.taskmanager.servicelibrary.services.implementations;

import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.taskmanager.dataaccess.repositories.ITaskRepository;
import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.Attribute;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.models.Task;
import com.njdaeger.taskmanager.servicelibrary.models.TaskAttribute;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.models.User;
import com.njdaeger.taskmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.IProjectService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskTypeService;
import com.njdaeger.taskmanager.servicelibrary.services.IUserService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class TaskService implements ITaskService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final ICacheService cacheService;
    private final IProjectService projectService;
    private final ITaskTypeService taskTypeService;
    private final IAttributeService attributeService;
    private final IPluginLogger logger;

    public TaskService(IServiceTransaction transaction, IUserService userService, IProjectService projectService, ITaskTypeService taskTypeService, IAttributeService attributeService, ICacheService cacheService, IPluginLogger logger) {
        this.transaction = transaction;
        this.userService = userService;
        this.cacheService = cacheService;
        this.projectService = projectService;
        this.taskTypeService = taskTypeService;
        this.attributeService = attributeService;
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
        cacheService.getTaskCache().clear();
    }

    @Override
    public CompletableFuture<Void> initializeCache() {
        transaction.use();

        return transaction.getUnitOfWork().repo(ITaskRepository.class).getTasks().thenApply(tasks -> {
            tasks.forEach(task -> {//none of these shoudl call to the unit of work, they should call to their services
                var taskAttributes = await(getTaskAttributes(task.getId())).getOrThrow();
                var taskType = await(taskTypeService.getTaskType(task.getTypeId())).getOrThrow();
                var project = await(projectService.getProjectById(task.getProjectId())).getOrThrow();
                var parentTask = task.getParentId() == 0 ? null : await(getTaskById(task.getParentId())).getOrThrow();
                //TODO: ensure parent isnt cyclic

                cacheService.getTaskCache().put(task.getId(), new Task(task.getId(), taskType, parentTask, project, task.getTaskKey(), taskAttributes));
            });
            return Result.good(List.copyOf(cacheService.getTaskCache().values()));
        }).whenComplete(finishTransaction()).thenAccept(r -> {});
    }

    @Override
    public CompletableFuture<Result<List<Task>>> getTasks() {
        transaction.use();

        return Util.<Result<List<Task>>>async(() -> {
            var cached = List.copyOf(cacheService.getTaskCache().values());
            if (!cached.isEmpty()) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).getTasks().thenApply(tasks -> {
                if (tasks.isEmpty()) return Result.bad("No tasks found.");
                tasks.forEach(task -> {
                    var taskAttributes = await(getTaskAttributes(task.getId())).getOrThrow();
                    var taskType = await(taskTypeService.getTaskType(task.getTypeId())).getOrThrow();
                    var project = await(projectService.getProjectById(task.getProjectId())).getOrThrow();
                    var parentTask = task.getParentId() == 0 ? null : await(getTaskById(task.getParentId())).getOrThrow();

                    cacheService.getTaskCache().put(task.getId(), new Task(task.getId(), taskType, parentTask, project, task.getTaskKey(), taskAttributes));
                });
                return Result.good(List.copyOf(cacheService.getTaskCache().values()));
            }));
        }).whenComplete(finishTransaction());

    }

    @Override
    public CompletableFuture<Result<Task>> getTaskById(int taskId) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var cached = cacheService.getTaskCache().get(taskId);
            if (cached != null) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).getTaskById(taskId).thenApply(task -> {
                if (task == null) return Result.bad("No task found with id " + taskId);
                var taskAttributes = await(getTaskAttributes(task.getId())).getOrThrow();
                var taskType = await(taskTypeService.getTaskType(task.getTypeId())).getOrThrow();
                var project = await(projectService.getProjectById(task.getProjectId())).getOrThrow();
                var parentTask = task.getParentId() == 0 ? null : await(getTaskById(task.getParentId())).getOrThrow();

                cacheService.getTaskCache().put(task.getId(), new Task(task.getId(), taskType, parentTask, project, task.getTaskKey(), taskAttributes));
                return Result.good(cacheService.getTaskCache().get(task.getId()));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> getTaskByTaskKey(Project project, TaskType taskType, int taskKey) {
        transaction.use();

        return Util.async(() -> {
            var cached = cacheService.getTaskCache().values().stream().filter(task -> task.getProject().equals(project) && task.getTaskType().equals(taskType) && task.getTaskKey() == taskKey).findFirst();
            return cached.map(Result::good).orElseGet(() -> await(transaction.getUnitOfWork().repo(ITaskRepository.class).getTaskByTaskKey(project.getProjectId(), taskType.getTaskTypeId(), taskKey).thenApply(task -> {
                if (task == null)
                    return Result.bad("No task found with key " + taskKey + " for project " + project.getProjectName() + " and task type " + taskType.getTaskTypeName());
                var taskAttributes = await(getTaskAttributes(task.getId())).getOrThrow();
                var parentTask = task.getParentId() == 0 ? null : await(getTaskById(task.getParentId())).getOrThrow();

                cacheService.getTaskCache().put(task.getId(), new Task(task.getId(), taskType, parentTask, project, task.getTaskKey(), taskAttributes));
                return Result.good(cacheService.getTaskCache().get(task.getId()));
            })));

        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<List<Task>>> getTasksOfProject(Project project) {
        transaction.use();

        return Util.<Result<List<Task>>>async(() -> {
            var cached = cacheService.getTaskCache().values().stream().filter(task -> task.getProject().equals(project)).toList();
            if (!cached.isEmpty()) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).getTasksOfProject(project.getProjectId()).thenApply(tasks -> {
                if (tasks.isEmpty()) return Result.bad("No tasks found for project " + project.getProjectName());
                tasks.forEach(task -> {
                    var taskAttributes = await(getTaskAttributes(task.getId())).getOrThrow();
                    var taskType = await(taskTypeService.getTaskType(task.getTypeId())).getOrThrow();
                    var parentTask = task.getParentId() == 0 ? null : await(getTaskById(task.getParentId())).getOrThrow();

                    cacheService.getTaskCache().put(task.getId(), new Task(task.getId(), taskType, parentTask, project, task.getTaskKey(), taskAttributes));
                });
                return Result.good(cacheService.getTaskCache().values().stream().filter(task -> task.getProject().equals(project)).toList());
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<List<Task>>> getTasksOfProjectAndType(Project project, TaskType taskType) {
        transaction.use();

        return Util.<Result<List<Task>>>async(() -> {
            var cached = cacheService.getTaskCache().values().stream().filter(task -> task.getProject().equals(project) && task.getTaskType().equals(taskType)).toList();
            if (!cached.isEmpty()) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).getTasksOfProjectAndType(project.getProjectId(), taskType.getTaskTypeId()).thenApply(tasks -> {
                if (tasks.isEmpty()) return Result.bad("No tasks found for project " + project.getProjectName() + " and task type " + taskType.getTaskTypeName());
                tasks.forEach(task -> {
                    var taskAttributes = await(getTaskAttributes(task.getId())).getOrThrow();
                    var parentTask = task.getParentId() == 0 ? null : await(getTaskById(task.getParentId())).getOrThrow();

                    cacheService.getTaskCache().put(task.getId(), new Task(task.getId(), taskType, parentTask, project, task.getTaskKey(), taskAttributes));
                });
                return Result.good(cacheService.getTaskCache().values().stream().filter(task -> task.getProject().equals(project) && task.getTaskType().equals(taskType)).toList());
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> createTask(UUID createdBy, Project project, TaskType taskType) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + createdBy);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).insertTask(userId, taskType.getTaskTypeId(), null, project.getProjectId()).thenApply(t -> {
                if (t == null) return Result.bad("Failed to create task.");

                var createdTask = new Task(t.getId(), taskType, null, project, t.getTaskKey(), List.of());

                cacheService.getTaskCache().put(createdTask.getTaskId(), createdTask);

                return Result.good(createdTask);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> createTask(UUID createdBy, Project project, TaskType taskType, List<Pair<Attribute, String>> taskAttribute) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + createdBy);

            var taskRepo = transaction.getUnitOfWork().repo(ITaskRepository.class);

            return await(taskRepo.insertTask(userId, taskType.getTaskTypeId(), null, project.getProjectId()).thenApply(t -> {
                if (t == null) return Result.bad("Failed to create task.");

                var createdTaskAttributes = new ArrayList<TaskAttribute>();

                for (var attribute : taskAttribute) {
                    var attr = await(taskRepo.insertTaskAttribute(userId, t.getId(), attribute.getFirst().getId(), attribute.getSecond()));
                    if (attr == null) return Result.bad("Failed to create task attribute.");
                    createdTaskAttributes.add(new TaskAttribute(attr.getId(), attribute.getFirst(), attribute.getSecond()));
                }

                var createdTask = new Task(t.getId(), taskType, null, project, t.getTaskKey(), createdTaskAttributes);

                cacheService.getTaskCache().put(t.getId(), createdTask);

                return Result.good(createdTask);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> createTask(UUID createdBy, Project project, TaskType taskType, Task parentTask) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + createdBy);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).insertTask(userId, taskType.getTaskTypeId(), parentTask.getTaskId(), project.getProjectId()).thenApply(t -> {
                if (t == null) return Result.bad("Failed to create task.");

                var createdTask = new Task(t.getId(), taskType, parentTask, project, t.getTaskKey(), List.of());

                cacheService.getTaskCache().put(t.getId(), createdTask);

                return Result.good(createdTask);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> createTask(UUID createdBy, Project project, TaskType taskType, Task parentTask, List<Pair<Attribute, String>> taskAttributes) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + createdBy);

            var taskRepo = transaction.getUnitOfWork().repo(ITaskRepository.class);

            return await(taskRepo.insertTask(userId, taskType.getTaskTypeId(), parentTask.getTaskId(), project.getProjectId()).thenApply(t -> {
                if (t == null) return Result.bad("Failed to create task.");

                var createdTaskAttributes = new ArrayList<TaskAttribute>();

                for (var attribute : taskAttributes) {
                    var attr = await(taskRepo.insertTaskAttribute(userId, t.getId(), attribute.getFirst().getId(), attribute.getSecond()));
                    if (attr == null) return Result.bad("Failed to create task attribute.");
                    createdTaskAttributes.add(new TaskAttribute(attr.getId(), attribute.getFirst(), attribute.getSecond()));
                }

                var createdTask = new Task(t.getId(), taskType, parentTask, project, t.getTaskKey(), createdTaskAttributes);

                cacheService.getTaskCache().put(t.getId(), createdTask);

                return Result.good(createdTask);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> updateTaskParent(UUID updatedBy, int taskId, Task parentTask) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + updatedBy);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).updateTask(userId, taskId, parentTask == null ? 0 : parentTask.getTaskId()).thenApply(t -> {
                if (t == null) return Result.bad("Failed to update task parent.");

                var task = cacheService.getTaskCache().get(taskId);
                if (task == null) return Result.bad("No task found with id " + taskId);

                var updatedTask = new Task(taskId, task.getTaskType(), parentTask, task.getProject(), task.getTaskKey(), task.getAttributes());

                cacheService.getTaskCache().put(taskId, updatedTask);

                return Result.good(updatedTask);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> deleteTask(UUID deletedBy, int taskId) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + deletedBy);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).deleteTask(userId, taskId).thenApply(t -> {
                if (t == -1) return Result.bad("Failed to delete task.");

                var task = cacheService.getTaskCache().remove(taskId);
                if (task == null) return Result.bad("No task found with id " + taskId);

                return Result.good(task);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<List<TaskAttribute>>> getTaskAttributes(int taskId) {
        transaction.use();

        return Util.<Result<List<TaskAttribute>>>async(() -> {
            var cached = cacheService.getTaskCache().get(taskId);
            if (cached != null) return Result.good(cached.getAttributes());

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).getTaskAttributesOfTask(taskId).thenApply(attrs -> {
                if (attrs.isEmpty()) return Result.bad("No task attributes found for task " + taskId);

                var taskAttributes = attrs.stream().map(attr -> {
                    var attributeModel = await(attributeService.getAttributeById(attr.getAttributeId())).getOrThrow();
                    return new TaskAttribute(attr.getId(), attributeModel, attr.getValue());
                }).toList();

                return Result.good(taskAttributes);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<TaskAttribute>> getTaskAttributeById(int taskAttributeId) {
        transaction.use();

        return Util.async(() -> {
            var cached = cacheService.getTaskCache().values().stream().map(Task::getAttributes).flatMap(List::stream).filter(attr -> attr.getTaskAttributeId() == taskAttributeId).findFirst();
            return cached.map(Result::good).orElseGet(() -> await(transaction.getUnitOfWork().repo(ITaskRepository.class).getTaskAttributeById(taskAttributeId).thenApply(attr -> {
                if (attr == null) return Result.bad("No task attribute found with id " + taskAttributeId);

                var attributeModel = await(attributeService.getAttributeById(attr.getAttributeId())).getOrThrow();
                var taskAttributeModel = new TaskAttribute(attr.getId(), attributeModel, attr.getValue());

                return Result.good(taskAttributeModel);
            })));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<List<TaskAttribute>>> getTaskAttributesByAttribute(int taskId, Attribute attribute) {
        transaction.use();

        return Util.<Result<List<TaskAttribute>>>async(() -> {
            var cached = cacheService.getTaskCache().get(taskId).getAttributes().stream().filter(attr -> attr.getAttribute().equals(attribute)).toList();
            if (!cached.isEmpty()) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(ITaskRepository.class).getTaskAttributesByAttributeId(taskId, attribute.getId()).thenApply(attrs -> {
                if (attrs.isEmpty()) return Result.bad("No task attributes found for task " + taskId + " and attribute " + attribute.getName());

                var taskAttributes = attrs.stream().map(attr -> {
                    var attributeModel = await(attributeService.getAttributeById(attr.getAttributeId())).getOrThrow();
                    return new TaskAttribute(attr.getId(), attributeModel, attr.getValue());
                }).toList();

                return Result.good(taskAttributes);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> addTaskAttributes(UUID createdBy, int taskId, List<Pair<Attribute, String>> taskAttributes) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var task = cacheService.getTaskCache().get(taskId);
            if (task == null) return Result.bad("No task found with id " + taskId);

            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + createdBy);

            var taskRepo = transaction.getUnitOfWork().repo(ITaskRepository.class);

            var currentTaskAttributes = await(getTaskAttributes(taskId)).getOr(ArrayList::new, new ArrayList<TaskAttribute>());

            var newTaskAttributes = new ArrayList<TaskAttribute>();

            for (var attribute : taskAttributes) {
                var attr = await(taskRepo.insertTaskAttribute(userId, taskId, attribute.getFirst().getId(), attribute.getSecond()));
                if (attr == null) return Result.bad("Failed to create task attribute.");
                newTaskAttributes.add(new TaskAttribute(attr.getId(), attribute.getFirst(), attribute.getSecond()));
            }

            currentTaskAttributes.addAll(newTaskAttributes);

            var updatedTask = new Task(taskId, task.getTaskType(), task.getParent(), task.getProject(), task.getTaskKey(), currentTaskAttributes);
            return Result.good(updatedTask);

        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> removeTaskAttributes(UUID removedBy, int taskId, int... taskAttributeIds) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var task = cacheService.getTaskCache().get(taskId);
            if (task == null) return Result.bad("No task found with id " + taskId);

            var userId = await(userService.getUserByUuid(removedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + removedBy);

            var taskRepo = transaction.getUnitOfWork().repo(ITaskRepository.class);

            var currentTaskAttributes = await(getTaskAttributes(taskId)).getOr(ArrayList::new, new ArrayList<TaskAttribute>());

            for (var attributeId : taskAttributeIds) {
                var attr = await(taskRepo.deleteTaskAttribute(userId, attributeId));
                if (attr == -1) return Result.bad("Failed to delete task attribute.");
                currentTaskAttributes.removeIf(a -> a.getTaskAttributeId() == attributeId);
            }

            var updatedTask = new Task(taskId, task.getTaskType(), task.getParent(), task.getProject(), task.getTaskKey(), currentTaskAttributes);
            return Result.good(updatedTask);
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Task>> updateTaskAttribute(UUID updatedBy, int taskId, int taskAttributeId, String newValue) {
        transaction.use();

        return Util.<Result<Task>>async(() -> {
            var task = cacheService.getTaskCache().get(taskId);
            if (task == null) return Result.bad("No task found with id " + taskId);

            var userId = await(userService.getUserByUuid(updatedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("No user found with uuid " + updatedBy);

            var taskRepo = transaction.getUnitOfWork().repo(ITaskRepository.class);

            var currentTaskAttributes = await(getTaskAttributes(taskId)).getOr(ArrayList::new, new ArrayList<TaskAttribute>());

            var attr = await(taskRepo.updateTaskAttribute(userId, taskAttributeId, newValue));
            if (attr == null) return Result.bad("Failed to update task attribute.");
            currentTaskAttributes.removeIf(a -> a.getTaskAttributeId() == taskAttributeId);
            currentTaskAttributes.add(new TaskAttribute(attr.getId(), await(attributeService.getAttributeById(attr.getAttributeId())).getOrThrow(), newValue));

            var updatedTask = new Task(taskId, task.getTaskType(), task.getParent(), task.getProject(), task.getTaskKey(), currentTaskAttributes);
            return Result.good(updatedTask);
        }).whenComplete(finishTransaction());
    }

}
