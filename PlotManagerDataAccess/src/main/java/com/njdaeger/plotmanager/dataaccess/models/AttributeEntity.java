package com.njdaeger.plotmanager.dataaccess.models;

import com.njdaeger.plotmanager.dataaccess.Identifiable;

public class AttributeEntity implements Identifiable {

    @Column
    private int id;
    @Column
    private String name;
    @Column
    private String type;

    //auditing fields
    @Column
    private boolean deleted;
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

    public String getType() {
        return type;
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

    public boolean isDeleted() {
        return deleted;
    }
}
