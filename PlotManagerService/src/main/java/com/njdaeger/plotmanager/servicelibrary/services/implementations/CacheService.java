package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.servicelibrary.PlotBuilder;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotGroup;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.models.World;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CacheService implements ICacheService {

    private final Map<String, Attribute> attributeCache = new ConcurrentHashMap<>();
    private final Map<UUID, User> userCache = new ConcurrentHashMap<>();
    private final Map<UUID, World> worldCache = new ConcurrentHashMap<>();
    private final Map<String, PlotGroup> groupCache = new ConcurrentHashMap<>();
    private final Map<Integer, Plot> plotCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlotBuilder> builderCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, Attribute> getAttributeCache() {
        return attributeCache;
    }

    @Override
    public Map<UUID, User> getUserCache() {
        return userCache;
    }

    @Override
    public Map<UUID, World> getWorldCache() {
        return worldCache;
    }

    @Override
    public Map<String, PlotGroup> getGroupCache() {
        return groupCache;
    }

    @Override
    public Map<Integer, Plot> getPlotCache() {
        return plotCache;
    }

    @Override
    public Map<UUID, PlotBuilder> getPlotBuilderCache() {
        return builderCache;
    }
}
