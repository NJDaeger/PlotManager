package com.njdaeger.taskmanager.dataaccess.transactional;

import com.njdaeger.taskmanager.dataaccess.IDatabase;
import com.njdaeger.taskmanager.dataaccess.IProcedure;
import com.njdaeger.taskmanager.dataaccess.IRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.IProjectRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.ITaskRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.ITaskTypeRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.IUserRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.implementations.AttributeRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.implementations.ProjectRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.implementations.TaskRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.implementations.TaskTypeRepository;
import com.njdaeger.taskmanager.dataaccess.repositories.implementations.UserRepository;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.serviceprovider.ServiceProviderBuilder;
import org.bukkit.plugin.Plugin;

public class UnitOfWork implements IUnitOfWork {

    private final IServiceProvider uowServiceProvider;
    private final AbstractDatabaseTransaction<?> transaction;
    private final IDatabase<AbstractDatabaseTransaction<?>, ?> database;
    private final IPluginLogger logger;

    public UnitOfWork(Plugin plugin, IPluginLogger logger, IDatabase<AbstractDatabaseTransaction<?>, ?> database) {
        this.logger = logger;
        try {
            this.transaction = database.createTransaction();
        } catch (Exception e) {
            logger.exception(e);
            throw new RuntimeException(e);
        }
        this.uowServiceProvider = ServiceProviderBuilder.builder()
                .addSingleton(IPluginLogger.class, (s) -> logger)
                .addSingleton(AbstractDatabaseTransaction.class, (s) -> this.transaction)
                .addSingleton(IProcedure.class, (s) -> database.getProcedures())
                .addSingleton(IAttributeRepository.class, AttributeRepository.class)
                .addSingleton(IUserRepository.class, UserRepository.class)
                .addSingleton(IProjectRepository.class, ProjectRepository.class)
                .addSingleton(ITaskRepository.class, TaskRepository.class)
                .addSingleton(ITaskTypeRepository.class, TaskTypeRepository.class)
                .build(plugin);
        this.database = database;
    }

    @Override
    public AbstractDatabaseTransaction<?> getTransaction() {
        return transaction;
    }

    @Override
    public <R extends IRepository> R repo(Class<R> repositoryClass) {
        logger.debug("Getting repository " + repositoryClass.getSimpleName() + " from unit of work.");
        return uowServiceProvider.getRequiredService(repositoryClass);
    }

    @Override
    public void commit() {
        database.commitTransaction(transaction);
    }

    @Override
    public void abort() {
        database.rollbackTransaction(transaction);
    }

    @Override
    public void close() throws Exception {
        try {
            commit();
        } catch (Exception e) {
            abort();
            logger.exception(e);
            throw e;
        } finally {
            transaction.close();
        }
    }
}
