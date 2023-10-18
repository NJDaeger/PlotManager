package com.njdaeger.plotmanager.servicelibrary.models;

import java.util.List;
import java.util.function.Predicate;

public class AttributeType {

    private final String name;
    private final List<String> values;
    private final Predicate<Object> validator;

    public AttributeType(String name, List<String> values) {
        this.name = name;
        this.values = values;
        this.validator = (obj) -> values.contains(obj.toString());
    }

    public AttributeType(String name, List<String> values, Predicate<Object> validator) {
        this.name = name;
        this.values = values;
        this.validator = validator;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isValidValue(Object obj) {
        return validator.test(obj);
    }

}
