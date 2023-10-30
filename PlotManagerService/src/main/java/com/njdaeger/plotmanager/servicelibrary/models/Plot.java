package com.njdaeger.plotmanager.servicelibrary.models;

import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class Plot {

    private final int id;
    private Plot parent;
    private PlotGroup plotGroup;
    private Location location;
    private List<PlotAttribute> attributes;
    private List<PlotUser> users;
    private boolean deleted;

    public Plot(int id, Location location, List<PlotAttribute> attributes, List<PlotUser> users, Plot parent, PlotGroup plotGroup, boolean deleted) {
        this.id = id;
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

    public PlotGroup getPlotGroup() {
        return plotGroup;
    }

    public List<PlotUser> getUsers() {
        return users;
    }

    public PlotUser getUser(UUID userId) {
        return users.stream().filter(user -> user.getUser().getUserId().equals(userId)).findFirst().orElse(null);
    }

    public boolean hasUser(UUID userId) {
        return users.stream().anyMatch(user -> user.getUser().getUserId().equals(userId) && !user.isDeleted());
    }

    public boolean hasAnyActiveUsers() {
        return users.stream().anyMatch(user -> !user.isDeleted());
    }

    public int getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
