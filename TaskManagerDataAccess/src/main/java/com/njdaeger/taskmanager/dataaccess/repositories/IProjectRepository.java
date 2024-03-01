package com.njdaeger.taskmanager.dataaccess.repositories;

import com.njdaeger.taskmanager.dataaccess.IRepository;
import com.njdaeger.taskmanager.dataaccess.models.ProjectEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the interactions with the following tables:
 * * Project
 * * ProjectTasks
 */
public interface IProjectRepository extends IRepository {

    /**
     * Get all projects
     * @return A list of projects
     */
    CompletableFuture<List<ProjectEntity>> getProjects();

    /**
     * Get a project by id
     * @param projectId The id of the project
     * @return The project
     */
    CompletableFuture<ProjectEntity> getProjectById(int projectId);

    /**
     * Get a project by name
     * @param projectName The name of the project
     * @return The project
     */
    CompletableFuture<ProjectEntity> getProjectByName(String projectName);

    /**
     * Get a project by prefix
     * @param projectPrefix The prefix of the project
     * @return The project
     */
    CompletableFuture<ProjectEntity> getProjectByPrefix(String projectPrefix);

    /**
     * Insert a project
     * @param createdBy The id of the user who created the project
     * @param projectName The name of the project
     * @param projectDescription The description of the project
     * @param projectPrefix The prefix of the project
     * @return The inserted project, or null if the project was not inserted
     */
    CompletableFuture<ProjectEntity> insertProject(int createdBy, String projectName, String projectDescription, String projectPrefix);

    /**
     * Update a project
     * @param modifiedBy The id of the user who updated the project
     * @param projectId The id of the project to update
     * @param projectName The new name of the project, or null to keep the old name
     * @param projectDescription The new description of the project, or null to keep the old description
     * @param projectPrefix The new prefix of the project, or null to keep the old prefix
     * @return The updated project, or null if the project was not updated
     */
    CompletableFuture<ProjectEntity> updateProject(int modifiedBy, int projectId, String projectName, String projectDescription, String projectPrefix);

    /**
     * Delete a project
     * @param deletedBy The id of the user who deleted the project
     * @param projectId The id of the project to delete
     * @return The deleted project id, or -1 if the project was not deleted
     */
    CompletableFuture<Integer> deleteProject(int deletedBy, int projectId);

}
