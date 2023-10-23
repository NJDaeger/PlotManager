package com.njdaeger.plotmanager.servicelibrary.models;

import java.util.List;

public class PlotGroup {

    private final int id;
    private final String name;
    private final List<PlotAttribute> attributes;

    public PlotGroup(int id, String name, List<PlotAttribute> attributes) {
        this.id = id;
        this.name = name;
        this.attributes = attributes;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PlotAttribute> getAttributes() {
        return attributes;
    }
}
