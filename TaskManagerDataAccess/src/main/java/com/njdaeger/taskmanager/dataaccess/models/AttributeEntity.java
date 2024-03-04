package com.njdaeger.taskmanager.dataaccess.models;

import com.njdaeger.taskmanager.dataaccess.Identifiable;

public class AttributeEntity implements Identifiable {

    @Column
    private int id;
    @Column
    private String name;

    @Column
    private int taskTypeId;

    //auditing fields
    @Column
    private int createdBy;
    @Column
    private Integer modifiedBy;
    @Column
    private long created;
    @Column
    private Long modified;

    public AttributeEntity() {}

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getTaskTypeId() {
        return taskTypeId;
    }

    public long getCreated() {
        return created;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public Long getModified() {
        return modified;
    }

    public Integer getModifiedBy() {
        return modifiedBy;
    }
}
