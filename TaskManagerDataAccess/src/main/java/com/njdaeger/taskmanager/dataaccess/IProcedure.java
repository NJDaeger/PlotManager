package com.njdaeger.taskmanager.dataaccess;

import com.njdaeger.pdk.utils.Pair;

import java.util.Map;
import java.util.UUID;

public interface IProcedure {

    //region Schema Procedures

    Pair<String, Map<String, Object>> selectSchemaVersion();

    Pair<String, Map<String, Object>> updateSchemaVersion(int version);

    Pair<String, Map<String, Object>> insertSchemaVersion(int version);

    //endregion

    //region Attribute Procedures

    Pair<String, Map<String, Object>> selectAttributes();

    Pair<String, Map<String, Object>> selectAttributesByTaskType(int taskTypeId);

    Pair<String, Map<String, Object>> selectAttributeByName(String attributeName, int taskTypeId);

    Pair<String, Map<String, Object>> selectAttributeById(int attributeId);

    Pair<String, Map<String, Object>> insertAttribute(int createdBy, int taskTypeId, String name);

    Pair<String, Map<String, Object>> updateAttribute(int modifiedBy, int attributeId, Integer taskTypeId, String name);

    //endregion

    //region User Procedures

    Pair<String, Map<String, Object>> insertSystemUser();

    Pair<String, Map<String, Object>> selectUsers();

    Pair<String, Map<String, Object>> selectUserById(int userId);

    Pair<String, Map<String, Object>> selectUsersByUsername(String username);

    Pair<String, Map<String, Object>> selectUserByUuid(UUID uuid);

    Pair<String, Map<String, Object>> insertUser(int createdBy, UUID uuid, String username);

    Pair<String, Map<String, Object>> updateUser(int modifiedBy, int userId, UUID uuid, String username);

    Pair<String, Map<String, Object>> deleteUser(int deletedBy, int userId);

    Pair<String, Map<String, Object>> restoreUser(int restoredBy, int userId);

    //endregion

    //region Project Procedures

    Pair<String, Map<String, Object>> selectProjects();

    Pair<String, Map<String, Object>> selectProjectById(int projectId);

    Pair<String, Map<String, Object>> selectProjectByName(String projectName);

    Pair<String, Map<String, Object>> selectProjectByPrefix(String projectPrefix);

    Pair<String, Map<String, Object>> insertProject(int createdBy, String projectName, String projectDescription, String projectPrefix);

    Pair<String, Map<String, Object>> updateProject(int modifiedBy, int projectId, String projectName, String projectDescription, String projectPrefix);

    Pair<String, Map<String, Object>> deleteProject(int deletedBy, int projectId);

    Pair<String, Map<String, Object>> restoreProject(int restoredBy, int projectId);

    //endregion

    //region Task Type Procedures

    Pair<String, Map<String, Object>> selectTaskTypes();

    Pair<String, Map<String, Object>> selectTaskTypeById(int taskTypeId);

    Pair<String, Map<String, Object>> selectTaskTypeByName(String taskTypeName);

    Pair<String, Map<String, Object>> selectTaskTypeByPrefix(String taskTypePrefix);

    Pair<String, Map<String, Object>> insertTaskType(int createdBy, String taskTypeName, String prefix);

    Pair<String, Map<String, Object>> updateTaskType(int modifiedBy, int taskTypeId, String taskTypeName, Integer taskTypeVersion, String prefix);

    Pair<String, Map<String, Object>> deleteTaskType(int deletedBy, int taskTypeId);

    Pair<String, Map<String, Object>> restoreTaskType(int restoredBy, int taskTypeId);

    //endregion

    //region Task Procedures

    Pair<String, Map<String, Object>> selectTasks();

    Pair<String, Map<String, Object>> selectTaskById(int taskId);

    Pair<String, Map<String, Object>> selectTasksByProject(int projectId);

    Pair<String, Map<String, Object>> selectTasksByProjectAndType(int projectId, int taskTypeId);

    Pair<String, Map<String, Object>> insertTask(int createdBy, int taskTypeId, int projectId, Integer parentId);

    Pair<String, Map<String, Object>> updateTask(int modifiedBy, int taskId, Integer parentId);

    Pair<String, Map<String, Object>> deleteTask(int deletedBy, int taskId);

    Pair<String, Map<String, Object>> restoreTask(int restoredBy, int taskId);

    //endregion

    //region Task Attribute Procedures

    Pair<String, Map<String, Object>> selectTaskAttributesForTask(int taskId);

    Pair<String, Map<String, Object>> selectTaskAttributesForTaskByAttributeId(int taskId, int attributeId);

    Pair<String, Map<String, Object>> selectTaskAttributeById(int taskAttributeId);

    Pair<String, Map<String, Object>> insertTaskAttribute(int createdBy, int taskId, int attributeId, String value);

    Pair<String, Map<String, Object>> updateTaskAttribute(int modifiedBy, int taskId, String value);

    Pair<String, Map<String, Object>> deleteTaskAttribute(int deletedBy, int taskAttributeId);

    Pair<String, Map<String, Object>> restoreTaskAttribute(int restoredBy, int taskAttributeId);

    //endregion
}
