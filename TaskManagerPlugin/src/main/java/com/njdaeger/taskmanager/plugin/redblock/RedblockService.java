package com.njdaeger.taskmanager.plugin.redblock;

import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.taskmanager.servicelibrary.services.IProjectService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskTypeService;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.njdaeger.taskmanager.dataaccess.Util.async;
import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class RedblockService implements IRedblockService {

    private final TaskType taskType;
    private final IPluginLogger pluginLogger;
    private final ITaskTypeService taskTypeService;
    private final ITaskService taskService;
    private final IAttributeService attributeService;
    private final IProjectService projectService;

    public RedblockService(IPluginLogger pluginLogger, IServiceProvider serviceProvider) {
        this.pluginLogger = pluginLogger;
        this.projectService = serviceProvider.getService(IProjectService.class);
        this.taskTypeService = serviceProvider.getService(ITaskTypeService.class);
        this.attributeService = serviceProvider.getService(IAttributeService.class);
        this.taskService = serviceProvider.getService(ITaskService.class);

        //add redblock task type
        this.taskType = getTaskType(RedblockConstants.REDBLOCK_TASK_TYPE_NAME, RedblockConstants.REDBLOCK_TASK_TYPE_PREFIX);
        if (this.taskType == null) {
            pluginLogger.exception("Failed to create redblock task type.");
            return;
        }

        try {
            ensureAttributeExists(RedblockConstants.STATUS_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.DESCRIPTION_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.COMPLETED_BY_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.COMPLETED_ON_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.ASSIGNED_TO_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.ASSIGNED_ON_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.CREATED_BY_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.CREATED_ON_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.APPROVED_BY_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.APPROVED_ON_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.LOCATION_ATTRIBUTE);
            ensureAttributeExists(RedblockConstants.MINIMUM_RANK);
        } catch (Exception e) {
            pluginLogger.exception(e, "Failed to create redblock attributes.");
        }
    }

    private void ensureAttributeExists(String name) throws Exception {
        var attribute = await(attributeService.getAttribute(name, taskType));
        if (!attribute.successful()) {
            var attributeCreateResult = await(attributeService.createAttribute(Util.SYSTEM_UUID, taskType, name));
            if (!attributeCreateResult.successful()) {
                pluginLogger.exception("Failed to create attribute: " + name);
                return;
            }
            attributeCreateResult.getOrThrow("Failed to find or create attribute: " + name);
        }
        attribute.getOrThrow("Failed to find or create attribute: " + name);
    }

    private TaskType getTaskType(String name, String prefix) {
        var taskType = await(taskTypeService.getTaskType(name));
        if (!taskType.successful()) {
            var taskTypeCreateResult = await(taskTypeService.createTaskType(Util.SYSTEM_UUID, name, prefix));
            if (!taskTypeCreateResult.successful()) {
                return null;
            }
            return taskTypeCreateResult.getOrThrow();
        }
        return taskType.getOrThrow();
    }

    @Override
    public TaskType getRedblockTaskType() {
        return taskType;
    }

    @Override
    public CompletableFuture<Result<RedblockTask>> createRedblock(Project project, UUID createdBy, Location location, UUID assignedTo, String minRank, String description) {
        return async(() -> {
            var locationAttr = await(attributeService.getAttribute(RedblockConstants.LOCATION_ATTRIBUTE, taskType));
            var assignedToAttr = await(attributeService.getAttribute(RedblockConstants.ASSIGNED_TO_ATTRIBUTE, taskType));
            var minRankAttr = await(attributeService.getAttribute(RedblockConstants.MINIMUM_RANK, taskType));
            var descriptionAttr = await(attributeService.getAttribute(RedblockConstants.DESCRIPTION_ATTRIBUTE, taskType));
            var statusAttr = await(attributeService.getAttribute(RedblockConstants.STATUS_ATTRIBUTE, taskType));

            if (!locationAttr.successful() || !assignedToAttr.successful() || !minRankAttr.successful() || !descriptionAttr.successful() || !statusAttr.successful()) {
                return Result.bad("Failed to find required attributes for redblock creation.");
            }
            return await(taskService.createTask(createdBy, project, taskType, new ArrayList<>() {{
                add(Pair.of(locationAttr.getOrThrow(), location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()));
                if (assignedTo != null) add(Pair.of(assignedToAttr.getOrThrow(), assignedTo.toString()));
                if (minRank != null) add(Pair.of(minRankAttr.getOrThrow(), minRank));
                add(Pair.of(descriptionAttr.getOrThrow(), description));
                add(Pair.of(statusAttr.getOrThrow(), RedblockConstants.REDBLOCK_STATUS_INCOMPLETE));
            }})).tryMap(RedblockTask::new);
        });
    }

    @Override
    public CompletableFuture<Result<RedblockTask>> deleteRedblock(Project project, UUID deletedBy, int taskKey) {
        //this should only mark the plot status as deleted
        return async(() -> {
            var task = await(taskService.getTaskByTaskKey(project, taskType, taskKey)).getOrThrow();

            var statusAttr = task.getAttribute(RedblockConstants.STATUS_ATTRIBUTE);
            if (statusAttr == null) return Result.bad("Failed to find status attribute for task: " + project.getProjectPrefix() + "-" + taskType.getTaskTypePrefix() + "-" + taskKey);

            return await(taskService.updateTaskAttribute(deletedBy, task.getTaskId(), statusAttr.getTaskAttributeId(), RedblockConstants.REDBLOCK_STATUS_DELETED)).tryMap(RedblockTask::new);
        });
    }

    @Override
    public CompletableFuture<Result<RedblockTask>> updateRedblock(Project project, UUID modifiedBy, int taskKey, UUID assignedTo, String minRank, String description) {
        return async(() -> {
            var task = await(taskService.getTaskByTaskKey(project, taskType, taskKey)).getOrThrow();

            //assigned to attribute updates
            var assignedToAttr = task.getAttribute(RedblockConstants.ASSIGNED_TO_ATTRIBUTE);
            if (assignedTo == null && assignedToAttr != null) await(taskService.removeTaskAttributes(modifiedBy, task.getTaskId(), assignedToAttr.getTaskAttributeId()));
            else if (assignedTo != null && assignedToAttr == null) await(taskService.addTaskAttributes(modifiedBy, task.getTaskId(), new ArrayList<>() {{
                add(Pair.of(await(attributeService.getAttribute(RedblockConstants.ASSIGNED_TO_ATTRIBUTE, taskType)).getOrThrow(), assignedTo.toString()));
            }}));
            else if (assignedTo != null) await(taskService.updateTaskAttribute(modifiedBy, task.getTaskId(), assignedToAttr.getTaskAttributeId(), assignedTo.toString()));

            //minRank attribute updates
            var minRankAttr = task.getAttribute(RedblockConstants.MINIMUM_RANK);
            if (minRank == null && minRankAttr != null) await(taskService.removeTaskAttributes(modifiedBy, task.getTaskId(), minRankAttr.getTaskAttributeId()));
            else if (minRank != null && minRankAttr == null) await(taskService.addTaskAttributes(modifiedBy, task.getTaskId(), new ArrayList<>() {{
                add(Pair.of(await(attributeService.getAttribute(RedblockConstants.MINIMUM_RANK, taskType)).getOrThrow(), minRank));
            }}));
            else if (minRank != null) await(taskService.updateTaskAttribute(modifiedBy, task.getTaskId(), minRankAttr.getTaskAttributeId(), minRank));

            //description attribute updates
            var descriptionAttr = task.getAttribute(RedblockConstants.DESCRIPTION_ATTRIBUTE);
            if (description == null && descriptionAttr != null) await(taskService.removeTaskAttributes(modifiedBy, task.getTaskId(), descriptionAttr.getTaskAttributeId()));
            else if (description != null && descriptionAttr == null) await(taskService.addTaskAttributes(modifiedBy, task.getTaskId(), new ArrayList<>() {{
                add(Pair.of(await(attributeService.getAttribute(RedblockConstants.DESCRIPTION_ATTRIBUTE, taskType)).getOrThrow(), description));
            }}));
            else if (description != null) await(taskService.updateTaskAttribute(modifiedBy, task.getTaskId(), descriptionAttr.getTaskAttributeId(), description));

            return await(taskService.getTaskById(task.getTaskId())).tryMap(RedblockTask::new);
        });
    }

    @Override
    public CompletableFuture<Result<RedblockTask>> approveRedblock(Project project, UUID approvedBy, int taskKey) {
        return async(() -> {
            var task = await(taskService.getTaskByTaskKey(project, taskType, taskKey)).getOrThrow();

            var statusAttr = task.getAttribute(RedblockConstants.STATUS_ATTRIBUTE);
            if (statusAttr == null) return Result.bad("Failed to find status attribute for task: " + project.getProjectPrefix() + "-" + taskType.getTaskTypePrefix() + "-" + taskKey);

            //add approvedBy and approvedOn attributes
            var approvedByAttr = await(attributeService.getAttribute(RedblockConstants.APPROVED_BY_ATTRIBUTE, taskType)).getOrThrow("Unable to find ApprovedBy attribute.");
            var approvedOnAttr = await(attributeService.getAttribute(RedblockConstants.APPROVED_ON_ATTRIBUTE, taskType)).getOrThrow("Unable to find ApprovedOn attribute.");

            await(taskService.addTaskAttributes(approvedBy, task.getTaskId(), new ArrayList<>() {{
                add(Pair.of(approvedByAttr, approvedBy.toString()));
                add(Pair.of(approvedOnAttr, String.valueOf(System.currentTimeMillis())));
            }}));

            return await(taskService.updateTaskAttribute(approvedBy, task.getTaskId(), statusAttr.getTaskAttributeId(), RedblockConstants.REDBLOCK_STATUS_APPROVED)).tryMap(RedblockTask::new);
        });
    }

    @Override
    public CompletableFuture<Result<RedblockTask>> denyRedblock(Project project, UUID deniedBy, int taskKey) {
        return async(() -> {
            var task = await(taskService.getTaskByTaskKey(project, taskType, taskKey)).getOrThrow();

            var statusAttr = task.getAttribute(RedblockConstants.STATUS_ATTRIBUTE);
            if (statusAttr == null) return Result.bad("Failed to find status attribute for task: " + project.getProjectPrefix() + "-" + taskType.getTaskTypePrefix() + "-" + taskKey);

            //remove completedBy and completedOn attributes
            var completedByAttr = task.getAttribute(RedblockConstants.COMPLETED_BY_ATTRIBUTE);
            if (completedByAttr == null) return Result.bad("Failed to find completed by attribute for task: " + project.getProjectPrefix() + "-" + taskType.getTaskTypePrefix() + "-" + taskKey);

            var completedOnAttr = task.getAttribute(RedblockConstants.COMPLETED_ON_ATTRIBUTE);
            if (completedOnAttr == null) return Result.bad("Failed to find completed on attribute for task: " + project.getProjectPrefix() + "-" + taskType.getTaskTypePrefix() + "-" + taskKey);

            await(taskService.removeTaskAttributes(deniedBy, task.getTaskId(), completedByAttr.getTaskAttributeId(), completedOnAttr.getTaskAttributeId()));

            return await(taskService.updateTaskAttribute(deniedBy, task.getTaskId(), statusAttr.getTaskAttributeId(), RedblockConstants.REDBLOCK_STATUS_INCOMPLETE)).tryMap(RedblockTask::new);
        });
    }

    @Override
    public CompletableFuture<Result<RedblockTask>> completeRedblock(Project project, UUID completedBy, int taskKey) {
        return Util.async(() -> {
            var task = await(taskService.getTaskByTaskKey(project, taskType, taskKey)).getOrThrow();

            var statusAttr = task.getAttribute(RedblockConstants.STATUS_ATTRIBUTE);
            if (statusAttr == null) return Result.bad("Failed to find status attribute for task: " + project.getProjectPrefix() + "-" + taskType.getTaskTypePrefix() + "-" + taskKey);

            var completedByAttr = task.getAttribute(RedblockConstants.COMPLETED_BY_ATTRIBUTE);
            if (completedByAttr == null) return Result.bad("Failed to find completed by attribute for task: " + project.getProjectPrefix() + "-" + taskType.getTaskTypePrefix() + "-" + taskKey);

            var completedOnAttr = task.getAttribute(RedblockConstants.COMPLETED_ON_ATTRIBUTE);
            if (completedOnAttr == null) return Result.bad("Failed to find completed on attribute for task: " + project.getProjectPrefix() + "-" + taskType.getTaskTypePrefix() + "-" + taskKey);

            var result = await(taskService.updateTaskAttribute(completedBy, task.getTaskId(), statusAttr.getTaskAttributeId(), RedblockConstants.REDBLOCK_STATUS_PENDING));
            if (!result.successful()) return result.tryMap(RedblockTask::new);

            result = await(taskService.updateTaskAttribute(completedBy, task.getTaskId(), completedByAttr.getTaskAttributeId(), completedBy.toString()));
            if (!result.successful()) return result.tryMap(RedblockTask::new);

            return await(taskService.updateTaskAttribute(completedBy, task.getTaskId(), completedOnAttr.getTaskAttributeId(), System.currentTimeMillis() + "")).tryMap(RedblockTask::new);
        });
    }

    @Override
    public RedblockTask getNearestRedblock(List<RedblockTask> redblockTasks, Location location, int searchRadius) {
        if (redblockTasks.isEmpty()) return null;
        RedblockTask nearestRedblock = null;
        double nearestDistance = Double.MAX_VALUE;
        for (RedblockTask redblock : redblockTasks) {
            if (!redblock.getLocation().getWorld().getUID().equals(location.getWorld().getUID())) continue;
            double distance = redblock.getLocation().distance(location);
            if (distance < nearestDistance && (searchRadius == -1 || distance <= searchRadius)) {
                nearestRedblock = redblock;
                nearestDistance = distance;
            }
        }
        return nearestRedblock;
    }
}
