package com.njdaeger.taskmanager.servicelibrary.services.implementations;

import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.taskmanager.dataaccess.repositories.IProjectRepository;
import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.models.User;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.IProjectService;
import com.njdaeger.taskmanager.servicelibrary.services.IUserService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class ProjectService implements IProjectService {

    private final IServiceTransaction transaction;
    private final IUserService userService;
    private final ICacheService cacheService;
    private final IPluginLogger logger;

    public ProjectService(IServiceTransaction transaction, IUserService userService, ICacheService cacheService, IPluginLogger logger) {
        this.transaction = transaction;
        this.userService = userService;
        this.cacheService = cacheService;
        this.logger = logger;
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
        cacheService.getProjectCache().clear();
    }

    @Override
    public CompletableFuture<Void> initializeCache() {
        transaction.use();

        return transaction.getUnitOfWork().repo(IProjectRepository.class).getProjects().thenApply(projects -> {
            projects.forEach(project -> cacheService.getProjectCache().put(project.getPrefix(), new Project(project.getId(), project.getName(), project.getDescription(), project.getPrefix())));
            return Result.good(List.copyOf(cacheService.getProjectCache().values()));
        }).whenComplete(finishTransaction()).thenAccept(r -> {});
    }


    @Override
    public CompletableFuture<Result<Project>> createProject(UUID createdBy, String projectName, String projectDescription, String projectPrefix) {
        transaction.use();

        return Util.<Result<Project>>async(() -> {
            if (projectName == null || projectName.isBlank() || projectName.length() > 255) return Result.bad("Project name cannot be null, blank, or longer than 255 characters.");
            if (projectPrefix == null || projectPrefix.isBlank() || projectPrefix.length() > 4) return Result.bad("Project prefix cannot be null, blank, or longer than 4 characters.");
            if (projectDescription == null || projectDescription.isBlank() || projectDescription.length() > 255) return Result.bad("Project description cannot be null, blank, or longer than 255 characters.");

            if (cacheService.getProjectCache().values().stream().anyMatch(project -> project.getProjectName().equals(projectName))) return Result.bad("A project with that name already exists.");
            if (cacheService.getProjectCache().get(projectPrefix) != null) return Result.bad("A project with that prefix already exists.");

            var userId = await(userService.getUserByUuid(createdBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + createdBy + ".");


            return await(transaction.getUnitOfWork().repo(IProjectRepository.class).insertProject(userId, projectName, projectDescription, projectPrefix).thenApply(project -> {
                if (project == null) return Result.bad("Failed to create project.");

                var createdProject = new Project(project.getId(), project.getName(), project.getDescription(), project.getPrefix());

                cacheService.getProjectCache().put(project.getPrefix(), createdProject);

                return Result.good(createdProject);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<List<Project>>> getProjects() {
        transaction.use();

        return Util.<Result<List<Project>>>async(() -> {
            var cached = List.copyOf(cacheService.getProjectCache().values());
            if (!cached.isEmpty()) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(IProjectRepository.class).getProjects().thenApply(projects -> {
                if (projects.isEmpty()) return Result.bad("No projects exist.");
                projects.forEach(project -> cacheService.getProjectCache().put(project.getPrefix(), new Project(project.getId(), project.getName(), project.getDescription(), project.getPrefix())));
                return Result.good(List.copyOf(cacheService.getProjectCache().values()));
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Project>> getProjectById(int projectId) {
        transaction.use();

        return Util.<Result<Project>>async(() -> {
            var cached = cacheService.getProjectCache().values().stream().filter(project -> project.getProjectId() == projectId).findFirst().orElse(null);
            if (cached != null) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(IProjectRepository.class).getProjectById(projectId).thenApply(project -> {
                if (project == null) return Result.bad("Project does not exist.");
                var newProject = new Project(project.getId(), project.getName(), project.getDescription(), project.getPrefix());
                cacheService.getProjectCache().put(project.getPrefix(), newProject);
                return Result.good(newProject);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Project>> getProjectByName(String projectName) {
        transaction.use();

        return Util.<Result<Project>>async(() -> {
            var cached = cacheService.getProjectCache().values().stream().filter(project -> project.getProjectName().equals(projectName)).findFirst().orElse(null);
            if (cached != null) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(IProjectRepository.class).getProjectByName(projectName).thenApply(project -> {
                if (project == null) return Result.bad("Project does not exist.");
                var newProject = new Project(project.getId(), project.getName(), project.getDescription(), project.getPrefix());
                cacheService.getProjectCache().put(project.getPrefix(), newProject);
                return Result.good(newProject);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Project>> getProjectByPrefix(String projectPrefix) {
        transaction.use();

        return Util.<Result<Project>>async(() -> {
            var cached = cacheService.getProjectCache().get(projectPrefix);
            if (cached != null) return Result.good(cached);

            return await(transaction.getUnitOfWork().repo(IProjectRepository.class).getProjectByPrefix(projectPrefix).thenApply(project -> {
                if (project == null) return Result.bad("Project does not exist.");
                var newProject = new Project(project.getId(), project.getName(), project.getDescription(), project.getPrefix());
                cacheService.getProjectCache().put(project.getPrefix(), newProject);
                return Result.good(newProject);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Project>> updateProject(UUID modifiedBy, int projectId, String projectName, String projectDescription, String projectPrefix) {
        transaction.use();

        return Util.<Result<Project>>async(() -> {
            if (projectName != null && (projectName.isBlank() || projectName.length() > 255)) return Result.bad("Project name cannot be null, blank, or longer than 255 characters.");
            if (projectPrefix != null && (projectPrefix.isBlank() || projectPrefix.length() > 4)) return Result.bad("Project prefix cannot be null, blank, or longer than 4 characters.");
            if (projectDescription != null && (projectDescription.isBlank() || projectDescription.length() > 255)) return Result.bad("Project description cannot be null, blank, or longer than 255 characters.");

            var userId = await(userService.getUserByUuid(modifiedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + modifiedBy + ".");

            return await(transaction.getUnitOfWork().repo(IProjectRepository.class).updateProject(userId, projectId, projectName, projectDescription, projectPrefix).thenApply(project -> {
                if (project == null) return Result.bad("Failed to update project.");

                var updatedProject = new Project(project.getId(), project.getName(), project.getDescription(), project.getPrefix());
                cacheService.getProjectCache().put(project.getPrefix(), updatedProject);
                return Result.good(updatedProject);
            }));
        }).whenComplete(finishTransaction());
    }

    @Override
    public CompletableFuture<Result<Project>> deleteProject(UUID deletedBy, int projectId) {
        transaction.use();

        return Util.<Result<Project>>async(() -> {
            var userId = await(userService.getUserByUuid(deletedBy)).getOr(User::getId, -1);
            if (userId == -1) return Result.bad("Failed to find user with uuid " + deletedBy + ".");

            return await(transaction.getUnitOfWork().repo(IProjectRepository.class).deleteProject(userId, projectId).thenApply(deleted -> {
                if (deleted == -1) return Result.bad("Failed to delete project.");

                var project = cacheService.getProjectCache().values().stream().filter(p -> p.getProjectId() == projectId).findFirst().orElse(null);
                if (project != null) cacheService.getProjectCache().remove(project.getProjectPrefix());

                return Result.good(project);
            }));
        }).whenComplete(finishTransaction());
    }

}
