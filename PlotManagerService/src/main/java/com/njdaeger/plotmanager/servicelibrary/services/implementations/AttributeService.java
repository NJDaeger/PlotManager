package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class AttributeService implements IAttributeService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final IConfigService configService;
    private final ICacheService cacheService;

    public AttributeService(IServiceTransaction transaction, IUserService userService, IConfigService configService, ICacheService cacheService) {
        this.transaction = transaction;
        this.userService = userService;
        this.configService = configService;
        this.cacheService = cacheService;
    }

    @Override
    public CompletableFuture<Result<List<Attribute>>> getAttributes() {
        transaction.use();
        if (!cacheService.getAttributeCache().isEmpty()) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(List.copyOf(cacheService.getAttributeCache().values())));
        }

        return transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributes().thenApply(attributes -> {
            transaction.release();
            attributes.forEach(attribute -> cacheService.getAttributeCache().put(attribute.getName(), new Attribute(attribute.getId(), attribute.getName(), attribute.getType())));
            return Result.good(List.copyOf(cacheService.getAttributeCache().values()));
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> getAttribute(String name) {
        transaction.use();
        if (name == null || name.isBlank()) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Attribute name cannot be null or blank."));
        }
        if (cacheService.getAttributeCache().containsKey(name)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(cacheService.getAttributeCache().get(name)));
        }
        return transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributeByName(name).thenApply(attribute -> {
            transaction.release();
            if (attribute == null) return Result.bad("Failed to find attribute with name " + name + ".");
            cacheService.getAttributeCache().put(name, new Attribute(attribute.getId(), attribute.getName(), attribute.getType()));
            return Result.good(cacheService.getAttributeCache().get(name));
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> getAttributeById(int id) {
        transaction.use();
        if (id < 0) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Attribute id cannot be less than 0."));
        }

        var attribute = cacheService.getAttributeCache().values().stream().filter(attr -> attr.getId() == id).findFirst().orElse(null);
        if (attribute != null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.good(attribute));
        }

        return transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributeById(id).thenApply(attr -> {
            transaction.release();
            if (attr == null) return Result.bad("Failed to find attribute with id " + id + ".");
            cacheService.getAttributeCache().put(attr.getName(), new Attribute(attr.getId(), attr.getName(), attr.getType()));
            return Result.good(cacheService.getAttributeCache().get(attr.getName()));
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> createAttribute(UUID createdBy, String name, String type) {
        transaction.use();
        if (createdBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Creator cannot be null."));
        }
        if (name == null || name.isBlank()) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Attribute name cannot be null or blank."));
        }
        if (type == null || type.isBlank()) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Attribute type cannot be null or blank."));
        }
        if (cacheService.getAttributeCache().containsKey(name)) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name already exists."));
        }

        var attributeType = configService.getAttributeType(type);
        if (attributeType == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("An attribute type with that name does not exist."));
        }

        var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + createdBy + "."));
        }

        return transaction.getUnitOfWork().repo(IAttributeRepository.class).insertAttribute(userId, name, type).thenApply(success -> {
            if (success == null) {
                transaction.release();
                transaction.abort();
                return Result.bad("Failed to insert attribute.");
            }
            transaction.release();
            var attribute = new Attribute(success.getId(), name, type);
            cacheService.getAttributeCache().put(name, attribute);
            return Result.good(attribute);
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttributeName(UUID modifiedBy, String oldName, String newName) {
        transaction.use();
        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }
        var oldAttribute = cacheService.getAttributeCache().get(oldName);
        if (oldAttribute == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IAttributeRepository.class).updateAttribute(userId, oldAttribute.getId(), newName, null).thenApply(success -> {
            if (success == null) {
                transaction.release();
                transaction.abort();
                return Result.bad("Failed to update attribute.");
            }
            transaction.release();
            var attribute = new Attribute(success.getId(), newName, success.getType());
            cacheService.getAttributeCache().remove(oldName);
            cacheService.getAttributeCache().put(newName, attribute);
            return Result.good(attribute);
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttributeType(UUID modifiedBy, String name, String newType) {
        transaction.use();
        if (modifiedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Modifier cannot be null."));
        }
        var oldAttribute = cacheService.getAttributeCache().get(name);
        if (oldAttribute == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IAttributeRepository.class).updateAttribute(userId, oldAttribute.getId(), null, newType).thenApply(success -> {
            if (success == null) {
                transaction.release();
                transaction.abort();
                return Result.bad("Failed to update attribute.");
            }
            transaction.release();
            var attribute = new Attribute(success.getId(), success.getName(), newType);
            cacheService.getAttributeCache().put(name, attribute);
            return Result.good(attribute);
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> deleteAttribute(UUID deletedBy, String name) {
        transaction.use();
        if (deletedBy == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Deleter cannot be null."));
        }
        var oldAttribute = cacheService.getAttributeCache().get(name);
        if (oldAttribute == null) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            transaction.release();
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + deletedBy + "."));
        }

        return transaction.getUnitOfWork().repo(IAttributeRepository.class).deleteAttribute(userId, oldAttribute.getId()).thenApply(success -> {
            if (success == -1) {
                transaction.release();
                transaction.abort();
                return Result.bad("Failed to delete attribute.");
            }
            transaction.release();
            cacheService.getAttributeCache().remove(name);
            return Result.good(oldAttribute);
        });
    }

    @Override
    public void clearCache() {
        cacheService.getAttributeCache().clear();
    }

    @Override
    public CompletableFuture<Void> initializeCache() {
        transaction.use();

        return transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributes().thenApply(attributes -> {
            transaction.release();
            attributes.forEach(attribute -> cacheService.getAttributeCache().put(attribute.getName(), new Attribute(attribute.getId(), attribute.getName(), attribute.getType())));
            return Result.good(List.copyOf(cacheService.getAttributeCache().values()));
        }).thenAccept((r) -> {});
    }
}
