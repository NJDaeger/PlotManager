package com.njdaeger.taskmanager.dataaccess.databases.mysql;

import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.taskmanager.dataaccess.IProcedure;
import com.njdaeger.taskmanager.dataaccess.Util;

import java.util.Map;
import java.util.UUID;

public class MySqlProcedures implements IProcedure {

    public MySqlProcedures() {
    }

    @Override
    public Pair<String, Map<String, Object>> selectSchemaVersion() {
        return Pair.of("call TaskManager.Select_SchemaVersion()", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> updateSchemaVersion(int version) {
        return Pair.of("call TaskManager.Update_SchemaVersion(?)", Map.of("1", version));
    }

    @Override
    public Pair<String, Map<String, Object>> insertSchemaVersion(int version) {
        return Pair.of("call TaskManager.Insert_SchemaVersion(?)", Map.of("1", version));
    }


    @Override
    public Pair<String, Map<String, Object>> selectAttributes() {
        return Pair.of("call TaskManager.Select_Attributes()", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectAttributesByTaskType(int taskTypeId) {
        return Pair.of("call TaskManager.Select_AttributesByTaskType(?)", Map.of("1", taskTypeId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectAttributeByName(String attributeName, int taskTypeId) {
        return Pair.of("call TaskManager.Select_AttributeByName(?)", Map.of("1", attributeName));
    }

    @Override
    public Pair<String, Map<String, Object>> selectAttributeById(int attributeId) {
        return Pair.of("call TaskManager.Select_AttributeById(?)", Map.of("1", attributeId));
    }

    @Override
    public Pair<String, Map<String, Object>> insertAttribute(int createdBy, int taskTypeId, String name) {
        return Pair.of("call TaskManager.Insert_Attribute(?, ?)", Map.of(
                "1", createdBy,
                "2", name
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateAttribute(int modifiedBy, int attributeId, Integer taskTypeId, String name) {
        return Pair.of("call TaskManager.Update_Attribute(?, ?, ?)", Map.of(
                "1", modifiedBy,
                "2", attributeId,
                "3", taskTypeId,
                "4", name
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> insertSystemUser() {
        return Pair.of("call TaskManager.Insert_User(?, ?, ?)", Map.of(
                "1", 0,
                "2", Util.SYSTEM_UUID.toString(),
                "3", "#system"
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectUsers() {
        return Pair.of("call TaskManager.Select_Users()", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectUserById(int userId) {
        return Pair.of("call TaskManager.Select_UserById(?)", Map.of("1", userId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectUsersByUsername(String username) {
        return Pair.of("call TaskManager.Select_UsersByUsername(?)", Map.of("1", username));
    }

    @Override
    public Pair<String, Map<String, Object>> selectUserByUuid(UUID uuid) {
        return Pair.of("call TaskManager.Select_UserByUuid(?)", Map.of("1", uuid.toString()));
    }

    @Override
    public Pair<String, Map<String, Object>> insertUser(int createdBy, UUID uuid, String username) {
        return Pair.of("call TaskManager.Insert_User(?, ?, ?)", Map.of(
                "1", createdBy,
                "2", uuid.toString(),
                "3", username
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateUser(int modifiedBy, int userId, UUID uuid, String username) {
        return Pair.of("call TaskManager.Update_User(?, ?, ?, ?)", Map.of(
                "1", modifiedBy,
                "2", userId,
                "3", uuid.toString(),
                "4", username
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> deleteUser(int deletedBy, int userId) {
        return Pair.of("call TaskManager.Delete_User(?, ?)", Map.of(
                "1", deletedBy,
                "2", userId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> restoreUser(int restoredBy, int userId) {
        return Pair.of("call TaskManager.Restore_User(?, ?)", Map.of(
                "1", restoredBy,
                "2", userId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectProjects() {
        return Pair.of("call TaskManager.Select_Projects()", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectProjectById(int projectId) {
        return Pair.of("call TaskManager.Select_ProjectById(?)", Map.of("1", projectId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectProjectByName(String projectName) {
        return Pair.of("call TaskManager.Select_ProjectByName(?)", Map.of("1", projectName));
    }

    @Override
    public Pair<String, Map<String, Object>> selectProjectByPrefix(String projectPrefix) {
        return Pair.of("call TaskManager.Select_ProjectByPrefix(?)", Map.of("1", projectPrefix));
    }

    @Override
    public Pair<String, Map<String, Object>> insertProject(int createdBy, String projectName, String projectDescription, String projectPrefix) {
        return Pair.of("call TaskManager.Insert_Project(?, ?, ?, ?)", Map.of(
                "1", createdBy,
                "2", projectName,
                "3", projectDescription,
                "4", projectPrefix
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateProject(int modifiedBy, int projectId, String projectName, String projectDescription, String projectPrefix) {
        return Pair.of("call TaskManager.Update_Project(?, ?, ?, ?, ?)", Map.of(
                "1", modifiedBy,
                "2", projectId,
                "3", projectName,
                "4", projectDescription,
                "5", projectPrefix
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> deleteProject(int deletedBy, int projectId) {
        return Pair.of("call TaskManager.Delete_Project(?, ?)", Map.of(
                "1", deletedBy,
                "2", projectId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> restoreProject(int restoredBy, int projectId) {
        return Pair.of("call TaskManager.Restore_Project(?, ?)", Map.of(
                "1", restoredBy,
                "2", projectId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskTypes() {
        return Pair.of("call TaskManager.Select_TaskTypes()", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskTypeById(int taskTypeId) {
        return Pair.of("call TaskManager.Select_TaskTypeById(?)", Map.of("1", taskTypeId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskTypeByName(String taskTypeName) {
        return Pair.of("call TaskManager.Select_TaskTypeByName(?)", Map.of("1", taskTypeName));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskTypeByPrefix(String taskTypePrefix) {
        return Pair.of("call TaskManager.Select_TaskTypeByPrefix(?)", Map.of("1", taskTypePrefix));
    }

    @Override
    public Pair<String, Map<String, Object>> insertTaskType(int createdBy, String taskTypeName, String prefix) {
        return Pair.of("call TaskManager.Insert_TaskType(?, ?, ?)", Map.of(
                "1", createdBy,
                "2", taskTypeName,
                "3", prefix
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateTaskType(int modifiedBy, int taskTypeId, String taskTypeName, Integer taskTypeVersion, String prefix) {
        return Pair.of("call TaskManager.Update_TaskType(?, ?, ?, ?, ?)", Map.of(
                "1", modifiedBy,
                "2", taskTypeId,
                "3", taskTypeName,
                "4", taskTypeVersion,
                "5", prefix
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> deleteTaskType(int deletedBy, int taskTypeId) {
        return Pair.of("call TaskManager.Delete_TaskType(?, ?)", Map.of(
                "1", deletedBy,
                "2", taskTypeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> restoreTaskType(int restoredBy, int taskTypeId) {
        return Pair.of("call TaskManager.Restore_TaskType(?, ?)", Map.of(
                "1", restoredBy,
                "2", taskTypeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTasks() {
        return Pair.of("call TaskManager.Select_Tasks()", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskById(int taskId) {
        return Pair.of("call TaskManager.Select_TaskById(?)", Map.of("1", taskId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTasksByProject(int projectId) {
        return Pair.of("call TaskManager.Select_TasksByProject(?)", Map.of("1", projectId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTasksByProjectAndType(int projectId, int taskTypeId) {
        return Pair.of("call TaskManager.Select_TasksByProjectAndType(?, ?)", Map.of(
                "1", projectId,
                "2", taskTypeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskByProjectAndTypeAndKey(int projectId, int taskTypeId, int taskKey) {
        return Pair.of("call TaskManager.Select_TaskByProjectAndTypeAndKey(?, ?, ?)", Map.of(
                "1", projectId,
                "2", taskTypeId,
                "3", taskKey
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> insertTask(int createdBy, int taskTypeId, int projectId, Integer parentId) {
        return Pair.of("call TaskManager.Insert_Task(?, ?, ?, ?)", Map.of(
                "1", createdBy,
                "2", taskTypeId,
                "3", projectId,
                "4", parentId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateTask(int modifiedBy, int taskId, Integer parentId) {
        return Pair.of("call TaskManager.Update_Task(?, ?, ?)", Map.of(
                "1", modifiedBy,
                "2", taskId,
                "3", parentId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> deleteTask(int deletedBy, int taskId) {
        return Pair.of("call TaskManager.Delete_Task(?, ?)", Map.of(
                "1", deletedBy,
                "2", taskId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> restoreTask(int restoredBy, int taskId) {
        return Pair.of("call TaskManager.Restore_Task(?, ?)", Map.of(
                "1", restoredBy,
                "2", taskId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskAttributesForTask(int taskId) {
        return Pair.of("call TaskManager.Select_TaskAttributes(?)", Map.of("1", taskId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskAttributesForTaskByAttributeId(int taskId, int attributeId) {
        return Pair.of("call TaskManager.Select_TaskAttributesByAttribute(?, ?)", Map.of(
                "1", taskId,
                "2", attributeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectTaskAttributeById(int taskAttributeId) {
        return Pair.of("call TaskManager.Select_TaskAttributeById(?)", Map.of("1", taskAttributeId));
    }

    @Override
    public Pair<String, Map<String, Object>> insertTaskAttribute(int createdBy, int taskId, int attributeId, String value) {
        return Pair.of("call TaskManager.Insert_TaskAttribute(?, ?, ?, ?)", Map.of(
                "1", createdBy,
                "2", taskId,
                "3", attributeId,
                "4", value
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateTaskAttribute(int modifiedBy, int taskAttributeId, String value) {
        return Pair.of("call TaskManager.Update_TaskAttribute(?, ?, ?)", Map.of(
                "1", modifiedBy,
                "2", taskAttributeId,
                "3", value
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> deleteTaskAttribute(int deletedBy, int taskAttributeId) {
        return Pair.of("call TaskManager.Delete_TaskAttribute(?, ?)", Map.of(
                "1", deletedBy,
                "2", taskAttributeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> restoreTaskAttribute(int restoredBy, int taskAttributeId) {
        return Pair.of("call TaskManager.Restore_TaskAttribute(?, ?)", Map.of(
                "1", restoredBy,
                "2", taskAttributeId
        ));
    }
}
