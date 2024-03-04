package com.njdaeger.taskmanager.dataaccess.repositories.implementations;

import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.taskmanager.dataaccess.IProcedure;
import com.njdaeger.taskmanager.dataaccess.models.AttributeEntity;
import com.njdaeger.taskmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.taskmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import com.njdaeger.taskmanager.dataaccess.Util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AttributeRepository implements IAttributeRepository {

    private final IPluginLogger logger;
    private final AbstractDatabaseTransaction<?> transaction;
    private final IProcedure procedures;

    public AttributeRepository(IPluginLogger logger, IProcedure procedures, AbstractDatabaseTransaction<?> transaction) {
        this.logger = logger;
        this.transaction = transaction;
        this.procedures = procedures;
        Util.await(initializeRepository());
    }

    @Override
    public CompletableFuture<Boolean> initializeRepository() {
        return CompletableFuture.supplyAsync(() -> true);
    }

    @Override
    public CompletableFuture<List<AttributeEntity>> getAttributes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectAttributes();
                return transaction.query(proc.getFirst(), AttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<List<AttributeEntity>> getAttributesByTaskType(int taskTypeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectAttributesByTaskType(taskTypeId);
                return transaction.query(proc.getFirst(), proc.getSecond(), AttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<AttributeEntity> getAttributeByName(String name, int taskTypeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectAttributeByName(name, taskTypeId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), AttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<AttributeEntity> getAttributeById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectAttributeById(id);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), AttributeEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<AttributeEntity> insertAttribute(int createdBy, int taskTypeId, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertAttribute(createdBy, taskTypeId, name);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getAttributeById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<AttributeEntity> updateAttribute(int modifiedBy, int attributeId, Integer taskTypeId, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updateAttribute(modifiedBy, attributeId, taskTypeId, name);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getAttributeById(attributeId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }
}
