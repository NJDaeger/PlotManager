package com.njdaeger.plotmanager.service.models;

public class Attribute {

    private final int id;
    private final String name;
    private final String type;

    public Attribute(int id, String name, String type) {
        this.name = name;
        this.type = type;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

}
