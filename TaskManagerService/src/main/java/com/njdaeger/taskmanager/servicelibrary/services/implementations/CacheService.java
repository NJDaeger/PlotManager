package com.njdaeger.taskmanager.servicelibrary.services.implementations;

import com.njdaeger.taskmanager.servicelibrary.PlotBuilder;
import com.njdaeger.taskmanager.servicelibrary.models.Attribute;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.models.Task;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.models.User;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CacheService implements ICacheService {

    private final Map<Integer, Attribute> attributeCache = new ConcurrentHashMap<>();
    private final Map<UUID, User> userCache = new ConcurrentHashMap<>();
    private final Map<String, TaskType> taskTypeCache = new ConcurrentHashMap<>();
    private final Map<Integer, Task> taskCache = new ConcurrentHashMap<>();
    private final Map<String, Project> projectCache = new ConcurrentHashMap<>();

    @Override
    public Map<Integer, Attribute> getAttributeCache() {
        return attributeCache;
    }

    @Override
    public Map<UUID, User> getUserCache() {
        return userCache;
    }

    @Override
    public Map<String, TaskType> getTaskTypeCache() {
        return taskTypeCache;
    }

    @Override
    public Map<Integer, Task> getTaskCache() {
        return taskCache;
    }

    @Override
    public Map<String, Project> getProjectCache() {
        return projectCache;
    }
}
