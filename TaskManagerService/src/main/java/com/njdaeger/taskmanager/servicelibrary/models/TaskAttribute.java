package com.njdaeger.taskmanager.servicelibrary.models;

public class TaskAttribute {

    private final int taskAttributeId;
    private final Attribute attribute;
    private final String value;

    public TaskAttribute(int taskAttributeId, Attribute attribute, String value) {
        this.taskAttributeId = taskAttributeId;
        this.attribute = attribute;
        this.value = value;
    }

    public int getTaskAttributeId() {
        return taskAttributeId;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof TaskAttribute other)) return false;
        return other.taskAttributeId == taskAttributeId;
    }
}
