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
     * Get an attribute by name
     * @param name The name of the attribute
     * @return The attribute
     */
    CompletableFuture<AttributeEntity> getAttributeByName(String name);

    /**
     * Get an attribute by id
     * @param id The id of the attribute
     * @return The attribute
     */
    CompletableFuture<AttributeEntity> getAttributeById(int id);

    /**
     * Insert an attribute
     * @param createdBy The id of the user who created the attribute
     * @param name The name of the attribute
     * @return The inserted attribute, or null if the attribute was not inserted
     */
    CompletableFuture<AttributeEntity> insertAttribute(int createdBy, String name);

    /**
     * Update an attribute
     * @param modifiedBy The id of the user who updated the attribute
     * @param attributeId The attribute to update
     * @param name The new name of the attribute, or null to keep the old name
     * @return The updated attribute, or null if the attribute was not updated
     */
    CompletableFuture<AttributeEntity> updateAttribute(int modifiedBy, int attributeId, String name);

}
