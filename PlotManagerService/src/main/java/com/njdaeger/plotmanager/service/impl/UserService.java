package com.njdaeger.plotmanager.service.impl;

import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.plotmanager.dataaccess.repositories.IUserRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.service.IUserService;
import com.njdaeger.plotmanager.service.Result;
import com.njdaeger.plotmanager.service.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class UserService implements IUserService {

    private final IUnitOfWork uow;
    private final Map<UUID, User> userCache;

    public UserService(IUnitOfWork uow) {
        this.uow = uow;
        this.userCache = new HashMap<>();
    }

    @Override
    public CompletableFuture<Result<List<User>>> getUsers() {
        if (!userCache.isEmpty()) return CompletableFuture.completedFuture(Result.good(List.copyOf(userCache.values())));

        return uow.repo(IUserRepository.class).getUsers().thenApply(users -> {
            users.forEach(user -> {
                var uuid = UUID.fromString(user.getUuid());
                userCache.put(uuid, new User(user.getId(), uuid, user.getUsername()));
            });
            return Result.good(List.copyOf(userCache.values()));
        });
    }

    @Override
    public CompletableFuture<Result<User>> getUserByUuid(UUID userId) {
        if (userCache.containsKey(userId)) return CompletableFuture.completedFuture(Result.good(userCache.get(userId)));

        return uow.repo(IUserRepository.class).getUserByUuid(userId).thenApply(user -> {
            if (user == null) return Result.bad("User not found.");
            var uuid = UUID.fromString(user.getUuid());
            var newUser = new User(user.getId(), uuid, user.getUsername());
            userCache.put(uuid, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<List<User>>> getUserByName(String username) {
        if (userCache.values().stream().anyMatch(user -> user.getLastKnownName().equalsIgnoreCase(username))) {
            return CompletableFuture.completedFuture(Result.good(List.copyOf(userCache.values())));
        }

        return uow.repo(IUserRepository.class).getUsersByUsername(username).thenApply(users -> {
            users.forEach(user -> {
                var uuid = UUID.fromString(user.getUuid());
                userCache.put(uuid, new User(user.getId(), uuid, user.getUsername()));
            });
            return Result.good(List.copyOf(userCache.values()));
        });
    }

    @Override
    public CompletableFuture<Result<User>> createUser(UUID createdBy, UUID newUserUuid, String newUserUsername) {
        if (userCache.containsKey(newUserUuid)) return CompletableFuture.completedFuture(Result.bad("User already exists."));

        var createdById = await(getUserByUuid(createdBy)).getOr(User::getId, -1);
        if (createdById == -1) {
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with the uuid " + createdBy + "."));
        }

        return uow.repo(IUserRepository.class).insertUser(createdById, newUserUuid, newUserUsername).thenApply(user -> {
            if (user == null) return Result.bad("Failed to create user.");
            var newUser = new User(user.getId(), newUserUuid, user.getUsername());
            userCache.put(newUserUuid, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<User>> updateUsername(UUID updatedBy, UUID userId, String newUsername) {
        var oldUser = await(getUserByUuid(updatedBy)).getOr(null);
        if (oldUser == null) return CompletableFuture.completedFuture(Result.bad("User does not exist."));

        var updatedById = await(getUserByUuid(updatedBy)).getOr(User::getId, -1);
        if (updatedById == -1) {
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with the uuid " + updatedBy + "."));
        }

        return uow.repo(IUserRepository.class).updateUser(updatedById, oldUser.getId(), null, newUsername).thenApply(user -> {
            if (user == null) return Result.bad("Failed to update user.");
            var newUser = new User(user.getId(), userId, user.getUsername());
            userCache.put(userId, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<User>> updateUserUuid(UUID updatedBy, UUID oldUserId, UUID newUserId) {
        var oldUser = await(getUserByUuid(updatedBy)).getOr(null);
        if (oldUser == null) return CompletableFuture.completedFuture(Result.bad("User does not exist."));

        var updatedById = await(getUserByUuid(updatedBy)).getOr(User::getId, -1);
        if (updatedById == -1) {
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with the uuid " + updatedBy + "."));
        }

        return uow.repo(IUserRepository.class).updateUser(updatedById, oldUser.getId(), newUserId, null).thenApply(user -> {
            if (user == null) return Result.bad("Failed to update user.");
            var newUser = new User(user.getId(), newUserId, user.getUsername());
            userCache.put(newUserId, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<User>> deleteUser(UUID deletedBy, UUID uuidOfUserToDelete) {
        var oldUser = await(getUserByUuid(deletedBy)).getOr(null);
        if (oldUser == null) return CompletableFuture.completedFuture(Result.bad("User does not exist."));

        var deletedById = await(getUserByUuid(deletedBy)).getOr(User::getId, -1);
        if (deletedById == -1) {
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with the uuid " + deletedBy + "."));
        }

        return uow.repo(IUserRepository.class).deleteUser(deletedById, oldUser.getId()).thenApply(user -> {
            if (user == -1) return Result.bad("Failed to delete user.");
            userCache.remove(uuidOfUserToDelete);
            return Result.good(oldUser);
        });
    }
}
