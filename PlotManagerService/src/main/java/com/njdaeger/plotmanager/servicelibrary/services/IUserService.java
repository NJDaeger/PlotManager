package com.njdaeger.plotmanager.servicelibrary.services;

import com.njdaeger.plotmanager.dataaccess.Util;
import com.njdaeger.plotmanager.servicelibrary.transactional.ITransactionalService;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IUserService extends ITransactionalService {

    /**
     * Get all users
     * @return A result with a list of users if successful, or a result with an empty list if the user retrieval was unsuccessful, or no users found.
     */
    CompletableFuture<Result<List<User>>> getUsers();

    /**
     * Get a user by their uuid
     * @param userId The uuid of the user to get
     * @return A result with the user if successful, or a result with null if the user retrieval was unsuccessful, or no user found.
     */
    CompletableFuture<Result<User>> getUserByUuid(UUID userId);

    /**
     * Get a user by their username
     * @param username The username of the user to get
     * @return A result with the list of users last seen with that username if successful, or a result with an empty list if the user retrieval was unsuccessful, or no user found.
     */
    CompletableFuture<Result<List<User>>> getUserByName(String username);

    /**
     * Create a user
     * @param createdBy The creator of the user
     * @param newUserUuid The uuid of the user to create
     * @param newUserUsername The username of the user to create
     * @return A result with the created user if successful, or a result with null if the user creation was unsuccessful.
     */
    CompletableFuture<Result<User>> createUser(UUID createdBy, UUID newUserUuid, String newUserUsername);

    /**
     * Update a user's username
     * @param updatedBy The updater of the user
     * @param userId The uuid of the user to update
     * @param newUsername The new username of the user
     * @return A result with the updated user if successful, or a result with null if the user update was unsuccessful.
     */
    CompletableFuture<Result<User>> updateUsername(UUID updatedBy, UUID userId, String newUsername);

    /**
     * Update a user's uuid
     * @param updatedBy The updater of the user
     * @param oldUserId The old uuid of the user
     * @param newUserId The new uuid of the user
     * @return A result with the updated user if successful, or a result with null if the user update was unsuccessful.
     */
    CompletableFuture<Result<User>> updateUserUuid(UUID updatedBy, UUID oldUserId, UUID newUserId);

    /**
     * Delete a user
     * @param deletedBy The deleter of the user
     * @param uuidOfUserToDelete The uuid of the user to delete
     * @return A result with the deleted user if successful, or a result with null if the user deletion was unsuccessful.
     */
    CompletableFuture<Result<User>> deleteUser(UUID deletedBy, UUID uuidOfUserToDelete);

    /**
     * Get the system user
     * @return A result with the system user if successful, or a result with null if the user retrieval was unsuccessful, or no user found.
     */
    default CompletableFuture<Result<User>> getSystemUser() {
        return getUserByUuid(Util.SYSTEM_UUID);
    }

}
