package com.njdaeger.taskmanager.servicelibrary.services.implementations;

import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.taskmanager.dataaccess.repositories.IUserRepository;
import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.User;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.IUserService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class UserService implements IUserService {

    private final IServiceTransaction transaction;
    private final ICacheService cacheService;
    private final IPluginLogger logger;

    public UserService(IServiceTransaction transaction, ICacheService cacheService, IPluginLogger logger) {
        this.transaction = transaction;
        this.cacheService = cacheService;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<Result<List<User>>> getUsers() {
        transaction.use();

        return Util.async(() -> {
            if (!cacheService.getUserCache().isEmpty()) return Result.good(List.copyOf(cacheService.getUserCache().values()));

            return await(transaction.getUnitOfWork().repo(IUserRepository.class).getUsers().thenApply(users -> {
                users.forEach(user -> {
                    var uuid = UUID.fromString(user.getUuid());
                    cacheService.getUserCache().put(uuid, new User(user.getId(), uuid, user.getUsername()));
                });
                return Result.good(List.copyOf(cacheService.getUserCache().values()));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<User>> getUserById(int id) {
        transaction.use();

        return Util.<Result<User>>async(() -> {
            if (cacheService.getUserCache().values().stream().anyMatch(user -> user.getId() == id)) return Result.good(cacheService.getUserCache().values().stream().filter(user -> user.getId() == id).findFirst().orElse(null));

            return await(transaction.getUnitOfWork().repo(IUserRepository.class).getUserById(id).thenApply(user -> {
                if (user == null) return Result.bad("User not found.");
                var uuid = UUID.fromString(user.getUuid());
                var newUser = new User(user.getId(), uuid, user.getUsername());
                cacheService.getUserCache().put(uuid, newUser);
                return Result.good(newUser);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<User>> getUserByUuid(UUID userId) {
        transaction.use();

        return Util.<Result<User>>async(() -> {
            if (cacheService.getUserCache().containsKey(userId)) return Result.good(cacheService.getUserCache().get(userId));

            return await(transaction.getUnitOfWork().repo(IUserRepository.class).getUserByUuid(userId).thenApply(user -> {
                if (user == null) return Result.bad("User not found.");
                var uuid = UUID.fromString(user.getUuid());
                var newUser = new User(user.getId(), uuid, user.getUsername());
                cacheService.getUserCache().put(uuid, newUser);
                return Result.good(newUser);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<List<User>>> getUserByName(String username) {
        transaction.use();

        return Util.async(() -> {
            if (cacheService.getUserCache().values().stream().anyMatch(user -> user.getLastKnownName().equalsIgnoreCase(username))) return Result.good(List.copyOf(cacheService.getUserCache().values().stream().filter(user -> user.getLastKnownName().equalsIgnoreCase(username)).toList()));

            return await(transaction.getUnitOfWork().repo(IUserRepository.class).getUsersByUsername(username).thenApply(users -> {
                users.forEach(user -> {
                    var uuid = UUID.fromString(user.getUuid());
                    cacheService.getUserCache().put(uuid, new User(user.getId(), uuid, user.getUsername()));
                });
                return Result.good(List.copyOf(cacheService.getUserCache().values()));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<User>> createUser(UUID createdBy, UUID newUserUuid, String newUserUsername) {
        transaction.use();

        return Util.<Result<User>>async(() -> {
            if (cacheService.getUserCache().containsKey(newUserUuid)) return Result.bad("User already exists.");

            var createdById = await(getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (createdById == -1) return Result.bad("Failed to find user with the uuid " + createdBy + ".");

            return await(transaction.getUnitOfWork().repo(IUserRepository.class).insertUser(createdById, newUserUuid, newUserUsername).thenApply(user -> {
                if (user == null) return Result.bad("Failed to create user.");
                var newUser = new User(user.getId(), newUserUuid, user.getUsername());
                cacheService.getUserCache().put(newUserUuid, newUser);
                return Result.good(newUser);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<User>> updateUsername(UUID updatedBy, UUID userId, String newUsername) {
        transaction.use();

        return Util.<Result<User>>async(() -> {
            var oldUser = await(getUserByUuid(updatedBy)).getOr(null);
            if (oldUser == null) return Result.bad("User does not exist.");

            var updatedById = await(getUserByUuid(updatedBy)).getOr(User::getId, -1);
            if (updatedById == -1) return Result.bad("Failed to find user with the uuid " + updatedBy + ".");

            return await(transaction.getUnitOfWork().repo(IUserRepository.class).updateUser(updatedById, oldUser.getId(), null, newUsername).thenApply(user -> {
                if (user == null) return Result.bad("Failed to update user.");
                var newUser = new User(user.getId(), userId, user.getUsername());
                cacheService.getUserCache().put(userId, newUser);
                return Result.good(newUser);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<User>> updateUserUuid(UUID updatedBy, UUID oldUserId, UUID newUserId) {
        transaction.use();

        return Util.<Result<User>>async(() -> {
            var oldUser = await(getUserByUuid(updatedBy)).getOr(null);
            if (oldUser == null) return Result.bad("User does not exist.");

            var updatedById = await(getUserByUuid(updatedBy)).getOr(User::getId, -1);
            if (updatedById == -1) return Result.bad("Failed to find user with the uuid " + updatedBy + ".");

            return await(transaction.getUnitOfWork().repo(IUserRepository.class).updateUser(updatedById, oldUser.getId(), newUserId, null).thenApply(user -> {
                if (user == null) return Result.bad("Failed to update user.");
                var newUser = new User(user.getId(), newUserId, user.getUsername());
                cacheService.getUserCache().put(newUserId, newUser);
                return Result.good(newUser);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<User>> deleteUser(UUID deletedBy, UUID uuidOfUserToDelete) {
        transaction.use();

        return Util.<Result<User>>async(() -> {
            var oldUser = await(getUserByUuid(deletedBy)).getOr(null);
            if (oldUser == null) return Result.bad("User does not exist.");

            var deletedById = await(getUserByUuid(deletedBy)).getOr(User::getId, -1);
            if (deletedById == -1) return Result.bad("Failed to find user with the uuid " + deletedBy + ".");

            return await(transaction.getUnitOfWork().repo(IUserRepository.class).deleteUser(deletedById, oldUser.getId()).thenApply(user -> {
                if (user == -1) return Result.bad("Failed to delete user.");
                cacheService.getUserCache().remove(uuidOfUserToDelete);
                return Result.good(oldUser);
            }));
        }).whenComplete(finishTransaction());
    }

    private <T extends Result<?>> BiConsumer<? super T, ? super Throwable> finishTransaction() {
        return (r, t) -> {
            if (t != null) logger.exception(new Exception(t));
            if (!r.successful()) transaction.abort();
            transaction.release();
        };
    }

    @Override
    public void clearCache() {
        cacheService.getUserCache().clear();
    }

    @Override
    public CompletableFuture<Void> initializeCache() {
        transaction.use();

        return transaction.getUnitOfWork().repo(IUserRepository.class).getUsers().thenApply(users -> {
            users.forEach(user -> {
                var uuid = UUID.fromString(user.getUuid());
                cacheService.getUserCache().put(uuid, new User(user.getId(), uuid, user.getUsername()));
            });
            return Result.good(List.copyOf(cacheService.getUserCache().values()));
        }).whenComplete(finishTransaction()).thenAccept(r -> {});
    }
}
