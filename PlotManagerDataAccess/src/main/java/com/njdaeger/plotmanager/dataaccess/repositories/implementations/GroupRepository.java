package com.njdaeger.plotmanager.dataaccess.repositories.implementations;

import com.njdaeger.plotmanager.dataaccess.models.GroupAttributeEntity;
import com.njdaeger.plotmanager.dataaccess.models.GroupEntity;
import com.njdaeger.plotmanager.dataaccess.repositories.IGroupRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GroupRepository implements IGroupRepository {
    @Override
    public CompletableFuture<Boolean> initializeRepository() {
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

    @Override
    public CompletableFuture<List<GroupAttributeEntity>> getGroupAttributesForGroup(int groupId) {
        return null;
    }

    @Override
    public CompletableFuture<GroupAttributeEntity> getGroupAttributeForGroup(int groupId, int attributeId) {
        return null;
    }

    @Override
    public CompletableFuture<GroupAttributeEntity> insertGroupAttribute(int createdBy, int groupId, int attributeId, String value) {
        return null;
    }

    @Override
    public CompletableFuture<GroupAttributeEntity> updateGroupAttribute(int updatedBy, int groupId, Integer attributeId, String value) {
        return null;
    }

    @Override
    public CompletableFuture<Integer> deleteGroupAttribute(int deletedBy, int groupId, int attributeId) {
        return null;
    }
}
