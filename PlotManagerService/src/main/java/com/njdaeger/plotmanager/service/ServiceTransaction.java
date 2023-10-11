package com.njdaeger.plotmanager.service;

import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.service.impl.AttributeService;
import com.njdaeger.plotmanager.service.impl.UserService;
import com.njdaeger.plotmanager.service.impl.WorldService;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.serviceprovider.ServiceProviderBuilder;
import org.bukkit.plugin.Plugin;

public class ServiceTransaction implements IServiceTransaction {

    private final IUnitOfWork unitOfWork;
    private final IServiceProvider serviceProvider;

    public ServiceTransaction(Plugin plugin, IUnitOfWork unitOfWork, IExceptionPublisher exceptionPublisher) {
        this.unitOfWork = unitOfWork;
        this.serviceProvider = ServiceProviderBuilder.builder()
                .addSingleton(Plugin.class, (s) -> plugin)
                .addSingleton(IUnitOfWork.class, (s) -> unitOfWork)
                .addSingleton(IExceptionPublisher.class, (s) -> exceptionPublisher)
                .addSingleton(IAttributeService.class, AttributeService.class)
                .addSingleton(IUserService.class, UserService.class)
                .addSingleton(IWorldService.class, WorldService.class)
                .build(plugin);
    }

    @Override
    public <S extends ITransactionalService> S getService(Class<S> serviceClass) {
        return serviceProvider.getRequiredService(serviceClass);
    }

    @Override
    public IUnitOfWork getUnitOfWork() {
        return unitOfWork;
    }

    @Override
    public void commit() {
        unitOfWork.commit();
    }

    @Override
    public void abort() {
        unitOfWork.abort();
    }

    @Override
    public void close() throws Exception {
        unitOfWork.close();
    }
}
