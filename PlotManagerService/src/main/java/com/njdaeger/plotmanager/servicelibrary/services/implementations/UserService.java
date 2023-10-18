package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.repositories.IUserRepository;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class UserService implements IUserService {

    private final IServiceTransaction transaction;
    private static final Map<UUID, User> userCache = new ConcurrentHashMap<>();

    public UserService(IServiceTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public CompletableFuture<Result<List<User>>> getUsers() {
        transaction.use();
        if (!userCache.isEmpty()) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(List.copyOf(userCache.values())));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).getUsers().thenApply(users -> {
            transaction.release();
            users.forEach(user -> {
                var uuid = UUID.fromString(user.getUuid());
                userCache.put(uuid, new User(user.getId(), uuid, user.getUsername()));
            });
            return Result.good(List.copyOf(userCache.values()));
        });
    }

    @Override
    public CompletableFuture<Result<User>> getUserByUuid(UUID userId) {
        transaction.use();
        if (userCache.containsKey(userId)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(userCache.get(userId)));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).getUserByUuid(userId).thenApply(user -> {
            transaction.release();
            if (user == null) return Result.bad("User not found.");
            var uuid = UUID.fromString(user.getUuid());
            var newUser = new User(user.getId(), uuid, user.getUsername());
            userCache.put(uuid, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<List<User>>> getUserByName(String username) {
        transaction.use();
        if (userCache.values().stream().anyMatch(user -> user.getLastKnownName().equalsIgnoreCase(username))) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(List.copyOf(userCache.values())));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).getUsersByUsername(username).thenApply(users -> {
            transaction.release();
            users.forEach(user -> {
                var uuid = UUID.fromString(user.getUuid());
                userCache.put(uuid, new User(user.getId(), uuid, user.getUsername()));
            });
            return Result.good(List.copyOf(userCache.values()));
        });
    }

    @Override
    public CompletableFuture<Result<User>> createUser(UUID createdBy, UUID newUserUuid, String newUserUsername) {
        transaction.use();
        if (userCache.containsKey(newUserUuid)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("User already exists."));
        }

        var createdById = await(getUserByUuid(createdBy)).getOr(User::getId, -1);
        if (createdById == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with the uuid " + createdBy + "."));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).insertUser(createdById, newUserUuid, newUserUsername).thenApply(user -> {
            transaction.release();
            if (user == null) return Result.bad("Failed to create user.");
            var newUser = new User(user.getId(), newUserUuid, user.getUsername());
            userCache.put(newUserUuid, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<User>> updateUsername(UUID updatedBy, UUID userId, String newUsername) {
        transaction.use();
        var oldUser = await(getUserByUuid(updatedBy)).getOr(null);
        if (oldUser == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("User does not exist."));
        }

        var updatedById = await(getUserByUuid(updatedBy)).getOr(User::getId, -1);
        if (updatedById == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with the uuid " + updatedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).updateUser(updatedById, oldUser.getId(), null, newUsername).thenApply(user -> {
            transaction.release();
            if (user == null) return Result.bad("Failed to update user.");
            var newUser = new User(user.getId(), userId, user.getUsername());
            userCache.put(userId, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<User>> updateUserUuid(UUID updatedBy, UUID oldUserId, UUID newUserId) {
        transaction.use();
        var oldUser = await(getUserByUuid(updatedBy)).getOr(null);
        if (oldUser == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("User does not exist."));
        }

        var updatedById = await(getUserByUuid(updatedBy)).getOr(User::getId, -1);
        if (updatedById == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with the uuid " + updatedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).updateUser(updatedById, oldUser.getId(), newUserId, null).thenApply(user -> {
            transaction.release();
            if (user == null) return Result.bad("Failed to update user.");
            var newUser = new User(user.getId(), newUserId, user.getUsername());
            userCache.put(newUserId, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<User>> deleteUser(UUID deletedBy, UUID uuidOfUserToDelete) {
        transaction.use();
        var oldUser = await(getUserByUuid(deletedBy)).getOr(null);
        if (oldUser == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("User does not exist."));
        }

        var deletedById = await(getUserByUuid(deletedBy)).getOr(User::getId, -1);
        if (deletedById == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with the uuid " + deletedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).deleteUser(deletedById, oldUser.getId()).thenApply(user -> {
            transaction.release();
            if (user == -1) return Result.bad("Failed to delete user.");
            userCache.remove(uuidOfUserToDelete);
            return Result.good(oldUser);
        });
    }
}
