package com.njdaeger.plotmanager.dataaccess.repositories;

import com.njdaeger.plotmanager.dataaccess.IRepository;
import com.njdaeger.plotmanager.dataaccess.models.PlotAttributeEntity;
import com.njdaeger.plotmanager.dataaccess.models.PlotEntity;
import com.njdaeger.plotmanager.dataaccess.models.PlotGroupEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IPlotRepository extends IRepository {

    /**
     * Get all plots
     * @return A list of plots
     */
    CompletableFuture<List<PlotEntity>> getPlots();

    /**
     * Get a plot by id
     * @param plotId The id of the plot
     * @return The plot
     */
    CompletableFuture<PlotEntity> getPlotById(int plotId);

    /**
     * Insert a plot
     * @param plot The plot to insert
     * @return The inserted plot id, or null if no plot was inserted
     */
    CompletableFuture<Integer> insertPlot(PlotEntity plot);

    /**
     * Update a plot
     * @param plot The plot to update
     * @return The updated plot id, or null if no plot was updated
     */
    CompletableFuture<Integer> updatePlot(PlotEntity plot);

    /**
     * Delete a plot
     * @param plotId The id of the plot to delete
     * @return The deleted plot id, or null if no plot was deleted
     */
    CompletableFuture<Integer> deletePlot(int plotId);

    /**
     * Get all attributes for a plot
     * @param plotId The id of the plot
     * @return A list of attributes
     */
    CompletableFuture<List<PlotAttributeEntity>> getPlotAttributesForPlot(int plotId);

    /**
     * Get an attribute for a plot
     * @param plotId The id of the plot
     * @param attributeId The id of the attribute
     * @return The attribute
     */
    CompletableFuture<PlotAttributeEntity> getPlotAttributeForPlot(int plotId, int attributeId);

    /**
     * Insert an attribute for a plot
     * @param plotId The id of the plot
     * @param attribute The attribute to insert
     * @return The inserted attribute id, or null if no attribute was inserted
     */
    CompletableFuture<Integer> insertPlotAttributeForPlot(int plotId, PlotAttributeEntity attribute);

    /**
     * Update an attribute for a plot
     * @param plotId The id of the plot
     * @param attribute The attribute to update
     * @return The updated attribute id, or null if no attribute was updated
     */
    CompletableFuture<Integer> updatePlotAttributeForPlot(int plotId, PlotAttributeEntity attribute);

    /**
     * Delete an attribute for a plot
     * @param plotId The id of the plot
     * @param attributeId The id of the attribute
     * @return The deleted attribute id, or null if no attribute was deleted
     */
    CompletableFuture<Integer> deletePlotAttributeForPlot(int plotId, int attributeId);

    /**
     * Get all plot groups
     * @return A list of plot groups
     */
    CompletableFuture<List<PlotGroupEntity>> getPlotGroups();

    /**
     * Get a plot groups by id
     * @param plotGroupId The id of the plot group
     * @return The plot group
     */
    CompletableFuture<PlotGroupEntity> getPlotGroupById(int plotGroupId);

    /**
     * Insert a plot group
     * @param plotGroup The plot group to insert
     * @return The inserted plot group id, or null if no plot group was inserted
     */
    CompletableFuture<Integer> insertPlotGroup(PlotGroupEntity plotGroup);

    /**
     * Update a plot group
     * @param plotGroup The plot group to update
     * @return The updated plot group id, or null if no plot group was updated
     */
    CompletableFuture<Integer> updatePlotGroup(PlotGroupEntity plotGroup);

    /**
     * Delete a plot group
     * @param plotGroupId The id of the plot group to delete
     * @return The deleted plot group id, or null if no plot group was deleted
     */
    CompletableFuture<Integer> deletePlotGroup(int plotGroupId);

}
