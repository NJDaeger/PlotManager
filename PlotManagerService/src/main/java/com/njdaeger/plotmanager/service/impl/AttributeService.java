package com.njdaeger.plotmanager.service.impl;

import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.plotmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.service.IAttributeService;
import com.njdaeger.plotmanager.service.IUserService;
import com.njdaeger.plotmanager.service.Result;
import com.njdaeger.plotmanager.service.models.Attribute;
import com.njdaeger.plotmanager.service.models.User;
import com.njdaeger.serviceprovider.IServiceProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class AttributeService implements IAttributeService {

    private final IUserService userService;
    private final IServiceProvider provider;
    private final IExceptionPublisher publisher;
    private final Map<String, Attribute> attributeCache;

    public AttributeService(IServiceProvider provider, IExceptionPublisher publisher, IUserService userService) {
        this.provider = provider;
        this.publisher = publisher;
        this.userService = userService;
        this.attributeCache = new HashMap<>();
    }

    @Override
    public CompletableFuture<Result<List<Attribute>>> getAttributes() {
        try {
            if (!attributeCache.isEmpty()) {
                return CompletableFuture.completedFuture(Result.good(List.copyOf(attributeCache.values())));
            }
            var uow = provider.getRequiredService(IUnitOfWork.class);
            return uow.repo(IAttributeRepository.class).getAttributes().thenApply(attributes -> {
                attributes.forEach(attribute -> attributeCache.put(attribute.getName(), new Attribute(attribute.getId(), attribute.getName(), attribute.getType())));
                return Result.good(List.copyOf(attributeCache.values()));
            });
        } catch (Exception e) {
            publisher.publishException(e);
            return CompletableFuture.completedFuture(Result.bad(e.getMessage()));
        }
    }

    @Override
    public CompletableFuture<Result<Attribute>> createAttribute(UUID createdBy, String name, String type) {
        if (attributeCache.containsKey(name)) {
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name already exists."));
        }
        try {
            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) {
                return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + createdBy + "."));
            }

            var uow = provider.getRequiredService(IUnitOfWork.class);

            return uow.repo(IAttributeRepository.class).insertAttribute(userId, name, type).thenApply(success -> {
                if (success != null) {
                    var attribute = new Attribute(success.getId(), name, type);
                    attributeCache.put(name, attribute);
                    return Result.good(attribute);
                }
                return Result.bad("Failed to insert attribute.");
            });
        } catch (Exception e) {
            publisher.publishException(e);
            return CompletableFuture.completedFuture(Result.bad(e.getMessage()));
        }
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttributeName(UUID modifiedBy, String oldName, String newName) {
        var oldAttribute = attributeCache.get(oldName);
        if (oldAttribute == null) {
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        try {
            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) {
                return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
            }

            var uow = provider.getRequiredService(IUnitOfWork.class);

            return uow.repo(IAttributeRepository.class).updateAttribute(userId, oldAttribute.getId(), newName, null).thenApply(success -> {
                if (success != null) {
                    var attribute = new Attribute(success.getId(), newName, success.getType());
                    attributeCache.remove(oldName);
                    attributeCache.put(newName, attribute);
                    return Result.good(attribute);
                }
                return Result.bad("Failed to update attribute.");
            });
        } catch (Exception e) {
            publisher.publishException(e);
            return CompletableFuture.completedFuture(Result.bad(e.getMessage()));
        }
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttributeType(UUID modifiedBy, String name, String newType) {
        var oldAttribute = attributeCache.get(name);
        if (oldAttribute == null) {
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        try {
            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) {
                return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + modifiedBy + "."));
            }

            var uow = provider.getRequiredService(IUnitOfWork.class);

            return uow.repo(IAttributeRepository.class).updateAttribute(userId, oldAttribute.getId(), null, newType).thenApply(success -> {
                if (success != null) {
                    var attribute = new Attribute(success.getId(), success.getName(), newType);
                    attributeCache.put(name, attribute);
                    return Result.good(attribute);
                }
                return Result.bad("Failed to update attribute.");
            });
        } catch (Exception e) {
            publisher.publishException(e);
            return CompletableFuture.completedFuture(Result.bad(e.getMessage()));
        }
    }

    @Override
    public CompletableFuture<Result<Attribute>> deleteAttribute(UUID deletedBy, String name) {
        var oldAttribute = attributeCache.get(name);
        if (oldAttribute == null) {
            return CompletableFuture.completedFuture(Result.bad("An attribute with that name does not exist."));
        }

        try {
            var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
            if (userId == -1) {
                return CompletableFuture.completedFuture(Result.bad("Failed to find user with uuid " + deletedBy + "."));
            }

            var uow = provider.getRequiredService(IUnitOfWork.class);

            return uow.repo(IAttributeRepository.class).deleteAttribute(userId, oldAttribute.getId()).thenApply(success -> {
                if (success != null) {
                    attributeCache.remove(name);
                    return Result.good(oldAttribute);
                }
                return Result.bad("Failed to delete attribute.");
            });
        } catch (Exception e) {
            publisher.publishException(e);
            return CompletableFuture.completedFuture(Result.bad(e.getMessage()));
        }
    }

}
