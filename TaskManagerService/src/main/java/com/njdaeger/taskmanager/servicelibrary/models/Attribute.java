package com.njdaeger.taskmanager.servicelibrary.models;

public class Attribute {

    private final int id;
    private final String name;

    private final TaskType taskType;

    public Attribute(int id, TaskType taskType, String name) {
        this.name = name;
        this.id = id;
        this.taskType = taskType;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Attribute other)) return false;
        return other.id == id;
    }
}
