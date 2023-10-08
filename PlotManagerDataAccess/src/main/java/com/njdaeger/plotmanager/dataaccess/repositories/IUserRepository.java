package com.njdaeger.plotmanager.dataaccess.repositories;

import com.njdaeger.plotmanager.dataaccess.IRepository;
import com.njdaeger.plotmanager.dataaccess.models.UserEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IUserRepository extends IRepository {

    /**
     * Get all users from the database.
     * @return A list of all users in the database.
     */
    CompletableFuture<List<UserEntity>> getUsers();

    /**
     * Get a user by their id.
     * @param id The id of the user.
     * @return The user with the given id.
     */
    CompletableFuture<UserEntity> getUserById(int id);

    /**
     * Get a user by their uuid.
     * @param uuid The uuid of the user.
     * @return The user with the given uuid.
     */
    CompletableFuture<UserEntity> getUserByUuid(UUID uuid);

    /**
     * Get a user by their username.
     * @param username The username of the user.
     * @return The user with the given username. (Might be multiple users due to name changes)
     */
    CompletableFuture<List<UserEntity>> getUsersByUsername(String username);

    CompletableFuture<UserEntity> insertUser(int createdBy, UUID userId, String username);

    CompletableFuture<UserEntity> updateUser(int modifiedBy, int userId, UUID userUniqueId, String username);

    CompletableFuture<Integer> deleteUser(int deletedBy, int userId);

}
