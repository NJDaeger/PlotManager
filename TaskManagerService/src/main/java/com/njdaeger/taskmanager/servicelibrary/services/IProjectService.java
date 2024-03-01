package com.njdaeger.taskmanager.servicelibrary.services;

import com.njdaeger.taskmanager.servicelibrary.Result;
import com.njdaeger.taskmanager.servicelibrary.models.Project;
import com.njdaeger.taskmanager.servicelibrary.transactional.ITransactionalService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IProjectService extends ITransactionalService, ICached {

    CompletableFuture<Result<List<Project>>> getProjects();

    CompletableFuture<Result<Project>> getProjectById(int projectId);

    CompletableFuture<Result<Project>> getProjectByName(String projectName);

    CompletableFuture<Result<Project>> getProjectByPrefix(String projectPrefix);

    CompletableFuture<Result<Project>> createProject(UUID createdBy, String projectName, String projectDescription, String projectPrefix);

    CompletableFuture<Result<Project>> updateProject(UUID modifiedBy, int projectId, String projectName, String projectDescription, String projectPrefix);

    CompletableFuture<Result<Project>> deleteProject(UUID deletedBy, int projectId);

}
