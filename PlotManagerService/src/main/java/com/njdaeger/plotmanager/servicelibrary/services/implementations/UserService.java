package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.repositories.IUserRepository;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class UserService implements IUserService {

    private final IServiceTransaction transaction;
    private final ICacheService cacheService;

    public UserService(IServiceTransaction transaction, ICacheService cacheService) {
        this.transaction = transaction;
        this.cacheService = cacheService;
    }

    @Override
    public CompletableFuture<Result<List<User>>> getUsers() {
        transaction.use();
        if (!cacheService.getUserCache().isEmpty()) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(List.copyOf(cacheService.getUserCache().values())));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).getUsers().thenApply(users -> {
            transaction.release();
            users.forEach(user -> {
                var uuid = UUID.fromString(user.getUuid());
                cacheService.getUserCache().put(uuid, new User(user.getId(), uuid, user.getUsername()));
            });
            return Result.good(List.copyOf(cacheService.getUserCache().values()));
        });
    }

    @Override
    public CompletableFuture<Result<User>> getUserByUuid(UUID userId) {
        transaction.use();
        if (cacheService.getUserCache().containsKey(userId)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(cacheService.getUserCache().get(userId)));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).getUserByUuid(userId).thenApply(user -> {
            transaction.release();
            if (user == null) return Result.bad("User not found.");
            var uuid = UUID.fromString(user.getUuid());
            var newUser = new User(user.getId(), uuid, user.getUsername());
            cacheService.getUserCache().put(uuid, newUser);
            return Result.good(newUser);
        });
    }

    @Override
    public CompletableFuture<Result<List<User>>> getUserByName(String username) {
        transaction.use();
        if (cacheService.getUserCache().values().stream().anyMatch(user -> user.getLastKnownName().equalsIgnoreCase(username))) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(List.copyOf(cacheService.getUserCache().values())));
        }

        return transaction.getUnitOfWork().repo(IUserRepository.class).getUsersByUsername(username).thenApply(users -> {
            transaction.release();
            users.forEach(user -> {
                var uuid = UUID.fromString(user.getUuid());
                cacheService.getUserCache().put(uuid, new User(user.getId(), uuid, user.getUsername()));
            });
            return Result.good(List.copyOf(cacheService.getUserCache().values()));
        });
    }

    @Override
    public CompletableFuture<Result<User>> createUser(UUID createdBy, UUID newUserUuid, String newUserUsername) {
        transaction.use();
        if (cacheService.getUserCache().containsKey(newUserUuid)) {
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
            cacheService.getUserCache().put(newUserUuid, newUser);
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
            cacheService.getUserCache().put(userId, newUser);
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
            cacheService.getUserCache().put(newUserId, newUser);
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
            cacheService.getUserCache().remove(uuidOfUserToDelete);
            return Result.good(oldUser);
        });
    }
}
