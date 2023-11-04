package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.Util;
import com.njdaeger.plotmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class AttributeService implements IAttributeService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final IConfigService configService;
    private final ICacheService cacheService;
    private final IPluginLogger logger;

    public AttributeService(IServiceTransaction transaction, IUserService userService, IConfigService configService, ICacheService cacheService, IPluginLogger logger) {
        this.transaction = transaction;
        this.userService = userService;
        this.configService = configService;
        this.cacheService = cacheService;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<Result<List<Attribute>>> getAttributes() {
        transaction.use();

        return Util.async(() -> {
            if (!cacheService.getAttributeCache().isEmpty()) return Result.good(List.copyOf(cacheService.getAttributeCache().values()));

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributes().thenApply(attributes -> {
                attributes.forEach(attribute -> cacheService.getAttributeCache().put(attribute.getName(), new Attribute(attribute.getId(), attribute.getName(), attribute.getType())));
                return Result.good(List.copyOf(cacheService.getAttributeCache().values()));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> getAttribute(String name) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (name == null || name.isBlank()) return Result.bad("Attribute name cannot be null or blank.");

            if (cacheService.getAttributeCache().containsKey(name)) return Result.good(cacheService.getAttributeCache().get(name));

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributeByName(name).thenApply(attribute -> {
                if (attribute == null) return Result.bad("Failed to find attribute with name " + name + ".");
                cacheService.getAttributeCache().put(name, new Attribute(attribute.getId(), attribute.getName(), attribute.getType()));
                return Result.good(cacheService.getAttributeCache().get(name));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> getAttributeById(int id) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (id < 0) return Result.bad("Attribute id cannot be less than 0.");

            var attribute = cacheService.getAttributeCache().values().stream().filter(attr -> attr.getId() == id).findFirst().orElse(null);
            if (attribute != null) return Result.good(attribute);

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributeById(id).thenApply(attr -> {
                if (attr == null) return Result.bad("Failed to find attribute with id " + id + ".");
                cacheService.getAttributeCache().put(attr.getName(), new Attribute(attr.getId(), attr.getName(), attr.getType()));
                return Result.good(cacheService.getAttributeCache().get(attr.getName()));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> createAttribute(UUID createdBy, String name, String type) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (createdBy == null) return Result.bad("Creator cannot be null.");
            if (name == null || name.isBlank()) return Result.bad("Attribute name cannot be null or blank.");
            if (type == null || type.isBlank()) return Result.bad("Attribute type cannot be null or blank.");
            if (cacheService.getAttributeCache().containsKey(name)) return Result.bad("An attribute with that name already exists.");

            var attributeType = configService.getAttributeType(type);
            if (attributeType == null) return Result.bad("An attribute type with that name does not exist.");

            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + createdBy + ".");

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).insertAttribute(userId, name, type).thenApply(success -> {
                if (success == null) return Result.bad("Failed to insert attribute.");
                var attribute = new Attribute(success.getId(), name, type);
                cacheService.getAttributeCache().put(name, attribute);
                return Result.good(attribute);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttributeName(UUID modifiedBy, String oldName, String newName) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");
            if (oldName == null || oldName.isBlank()) return Result.bad("Old attribute name cannot be null or blank.");
            if (newName == null || newName.isBlank()) return Result.bad("New attribute name cannot be null or blank.");
            if (cacheService.getAttributeCache().containsKey(newName)) return Result.bad("An attribute with that name already exists.");
            if (!cacheService.getAttributeCache().containsKey(oldName)) return Result.bad("An attribute with that name does not exist.");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).updateAttribute(userId, cacheService.getAttributeCache().get(oldName).getId(), newName, null).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update attribute.");
                var attribute = new Attribute(success.getId(), newName, success.getType());
                cacheService.getAttributeCache().remove(oldName);
                cacheService.getAttributeCache().put(newName, attribute);
                return Result.good(attribute);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttributeType(UUID modifiedBy, String name, String newType) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");
            if (name == null || name.isBlank()) return Result.bad("Attribute name cannot be null or blank.");
            if (newType == null || newType.isBlank()) return Result.bad("Attribute type cannot be null or blank.");
            if (!cacheService.getAttributeCache().containsKey(name)) return Result.bad("An attribute with that name does not exist.");

            var attributeType = configService.getAttributeType(newType);
            if (attributeType == null) return Result.bad("An attribute type with that name does not exist.");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).updateAttribute(userId, cacheService.getAttributeCache().get(name).getId(), null, newType).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update attribute.");
                var attribute = new Attribute(success.getId(), success.getName(), newType);
                cacheService.getAttributeCache().put(name, attribute);
                return Result.good(attribute);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> deleteAttribute(UUID deletedBy, String name) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (deletedBy == null) return Result.bad("Deleter cannot be null.");
            if (name == null || name.isBlank()) return Result.bad("Attribute name cannot be null or blank.");
            if (!cacheService.getAttributeCache().containsKey(name)) return Result.bad("An attribute with that name does not exist.");

            var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + deletedBy + ".");

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).deleteAttribute(userId, cacheService.getAttributeCache().get(name).getId()).thenApply(success -> {
                if (success == -1) return Result.bad("Failed to delete attribute.");
                var attribute = cacheService.getAttributeCache().get(name);
                cacheService.getAttributeCache().remove(name);
                return Result.good(attribute);
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
        cacheService.getAttributeCache().clear();
    }

    @Override
    public CompletableFuture<Void> initializeCache() {
        transaction.use();

        return transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributes().thenApply(attributes -> {
            attributes.forEach(attribute -> cacheService.getAttributeCache().put(attribute.getName(), new Attribute(attribute.getId(), attribute.getName(), attribute.getType())));
            return Result.good(List.copyOf(cacheService.getAttributeCache().values()));
        }).whenComplete(finishTransaction()).thenAccept((r) -> {});
    }
}
