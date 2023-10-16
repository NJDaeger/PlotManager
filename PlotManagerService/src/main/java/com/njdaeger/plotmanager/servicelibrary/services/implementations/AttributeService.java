package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.Result;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class AttributeService implements IAttributeService {

    private final IUnitOfWork uow;
    private final IUserService userService;
    private final IConfigService configService;
    private static final Map<String, Attribute> attributeCache = new ConcurrentHashMap<>();

    public AttributeService(IUnitOfWork uow, IUserService userService, IConfigService configService) {
        this.uow = uow;
        this.userService = userService;
        this.configService = configService;
    }

    @Override
    public CompletableFuture<Result<List<Attribute>>> getAttributes() {
        if (!attributeCache.isEmpty()) {
            return CompletableFuture.completedFuture(Result.good(List.copyOf(attributeCache.values())));
        }

        return uow.repo(IAttributeRepository.class).getAttributes().thenApply(attributes -> {
            attributes.forEach(attribute -> attributeCache.put(attribute.getName(), new Attribute(attribute.getId(), attribute.getName(), attribute.getType())));
            return Result.good(List.copyOf(attributeCache.values()));
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> createAttribute(UUID createdBy, String name, String type) {
        if (name == null || name.isBlank()) return CompletableFuture.completedFuture(Result.bad("Attribute name cannot be null or blank."));
        if (type == null || type.isBlank()) return CompletableFuture.completedFuture(Result.bad("Attribute type cannot be null or blank."));
        if (attributeCache.containsKey(name)) return CompletableFuture.completedFuture(Result.bad("An attribute with that name already exists."));

        var attributeType = configService.getAttributeType(type);
        if (attributeType == null) return CompletableFuture.completedFuture(Result.bad("An attribute type with that name does not exist."));

        var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
        if (userId == -1) return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + createdBy + "."));

        return uow.repo(IAttributeRepository.class).insertAttribute(userId, name, type).thenApply(success -> {
            if (success == null) return Result.bad("Failed to insert attribute.");
            var attribute = new Attribute(success.getId(), name, type);
            attributeCache.put(name, attribute);
            return Result.good(attribute);
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttributeName(UUID modifiedBy, String oldName, String newName) {
        var oldAttribute = attributeCache.get(oldName);
        if (oldAttribute == null) {
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return uow.repo(IAttributeRepository.class).updateAttribute(userId, oldAttribute.getId(), newName, null).thenApply(success -> {
            if (success == null) return Result.bad("Failed to update attribute.");
            var attribute = new Attribute(success.getId(), newName, success.getType());
            attributeCache.remove(oldName);
            attributeCache.put(newName, attribute);
            return Result.good(attribute);
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttributeType(UUID modifiedBy, String name, String newType) {
        var oldAttribute = attributeCache.get(name);
        if (oldAttribute == null) {
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
        }

        return uow.repo(IAttributeRepository.class).updateAttribute(userId, oldAttribute.getId(), null, newType).thenApply(success -> {
            if (success == null) return Result.bad("Failed to update attribute.");
            var attribute = new Attribute(success.getId(), success.getName(), newType);
            attributeCache.put(name, attribute);
            return Result.good(attribute);
        });
    }

    @Override
    public CompletableFuture<Result<Attribute>> deleteAttribute(UUID deletedBy, String name) {
        var oldAttribute = attributeCache.get(name);
        if (oldAttribute == null) {
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
        if (userId == -1) {
            return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + deletedBy + "."));
        }

        return uow.repo(IAttributeRepository.class).deleteAttribute(userId, oldAttribute.getId()).thenApply(success -> {
            if (success == -1) return Result.bad("Failed to delete attribute.");
            attributeCache.remove(name);
            return Result.good(oldAttribute);
        });
    }
}
