package com.njdaeger.taskmanager.servicelibrary.models;

import java.util.List;

public class Task {

    private int taskId;
    private TaskType taskType;
    private Task parent;
    private Project project;
    private int taskKey;
    private List<TaskAttribute> attributes;

    public Task(int taskId, TaskType taskType, Task parent, Project project, int taskKey, List<TaskAttribute> attributes) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.parent = parent;
        this.project = project;
        this.taskKey = taskKey;
        this.attributes = attributes;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getTaskKey() {
        return taskKey;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public Task getParent() {
        return parent;
    }

    public Project getProject() {
        return project;
    }

    public List<TaskAttribute> getAttributes() {
        return attributes;
    }

    public boolean hasAttribute(String name) {
        return attributes.stream().anyMatch(attr -> attr.getAttribute().getName().equalsIgnoreCase(name));
    }

    public TaskAttribute getAttribute(String name) {
        return attributes.stream().filter(attr -> attr.getAttribute().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Task other)) return false;
        return other.taskId == taskId;
    }
}
