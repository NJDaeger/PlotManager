package com.njdaeger.plotmanager.dataaccess.models;

import com.njdaeger.plotmanager.dataaccess.Identifiable;

public class PlotUserEntity implements Identifiable {

    @Column
    private int id;
    @Column
    private int plotId;
    @Column
    private int userId;


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

    public PlotUserEntity() {}

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getPlot() {
        return plotId;
    }

    public int getUser() {
        return userId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public long getCreated() {
        return created;
    }

    public Integer getModifiedBy() {
        return modifiedBy;
    }

    public Long getModified() {
        return modified;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
