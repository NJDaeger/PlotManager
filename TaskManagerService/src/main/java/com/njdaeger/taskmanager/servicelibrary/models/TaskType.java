package com.njdaeger.taskmanager.servicelibrary.models;

public class TaskType {

    private int taskTypeId;
    private String taskTypeName;
    private String taskTypePrefix;
    private int taskTypeVersion;

    public TaskType(int taskTypeId, String taskTypeName, String taskTypePrefix, int taskTypeVersion) {
        this.taskTypeId = taskTypeId;
        this.taskTypeName = taskTypeName;
        this.taskTypePrefix = taskTypePrefix;
        this.taskTypeVersion = taskTypeVersion;
    }

    public int getTaskTypeId() {
        return taskTypeId;
    }

    public String getTaskTypeName() {
        return taskTypeName;
    }

    public String getTaskTypePrefix() {
        return taskTypePrefix;
    }

    public int getTaskTypeVersion() {
        return taskTypeVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof TaskType other)) return false;
        return other.taskTypeId == taskTypeId;
    }
}
