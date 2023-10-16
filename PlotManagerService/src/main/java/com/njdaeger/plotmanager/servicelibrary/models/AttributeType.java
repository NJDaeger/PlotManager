package com.njdaeger.plotmanager.servicelibrary.models;

import java.util.List;

public class AttributeType {

    private final String name;
    private final List<String> values;

    public AttributeType(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}
