package com.njdaeger.plotmanager.dataaccess.repositories;

import com.njdaeger.plotmanager.dataaccess.IRepository;
import com.njdaeger.plotmanager.dataaccess.models.GroupEntity;
import com.njdaeger.plotmanager.dataaccess.models.GroupAttributeEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IGroupRepository extends IRepository {

    /**
     * Get all plot groups
     * @return A list of plot groups
     */
    CompletableFuture<List<GroupEntity>> getPlotGroups();

    /**
     * Get a plot groups by id
     * @param plotGroupId The id of the plot group
     * @return The plot group
     */
    CompletableFuture<GroupEntity> getPlotGroupById(int plotGroupId);

    /**
     * Insert a plot group
     * @param createdBy The id of the user who created this plot group
     * @param groupName The name of the plot group
     * @return The newly created plot group, or null if no plot group was created
     */
    CompletableFuture<GroupEntity> insertPlotGroup(int createdBy, String groupName);

    /**
     * Update a plot group
     * @param updatedBy The id of the user who updated this plot group
     * @param plotGroupid The id of the plot group to update
     * @param newPlotGroupName The new plot group name
     * @return The updated plot group, or null if no plot group was updated
     */
    CompletableFuture<GroupEntity> updatePlotGroup(int updatedBy, int plotGroupid, String newPlotGroupName);

    /**
     * Delete a plot group
     * @param deletedBy The id of the user who deleted this plot group
     * @param plotGroupId The id of the plot group to delete
     * @return The deleted plot group id, or -1 if no plot group was deleted
     */
    CompletableFuture<Integer> deletePlotGroup(int deletedBy, int plotGroupId);

    /**
     * Get all attributes for a group
     * @param groupId The id of the group
     * @return A list of attributes for the group
     */
    CompletableFuture<List<GroupAttributeEntity>> getGroupAttributesForGroup(int groupId);

    /**
     * Get an attribute for a group
     * @param groupId The id of the group
     * @param attributeId The id of the attribute
     * @return The attribute for the group
     */
    CompletableFuture<GroupAttributeEntity> getGroupAttributeForGroup(int groupId, int attributeId);

    /**
     * Insert an attribute for a group
     * @param createdBy The id of the user who created this attribute
     * @param groupId The id of the group
     * @param attributeId The id of the attribute
     * @param value The value of the attribute
     * @return The inserted attribute, or null if no attribute was inserted
     */
    CompletableFuture<GroupAttributeEntity> insertGroupAttribute(int createdBy, int groupId, int attributeId, String value);

    /**
     * Update an attribute for a group
     * @param updatedBy The id of the user who updated this attribute
     * @param groupId The id of the group
     * @param attributeId The id of the attribute, or null to not update the attribute id
     * @param value The new value of the attribute, or null to not update the value
     * @return The updated attribute id, or null if no attribute was updated
     */
    CompletableFuture<GroupAttributeEntity> updateGroupAttribute(int updatedBy, int groupId, Integer attributeId, String value);

    /**
     * Delete an attribute for a group
     * @param deletedBy The id of the user who deleted this attribute
     * @param groupId The id of the group
     * @param attributeId The id of the attribute
     * @return The deleted attribute id, or null if no attribute was deleted
     */
    CompletableFuture<Integer> deleteGroupAttribute(int deletedBy, int groupId, int attributeId);

}
