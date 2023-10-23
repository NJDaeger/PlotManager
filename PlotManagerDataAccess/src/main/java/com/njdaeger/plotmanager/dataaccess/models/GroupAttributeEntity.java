package com.njdaeger.plotmanager.dataaccess.models;

import com.njdaeger.plotmanager.dataaccess.Identifiable;

public class GroupAttributeEntity implements Identifiable {

    @Column
    private int id;
    @Column
    private int groupId;
    @Column
    private int attributeId;
    @Column
    private String value;

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

    public GroupAttributeEntity() {}

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getAttribute() {
        return attributeId;
    }

    public int getGroup() {
        return groupId;
    }

    public String getValue() {
        return value;
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
