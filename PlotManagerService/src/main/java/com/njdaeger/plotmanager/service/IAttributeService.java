package com.njdaeger.plotmanager.service;

import com.njdaeger.plotmanager.service.models.Attribute;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IAttributeService extends ITransactionalService {

    /**
     * Get all attributes
     * @return A result with a list of attributes if successful, or a result with an empty list if the attribute retrieval was unsuccessful.
     */
    CompletableFuture<Result<List<Attribute>>> getAttributes();

    /**
     * Create an attribute
     * @param createdBy The creator of the attribute
     * @param name The name of the attribute
     * @param type The type of the attribute
     * @return A result with the created attribute if successful, or a result with null if the attribute creation was unsuccessful.
     */
    CompletableFuture<Result<Attribute>> createAttribute(UUID createdBy, String name, String type);

    /**
     * Update an attribute name
     * @param modifiedBy The modifier of the attribute
     * @param oldName The old name of the attribute
     * @param newName The new name of the attribute
     * @return A result with the updated attribute if successful, or a result with null if the attribute update was unsuccessful.
     */
    CompletableFuture<Result<Attribute>> updateAttributeName(UUID modifiedBy, String oldName, String newName);

    /**
     * Update an attribute type
     * @param modifiedBy The modifier of the attribute
     * @param oldType The old type of the attribute
     * @param newType The new type of the attribute
     * @return A result with the updated attribute if successful, or a result with null if the attribute update was unsuccessful.
     */
    CompletableFuture<Result<Attribute>> updateAttributeType(UUID modifiedBy, String oldType, String newType);

    /**
     * Delete an attribute
     * @param deletedBy The deleter of the attribute
     * @param name The name of the attribute to delete
     * @return A result with the deleted attribute if successful, or a result with null if the attribute deletion was unsuccessful.
     */
    CompletableFuture<Result<Attribute>> deleteAttribute(UUID deletedBy, String name);

}
