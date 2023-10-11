package com.njdaeger.plotmanager.servicelibrary;

import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotAttribute;
import com.njdaeger.plotmanager.servicelibrary.models.PlotGroup;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class PlotBuilder {

    private final Location location;
    private List<PlotAttribute> attributes;
    private PlotGroup plotGroup;
    private List<User> users;
    private Plot parent;

    public PlotBuilder(Location location) {
        this.location = location;

        this.attributes = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public PlotBuilder addAttribute(String attribute, String value) {
        attributes.removeIf(pa -> pa.getAttribute().equalsIgnoreCase(attribute));
        attributes.add(new PlotAttribute(attribute, value));
        return this;
    }

//    public PlotBuilder addUser(UUID userId) {
//        users.removeIf(user -> user.getUserId().equals(userId));
//        users.add(userId);
//        return this;
//    }

    public PlotBuilder setPlotGroup(PlotGroup plotGroup) {
        this.plotGroup = plotGroup;
        return this;
    }

    public PlotBuilder setParent(Plot parent) {
        this.parent = parent;
        return this;
    }

    public Plot build() {
        return new Plot(location, attributes, users, parent, plotGroup);
    }

}
