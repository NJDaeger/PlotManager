package com.njdaeger.plotmanager.servicelibrary.models;

public class PlotAttribute {

    private final String attribute;
    private final String value;

    public PlotAttribute(String attribute, String value) {
        this.value = value;
        this.attribute = attribute;
    }

    public String getValue() {
        return value;
    }

    public String getAttribute() {
        return attribute;
    }

}
