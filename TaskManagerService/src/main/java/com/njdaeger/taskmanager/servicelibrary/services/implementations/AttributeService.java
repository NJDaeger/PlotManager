package com.njdaeger.taskmanager.servicelibrary.services.implementations;

import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.taskmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.events.attributes.AttributeCreateEvent;
import com.njdaeger.taskmanager.servicelibrary.models.Attribute;
import com.njdaeger.taskmanager.servicelibrary.models.TaskType;
import com.njdaeger.taskmanager.servicelibrary.models.User;
import com.njdaeger.taskmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.IConfigService;
import com.njdaeger.taskmanager.servicelibrary.services.ITaskTypeService;
import com.njdaeger.taskmanager.servicelibrary.services.IUserService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class AttributeService implements IAttributeService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final ITaskTypeService taskTypeService;
    private final IConfigService configService;
    private final ICacheService cacheService;
    private final IPluginLogger logger;

    public AttributeService(IServiceTransaction transaction, IUserService userService, ITaskTypeService taskTypeService, IConfigService configService, ICacheService cacheService, IPluginLogger logger) {
        this.transaction = transaction;
        this.userService = userService;
        this.configService = configService;
        this.cacheService = cacheService;
        this.taskTypeService = taskTypeService;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<Result<List<Attribute>>> getAttributes() {
        transaction.use();

        return Util.async(() -> {
            if (!cacheService.getAttributeCache().isEmpty()) return Result.good(List.copyOf(cacheService.getAttributeCache().values()));

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributes().thenApply(attributes -> {
                attributes.forEach(attribute -> cacheService.getAttributeCache().put(attribute.getId(), new Attribute(attribute.getId(), await(taskTypeService.getTaskType(attribute.getTaskTypeId())).getOrThrow(), attribute.getName())));
                return Result.good(List.copyOf(cacheService.getAttributeCache().values()));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> getAttribute(String name, TaskType taskType) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (name == null || name.isBlank()) return Result.bad("Attribute name cannot be null or blank.");

            var found = cacheService.getAttributeCache().values().stream().filter(attr -> attr.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
            if (found != null) return Result.good(found);

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributeByName(name, taskType.getTaskTypeId()).thenApply(attribute -> {
                if (attribute == null) return Result.bad("Failed to find attribute with name " + name + ".");
                var attr = new Attribute(attribute.getId(), taskType, attribute.getName());
                cacheService.getAttributeCache().put(attribute.getId(), attr);
                return Result.good(attr);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> getAttributeById(int id) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (id < 0) return Result.bad("Attribute id cannot be less than 0.");

            if (cacheService.getAttributeCache().containsKey(id)) return Result.good(cacheService.getAttributeCache().get(id));

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).getAttributeById(id).thenApply(attr -> {
                if (attr == null) return Result.bad("Failed to find attribute with id " + id + ".");
                var taskTypeObj = await(taskTypeService.getTaskType(attr.getTaskTypeId())).getOrThrow();
                var attribute = new Attribute(attr.getId(), taskTypeObj, attr.getName());
                cacheService.getAttributeCache().put(attr.getId(), attribute);
                return Result.good(attribute);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> createAttribute(UUID createdBy, TaskType taskType, String name) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {

            if (createdBy == null) return Result.bad("Creator cannot be null.");

            if (name == null || name.isBlank()) return Result.bad("Attribute name cannot be null or blank.");

            var found = cacheService.getAttributeCache().values().stream().filter(attr -> attr.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
            if (found != null) return Result.bad("An attribute with that name already exists.");
            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + createdBy + ".");

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).insertAttribute(userId, taskType.getTaskTypeId(), name).thenApply(success -> {
                if (success == null) return Result.bad("Failed to insert attribute.");
                var taskTypeObj = await(taskTypeService.getTaskType(success.getTaskTypeId())).getOrThrow();
                var attribute = new Attribute(success.getId(), taskTypeObj, name);
                cacheService.getAttributeCache().put(success.getId(), attribute);
                return Result.good(attribute);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Attribute>> updateAttribute(UUID modifiedBy, String attributeBeingUpdated, TaskType taskType, String newName) {
        transaction.use();

        return Util.<Result<Attribute>>async(() -> {
            if (modifiedBy == null) return Result.bad("Modifier cannot be null.");
            if (attributeBeingUpdated == null || attributeBeingUpdated.isBlank()) return Result.bad("Old attribute name cannot be null or blank.");
            if (newName == null || newName.isBlank()) return Result.bad("New attribute name cannot be null or blank.");
            if (cacheService.getAttributeCache().values().stream().anyMatch(attr -> attr.getName().equalsIgnoreCase(newName))) return Result.bad("An attribute with that name already exists.");
            var found = cacheService.getAttributeCache().values().stream().filter(attr -> attr.getName().equalsIgnoreCase(attributeBeingUpdated)).findFirst().orElse(null);
            if (found == null) return Result.bad("Failed to find attribute with name " + attributeBeingUpdated + ".");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            var taskTypeId = taskType == null ? null : taskType.getTaskTypeId();

            return await(transaction.getUnitOfWork().repo(IAttributeRepository.class).updateAttribute(userId, found.getId(), taskTypeId, newName).thenApply(success -> {
                if (success == null) return Result.bad("Failed to update attribute.");
                var taskTypeObj = await(taskTypeService.getTaskType(success.getTaskTypeId())).getOrThrow();
                var attribute = new Attribute(success.getId(), taskTypeObj, newName);
                cacheService.getAttributeCache().remove(found.getId());
                cacheService.getAttributeCache().put(success.getId(), attribute);
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
            attributes.forEach(attribute -> cacheService.getAttributeCache().put(attribute.getId(), new Attribute(attribute.getId(), await(taskTypeService.getTaskType(attribute.getTaskTypeId())).getOrThrow(), attribute.getName())));
            return Result.good(List.copyOf(cacheService.getAttributeCache().values()));
        }).whenComplete(finishTransaction()).thenAccept((r) -> {});
    }
}
