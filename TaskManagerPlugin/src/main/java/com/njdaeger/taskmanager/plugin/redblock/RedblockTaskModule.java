package com.njdaeger.taskmanager.plugin.redblock;

import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.taskmanager.servicelibrary.services.IProjectService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskTypeService;

import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class RedblockTaskModule {

    private final TaskType taskType;
    private final IPluginLogger pluginLogger;
    private final ITaskTypeService taskTypeService;
    private final IAttributeService attributeService;
    private final IProjectService projectService;

    public RedblockTaskModule(IPluginLogger pluginLogger, IServiceProvider serviceProvider) {
        this.pluginLogger = pluginLogger;
        this.projectService = serviceProvider.getService(IProjectService.class);
        this.taskTypeService = serviceProvider.getService(ITaskTypeService.class);
        this.attributeService = serviceProvider.getService(IAttributeService.class);

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

}
