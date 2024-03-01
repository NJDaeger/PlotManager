package com.njdaeger.taskmanager.servicelibrary.services;

import com.njdaeger.taskmanager.servicelibrary.models.Attribute;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.models.Task;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.models.User;

import java.util.Map;
import java.util.UUID;

public interface ICacheService {

    /**
     * Attribute name mapped to its attribute object
     * @return The attribute cache
     */
    Map<String, Attribute> getAttributeCache();

    /**
     * User UUID mapped to their user object
     * @return The user cache
     */
    Map<UUID, User> getUserCache();

    Map<String, TaskType> getTaskTypeCache();

    Map<Integer, Task> getTaskCache();

    Map<String, Project> getProjectCache();

}
