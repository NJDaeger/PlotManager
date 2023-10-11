package com.njdaeger.plotmanager.dataaccess.repositories.implementations;

import com.njdaeger.plotmanager.dataaccess.models.PlotAttributeEntity;
import com.njdaeger.plotmanager.dataaccess.models.PlotEntity;
import com.njdaeger.plotmanager.dataaccess.models.GroupEntity;
import com.njdaeger.plotmanager.dataaccess.repositories.IPlotRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlotRepository implements IPlotRepository {

    @Override
    public CompletableFuture<Boolean> initializeRepository() {
        return null;
    }

    @Override
    public CompletableFuture<List<PlotEntity>> getPlots() {
        return null;
    }

    @Override
    public CompletableFuture<PlotEntity> getPlotById(int plotId) {
        return null;
    }

    @Override
    public CompletableFuture<PlotEntity> insertPlot(int createdBy, int worldId, int x, int y, int z) {
        return null;
    }

    @Override
    public CompletableFuture<PlotEntity> updatePlot(int updatedBy, int plotId, Integer newWorldId, Integer newX, Integer newY, Integer newZ) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> deletePlot(int deletedBy, int plotId) {
        return null;
    }

    @Override
    public CompletableFuture<List<PlotAttributeEntity>> getPlotAttributesForPlot(int plotId) {
        return null;
    }

    @Override
    public CompletableFuture<PlotAttributeEntity> getPlotAttributeForPlot(int plotId, int attributeId) {
        return null;
    }

    @Override
    public CompletableFuture<PlotAttributeEntity> insertPlotAttribute(int createdBy, int plotId, int attributeId, String value) {
        return null;
    }

    @Override
    public CompletableFuture<PlotAttributeEntity> updatePlotAttribute(int updatedBy, int plotId, Integer attributeId, String value) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> deletePlotAttribute(int deletedBy, int plotId, int attributeId) {
        return null;
    }

    @Override
    public CompletableFuture<List<GroupEntity>> getPlotGroups() {
        return null;
    }

    @Override
    public CompletableFuture<GroupEntity> getPlotGroupById(int plotGroupId) {
        return null;
    }

    @Override
    public CompletableFuture<GroupEntity> insertPlotGroup(int createdBy, String groupName) {
        return null;
    }

    @Override
    public CompletableFuture<GroupEntity> updatePlotGroup(int updatedBy, int plotGroupid, String newPlotGroupName) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> deletePlotGroup(int deletedBy, int plotGroupId) {
        return null;
    }
}
