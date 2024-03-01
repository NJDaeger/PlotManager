package com.njdaeger.taskmanager.dataaccess.repositories;

import com.njdaeger.taskmanager.dataaccess.IRepository;
import com.njdaeger.taskmanager.dataaccess.models.UserEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the interactions with the following tables:
 * * User
 */
public interface IUserRepository extends IRepository {

    /**
     * Get all users from the database.
     * @return A list of all users in the database. Empty if no users were found.
     */
    CompletableFuture<List<UserEntity>> getUsers();

    /**
     * Get a user by their id.
     * @param id The id of the user.
     * @return The user with the given id. Null if no user was found.
     */
    CompletableFuture<UserEntity> getUserById(int id);

    /**
     * Get a user by their uuid.
     * @param uuid The uuid of the user.
     * @return The user with the given uuid. Null if no user was found.
     */
    CompletableFuture<UserEntity> getUserByUuid(UUID uuid);

    /**
     * Get a user by their username.
     * @param username The username of the user.
     * @return The user with the given username. (Might be multiple users due to name changes) Empty if no users were found.
     */
    CompletableFuture<List<UserEntity>> getUsersByUsername(String username);

    /**
     * Insert a new user into the database.
     * @param createdBy The id of the user who created this user.
     * @param userId The uuid of the user.
     * @param username The username of the user.
     * @return The newly created user, or null if the user could not be created.
     */
    CompletableFuture<UserEntity> insertUser(int createdBy, UUID userId, String username);

    /**
     * Update a user's username or uuid.
     * @param modifiedBy The id of the user who modified this user.
     * @param userId The id of the user to update.
     * @param userUniqueId The new uuid of the user.
     * @param username The new username of the user.
     * @return The updated user, or null if the user could not be updated.
     */
    CompletableFuture<UserEntity> updateUser(int modifiedBy, int userId, UUID userUniqueId, String username);

    /**
     * Delete a user from the database.
     * @param deletedBy The id of the user who deleted this user.
     * @param userId The id of the user to delete.
     * @return The id of the deleted user, or -1 if no user was deleted.
     */
    CompletableFuture<Integer> deleteUser(int deletedBy, int userId);

}
