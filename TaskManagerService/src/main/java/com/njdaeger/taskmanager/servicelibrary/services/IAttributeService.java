package com.njdaeger.taskmanager.servicelibrary.services;

import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.transactional.ITransactionalService;
import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.Attribute;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IAttributeService extends ITransactionalService, ICached {

    /**
     * Get all attributes
     * @return A result with a list of attributes if successful, or a result with an empty list if the attribute retrieval was unsuccessful.
     */
    CompletableFuture<Result<List<Attribute>>> getAttributes();

    /**
     * Get an attribute by name
     * @param name The name of the attribute
     * @param taskType The type of task the attribute is for
     * @return A result with the attribute if successful, or a result with null if the attribute retrieval was unsuccessful.
     */
    CompletableFuture<Result<Attribute>> getAttribute(String name, TaskType taskType);

    /**
     * Get an attribute by id
     * @param id The id of the attribute
     * @return A result with the attribute if successful, or a result with null if the attribute retrieval was unsuccessful.
     */
    CompletableFuture<Result<Attribute>> getAttributeById(int id);

    /**
     * Create an attribute
     * @param createdBy The creator of the attribute
     * @param taskType The type of task the attribute is for
     * @param name The name of the attribute
     * @return A result with the created attribute if successful, or a result with null if the attribute creation was unsuccessful.
     */
    CompletableFuture<Result<Attribute>> createAttribute(UUID createdBy, TaskType taskType, String name);

    /**
     * Update an attribute name
     * @param modifiedBy The modifier of the attribute
     * @param attributeBeingUpdated The old name of the attribute
     * @param taskType The type of task the attribute is for, or null to keep the old task type
     * @param newName The new name of the attribute, or null to keep the old name
     * @return A result with the updated attribute if successful, or a result with null if the attribute update was unsuccessful.
     */
    CompletableFuture<Result<Attribute>> updateAttribute(UUID modifiedBy, String attributeBeingUpdated, TaskType taskType, String newName);

}
