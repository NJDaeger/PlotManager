package com.njdaeger.plotmanager.dataaccess.repositories.impl;

import com.njdaeger.plotmanager.dataaccess.models.PlotAttributeEntity;
import com.njdaeger.plotmanager.dataaccess.models.PlotEntity;
import com.njdaeger.plotmanager.dataaccess.models.PlotGroupEntity;
import com.njdaeger.plotmanager.dataaccess.repositories.IPlotRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlotRepositoryImpl implements IPlotRepository {
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
    public CompletableFuture<Integer> insertPlot(PlotEntity plot) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> updatePlot(PlotEntity plot) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> deletePlot(int plotId) {
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
    public CompletableFuture<Integer> insertPlotAttributeForPlot(int plotId, PlotAttributeEntity attribute) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> updatePlotAttributeForPlot(int plotId, PlotAttributeEntity attribute) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> deletePlotAttributeForPlot(int plotId, int attributeId) {
        return null;
    }

    @Override
    public CompletableFuture<List<PlotGroupEntity>> getPlotGroups() {
        return null;
    }

    @Override
    public CompletableFuture<PlotGroupEntity> getPlotGroupById(int plotGroupId) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> insertPlotGroup(PlotGroupEntity plotGroup) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> updatePlotGroup(PlotGroupEntity plotGroup) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> deletePlotGroup(int plotGroupId) {
        return null;
    }
}
