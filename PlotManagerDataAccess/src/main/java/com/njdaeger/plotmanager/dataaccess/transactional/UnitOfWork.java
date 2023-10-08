package com.njdaeger.plotmanager.dataaccess.transactional;

import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.plotmanager.dataaccess.IDatabase;
import com.njdaeger.plotmanager.dataaccess.IRepository;
import com.njdaeger.plotmanager.dataaccess.repositories.IAttributeRepository;
import com.njdaeger.plotmanager.dataaccess.repositories.IUserRepository;
import com.njdaeger.plotmanager.dataaccess.repositories.impl.AttributeRepository;
import com.njdaeger.plotmanager.dataaccess.repositories.impl.UserRepository;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.serviceprovider.ServiceProviderBuilder;
import org.bukkit.plugin.Plugin;

public class UnitOfWork implements IUnitOfWork {

    private final IServiceProvider uowServiceProvider;
    private final ITransaction<?> transaction;
    private final IDatabase<ITransaction<?>, ?> database;

    public UnitOfWork(Plugin plugin, IExceptionPublisher exceptionPublisher, IDatabase<ITransaction<?>, ?> database) {
        try {
            this.transaction = database.createTransaction();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.uowServiceProvider = ServiceProviderBuilder.builder()
                .addSingleton(IExceptionPublisher.class, (s) -> exceptionPublisher)
                .addSingleton(ITransaction.class, (s) -> this.transaction)
                .addSingleton(IAttributeRepository.class, AttributeRepository.class)
                .addSingleton(IUserRepository.class, UserRepository.class)
                .build(plugin);
        this.database = database;
    }

    @Override
    public <R extends IRepository> R repo(Class<R> repositoryClass) {
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
        transaction.close();
    }
}
