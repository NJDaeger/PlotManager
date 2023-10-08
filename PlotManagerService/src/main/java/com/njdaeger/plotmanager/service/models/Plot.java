package com.njdaeger.plotmanager.service.models;

import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class Plot {

    private Plot parent;
    private PlotGroup plotGroup;
    private Location location;
    private List<PlotAttribute> attributes;
    private List<User> users;
    //name, description, status, type are all attributes

    public Plot(Location location, List<PlotAttribute> attributes, List<User> users, Plot parent, PlotGroup plotGroup) {
        this.location = location;
        this.attributes = attributes;
        this.users = users;
        this.parent = parent;
        this.plotGroup = plotGroup;
    }

    public Location getLocation() {
        return location;
    }

    public List<PlotAttribute> getAttributes() {
        return attributes;
    }

    public PlotAttribute getAttribute(String attributeName) {
        return attributes.stream().filter(attribute -> attribute.getAttribute().equalsIgnoreCase(attributeName)).findFirst().orElse(null);
    }

    public Plot getParent() {
        return parent;
    }

    public List<User> getUsers() {
        return users;
    }

    public User getUser(UUID userId) {
        return users.stream().filter(user -> user.getUserId().equals(userId)).findFirst().orElse(null);
    }

}
