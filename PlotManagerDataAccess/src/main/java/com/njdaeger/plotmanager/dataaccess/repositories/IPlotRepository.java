package com.njdaeger.plotmanager.dataaccess.repositories;

import com.njdaeger.plotmanager.dataaccess.IRepository;
import com.njdaeger.plotmanager.dataaccess.models.PlotAttributeEntity;
import com.njdaeger.plotmanager.dataaccess.models.PlotEntity;
import com.njdaeger.plotmanager.dataaccess.models.GroupEntity;

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
     * @param createdBy The id of the user who created this plot
     * @param worldId The id of the world this plot is in
     * @param x The x coordinate of the plot
     * @param y The y coordinate of the plot
     * @param z The z coordinate of the plot
     * @return The newly created plot, or null if no plot was created
     */
    CompletableFuture<PlotEntity> insertPlot(int createdBy, int worldId, int x, int y, int z);

    /**
     * Update a plot
     * @param updatedBy The id of the user who updated this plot
     * @param plotId The id of the plot to update
     * @param newWorldId The new world id of the plot, or null to not update the world id
     * @param newX The new x coordinate of the plot, or null to not update the x coordinate
     * @param newY The new y coordinate of the plot, or null to not update the y coordinate
     * @param newZ The new z coordinate of the plot, or null to not update the z coordinate
     * @return The updated plot, or null if no plot was updated
     */
    CompletableFuture<PlotEntity> updatePlotLocation(int updatedBy, int plotId, Integer newWorldId, Integer newX, Integer newY, Integer newZ);

    /**
     * Update a plot's parent
     * @param updatedBy The id of the user who updated this plot
     * @param plotId The id of the plot to update
     * @param newParentId The new parent id of the plot, or null to not update the parent id
     * @return The updated plot, or null if no plot was updated
     */
    CompletableFuture<PlotEntity> updatePlotParent(int updatedBy, int plotId, Integer newParentId);

    /**
     * Update a plot's group
     * @param updatedBy The id of the user who updated this plot
     * @param plotId The id of the plot to update
     * @param newGroupId The new group id of the plot, or null remove the plot from its group
     * @return The updated plot, or null if no plot was updated
     */
    CompletableFuture<PlotEntity> updatePlotGroup(int updatedBy, int plotId, Integer newGroupId);

    /**
     * Adds a user to a plot
     * @param insertedBy The id of the user who inserted this plot user
     * @param plotId The id of the plot to insert the user into
     * @param userId The id of the user to insert into the plot
     * @return The updated plot, or null if no plot was updated
     */
    CompletableFuture<PlotEntity> insertPlotUser(int insertedBy, int plotId, int userId);

    /**
     * Removes a user from a plot
     * @param deletedBy The id of the user who deleted this plot user
     * @param plotId The id of the plot to delete the user from
     * @param userId The id of the user to delete from the plot
     * @return The updated plot, or null if no plot was updated
     */
    CompletableFuture<PlotEntity> deletePlotUser(int deletedBy, int plotId, int userId);

    /**
     * Delete a plot
     * @param deletedBy The id of the user who deleted this plot
     * @param plotId The id of the plot to delete
     * @return The deleted plot id, or -1 if no plot was deleted
     */
    CompletableFuture<Integer> deletePlot(int deletedBy, int plotId);

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
     * @param createdBy The id of the user who created this attribute
     * @param plotId The id of the plot
     * @param attributeId The id of the attribute
     * @param value The value of the attribute
     * @return The inserted attribute, or null if no attribute was inserted
     */
    CompletableFuture<PlotAttributeEntity> insertPlotAttribute(int createdBy, int plotId, int attributeId, String value);

    /**
     * Update an attribute for a plot
     * @param updatedBy The id of the user who updated this attribute
     * @param plotId The id of the plot
     * @param attributeId The id of the attribute, or null to not update the attribute id
     * @param value The new value of the attribute, or null to not update the value
     * @return The updated attribute id, or null if no attribute was updated
     */
    CompletableFuture<PlotAttributeEntity> updatePlotAttribute(int updatedBy, int plotId, Integer attributeId, String value);

    /**
     * Delete an attribute for a plot
     * @param deletedBy The id of the user who deleted this attribute
     * @param plotId The id of the plot
     * @param attributeId The id of the attribute
     * @return The deleted attribute id, or null if no attribute was deleted
     */
    CompletableFuture<Integer> deletePlotAttribute(int deletedBy, int plotId, int attributeId);

}
