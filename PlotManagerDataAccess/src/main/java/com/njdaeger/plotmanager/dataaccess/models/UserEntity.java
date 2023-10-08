package com.njdaeger.plotmanager.dataaccess.models;

import com.njdaeger.plotmanager.dataaccess.Identifiable;

public class UserEntity implements Identifiable {

    @Column
    private int id;
    @Column
    private String uuid;
    @Column
    private String username;

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

    public UserEntity() {}

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
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
