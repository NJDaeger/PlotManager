package com.njdaeger.plotmanager.servicelibrary.transactional;

import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.AttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.UserService;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.WorldService;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.services.IWorldService;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.serviceprovider.ServiceProviderBuilder;
import org.bukkit.plugin.Plugin;

public class ServiceTransaction implements IServiceTransaction {

    private final IUnitOfWork unitOfWork;
    private final IServiceProvider serviceProvider;

    public ServiceTransaction(Plugin plugin, IUnitOfWork unitOfWork, IPluginLogger logger) {
        this.unitOfWork = unitOfWork;
        this.serviceProvider = ServiceProviderBuilder.builder()
                .addSingleton(Plugin.class, (s) -> plugin)
                .addSingleton(IUnitOfWork.class, (s) -> unitOfWork)
                .addSingleton(IPluginLogger.class, (s) -> logger)
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
