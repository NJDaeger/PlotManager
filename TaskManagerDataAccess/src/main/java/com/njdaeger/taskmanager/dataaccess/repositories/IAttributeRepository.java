package com.njdaeger.taskmanager.dataaccess.repositories;

import com.njdaeger.taskmanager.dataaccess.IRepository;
import com.njdaeger.taskmanager.dataaccess.models.AttributeEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the interactions with the following tables:
 * * Attribute
 */
public interface IAttributeRepository extends IRepository {

    /**
     * Get all attributes
     * @return A list of attributes
     */
    CompletableFuture<List<AttributeEntity>> getAttributes();

    /**
     * Get all attributes for a task type
     * @param taskTypeId The id of the task type
     * @return A list of attributes
     */
    CompletableFuture<List<AttributeEntity>> getAttributesByTaskType(int taskTypeId);

    /**
     * Get an attribute by name
     * @param name The name of the attribute
     * @param taskTypeId The id of the task type the attribute is for
     * @return The attribute
     */
    CompletableFuture<AttributeEntity> getAttributeByName(String name, int taskTypeId);

    /**
     * Get an attribute by id
     * @param id The id of the attribute
     * @return The attribute
     */
    CompletableFuture<AttributeEntity> getAttributeById(int id);

    /**
     * Insert an attribute
     * @param createdBy The id of the user who created the attribute
     * @param taskTypeId The id of the task type the attribute is for
     * @param name The name of the attribute
     * @return The inserted attribute, or null if the attribute was not inserted
     */
    CompletableFuture<AttributeEntity> insertAttribute(int createdBy, int taskTypeId, String name);

    /**
     * Update an attribute
     * @param modifiedBy The id of the user who updated the attribute
     * @param attributeId The attribute to update
     * @param taskTypeId The new task type of the attribute, or null to keep the old task type
     * @param name The new name of the attribute, or null to keep the old name
     * @return The updated attribute, or null if the attribute was not updated
     */
    CompletableFuture<AttributeEntity> updateAttribute(int modifiedBy, int attributeId, Integer taskTypeId, String name);

}
