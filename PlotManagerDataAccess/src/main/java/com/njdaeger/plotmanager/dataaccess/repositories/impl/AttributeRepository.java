package com.njdaeger.plotmanager.dataaccess.repositories.impl;

import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.models.AttributeEntity;
import com.njdaeger.plotmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.ITransaction;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class AttributeRepository implements IAttributeRepository {

    private final IExceptionPublisher exceptionPublisher;
    private final ITransaction<?> transaction;
    private final IProcedure procedures;

    public AttributeRepository(IExceptionPublisher exceptionPublisher, IProcedure procedures, ITransaction<?> transaction) {
        this.exceptionPublisher = exceptionPublisher;
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
                exceptionPublisher.publishException(e);
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
                exceptionPublisher.publishException(e);
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
                exceptionPublisher.publishException(e);
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
                exceptionPublisher.publishException(e);
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
                exceptionPublisher.publishException(e);
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
                exceptionPublisher.publishException(e);
                return -1;
            }
        });
    }
}
