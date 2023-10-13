package com.njdaeger.plotmanager.dataaccess.repositories.implementations;

import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.models.UserEntity;
import com.njdaeger.plotmanager.dataaccess.repositories.IUserRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.AbstractDatabaseTransaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class UserRepository implements IUserRepository {

    private final IPluginLogger logger;
    private final AbstractDatabaseTransaction<?> transaction;
    private final IProcedure procedures;

    public UserRepository(IPluginLogger logger, IProcedure procedures, AbstractDatabaseTransaction<?> transaction) {
        this.logger = logger;
        this.transaction = transaction;
        this.procedures = procedures;
        await(initializeRepository());
    }

    @Override
    public CompletableFuture<Boolean> initializeRepository() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<List<UserEntity>> getUsers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectUsers();
                return transaction.query(proc.getFirst(), UserEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<UserEntity> getUserById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectUserById(id);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), UserEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<UserEntity> getUserByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectUserByUuid(uuid);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), UserEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<List<UserEntity>> getUsersByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectUsersByUsername(username);
                return transaction.query(proc.getFirst(), UserEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<UserEntity> insertUser(int createdBy, UUID userId, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertUser(createdBy, userId, username);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return await(getUserById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<UserEntity> updateUser(int modifiedBy, int userId, UUID userUniqueId, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updateUser(modifiedBy, userId, userUniqueId, username);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return await(getUserById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteUser(int deletedBy, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deleteUser(deletedBy, userId);
                return transaction.execute(proc.getFirst(), proc.getSecond());
            } catch (Exception e) {
                logger.exception(e);
                return -1;
            }
        });
    }
}
