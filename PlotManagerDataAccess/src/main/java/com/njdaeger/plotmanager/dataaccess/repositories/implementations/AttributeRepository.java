package com.njdaeger.plotmanager.dataaccess.repositories.implementations;

import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.models.AttributeEntity;
import com.njdaeger.plotmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.ITransaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class AttributeRepository implements IAttributeRepository {

    private final IPluginLogger logger;
    private final ITransaction<?> transaction;
    private final IProcedure procedures;

    public AttributeRepository(IPluginLogger logger, IProcedure procedures, ITransaction<?> transaction) {
        this.logger = logger;
        this.transaction = transaction;
        this.procedures = procedures;
        await(initializeRepository());
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
    public CompletableFuture<AttributeEntity> getAttributeByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectAttributeByName(name);
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
    public CompletableFuture<AttributeEntity> insertAttribute(int createdBy, String name, String type) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertAttribute(createdBy, name, type);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return await(getAttributeById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<AttributeEntity> updateAttribute(int modifiedBy, int attributeId, String name, String type) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updateAttribute(modifiedBy, attributeId, name, type);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return await(getAttributeById(attributeId));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteAttribute(int deletedBy, int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deleteAttribute(deletedBy, id);
                return transaction.execute(proc.getFirst(), proc.getSecond());
            } catch (Exception e) {
                logger.exception(e);
                return -1;
            }
        });
    }
}
