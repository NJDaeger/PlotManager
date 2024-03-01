package com.njdaeger.taskmanager.dataaccess.repositories.implementations;

import com.njdaeger.taskmanager.dataaccess.IProcedure;
import com.njdaeger.taskmanager.dataaccess.models.ProjectEntity;
import com.njdaeger.taskmanager.dataaccess.repositories.IProjectRepository;
import com.njdaeger.taskmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.taskmanager.dataaccess.Util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProjectRepository implements IProjectRepository {

    private final IPluginLogger logger;
    private final AbstractDatabaseTransaction<?> transaction;
    private final IProcedure procedures;

    public ProjectRepository(IPluginLogger logger, IProcedure procedures, AbstractDatabaseTransaction<?> transaction) {
        this.logger = logger;
        this.transaction = transaction;
        this.procedures = procedures;
        Util.await(initializeRepository());
    }

    @Override
    public CompletableFuture<Boolean> initializeRepository() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<List<ProjectEntity>> getProjects() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectProjects();
                return transaction.query(proc.getFirst(), ProjectEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<ProjectEntity> getProjectById(int projectId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectProjectById(projectId);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), ProjectEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<ProjectEntity> getProjectByName(String projectName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectProjectByName(projectName);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), ProjectEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<ProjectEntity> getProjectByPrefix(String projectPrefix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.selectProjectByPrefix(projectPrefix);
                return transaction.queryScalar(proc.getFirst(), proc.getSecond(), ProjectEntity.class);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<ProjectEntity> insertProject(int createdBy, String projectName, String projectDescription, String projectPrefix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.insertProject(createdBy, projectName, projectDescription, projectPrefix);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getProjectById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<ProjectEntity> updateProject(int modifiedBy, int projectId, String projectName, String projectDescription, String projectPrefix) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.updateProject(modifiedBy, projectId, projectName, projectDescription, projectPrefix);
                var id = transaction.execute(proc.getFirst(), proc.getSecond());
                if (id == -1) return null;
                return Util.await(getProjectById(id));
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteProject(int deletedBy, int projectId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var proc = procedures.deleteProject(deletedBy, projectId);
                return transaction.execute(proc.getFirst(), proc.getSecond());
            } catch (Exception e) {
                logger.exception(e);
                return -1;
            }
        });
    }
}
