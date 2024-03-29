package com.njdaeger.plotmanager.dataaccess.models;

import com.njdaeger.plotmanager.dataaccess.Identifiable;

public class PlotEntity implements Identifiable {

    @Column
    private int id;
    @Column
    private Integer parentId;
    @Column
    private int worldId;
    @Column
    private int x;
    @Column
    private int y;
    @Column
    private int z;

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


    public PlotEntity() {}

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public Integer getParent() {
        return parentId;
    }

    public int getWorld() {
        return worldId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
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
