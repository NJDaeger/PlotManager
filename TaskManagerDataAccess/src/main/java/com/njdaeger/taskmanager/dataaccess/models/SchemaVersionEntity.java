package com.njdaeger.taskmanager.dataaccess.models;

import com.njdaeger.taskmanager.dataaccess.Identifiable;

public class SchemaVersionEntity implements Identifiable {

    @Column
    private int version;

    public SchemaVersionEntity() {
    }

    public int getId() {
        return version;
    }

    @Override
    public void setId(int id) {
        this.version = id;
    }
}
