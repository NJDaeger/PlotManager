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
}
