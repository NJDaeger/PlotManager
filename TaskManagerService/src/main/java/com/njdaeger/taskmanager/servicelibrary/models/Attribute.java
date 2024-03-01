package com.njdaeger.taskmanager.servicelibrary.models;

public class Attribute {

    private final int id;
    private final String name;

    public Attribute(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

}
