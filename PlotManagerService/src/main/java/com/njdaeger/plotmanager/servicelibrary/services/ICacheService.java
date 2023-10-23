package com.njdaeger.plotmanager.servicelibrary.services;

import com.njdaeger.plotmanager.servicelibrary.PlotBuilder;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotGroup;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.models.World;

import java.util.Map;
import java.util.UUID;

public interface ICacheService {

    /**
     * Attribute name mapped to its attribute object
     * @return The attribute cache
     */
    Map<String, Attribute> getAttributeCache();

    /**
     * User UUID mapped to their user object
     * @return The user cache
     */
    Map<UUID, User> getUserCache();

    /**
     * World UUID mapped to its world object
     * @return The world cache
     */
    Map<UUID, World> getWorldCache();

    /**
     * PlotGroup name mapped to its plot group object
     * @return The plot group cache
     */
    Map<String, PlotGroup> getGroupCache();

    /**
     * Plot ID mapped to its plot object
     * @return The plot cache
     */
    Map<Integer, Plot> getPlotCache();

    /**
     * User UUID mapped to their plot builder object
     * @return The plot builder cache
     */
    Map<UUID, PlotBuilder> getPlotBuilderCache();

}
