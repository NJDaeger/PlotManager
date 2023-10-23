package com.njdaeger.plotmanager.servicelibrary.transactional;

import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IPlotService;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.PlotService;
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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceTransaction implements IServiceTransaction {

    private IUnitOfWork unitOfWork;
    private final IServiceProvider serviceProvider;
    private final IServiceProvider pluginServiceProvider;
    private final UUID transactionId;
    private final IPluginLogger logger;
    private final AtomicInteger refCount = new AtomicInteger(0);
    private final AtomicBoolean canClose = new AtomicBoolean(false);

    public ServiceTransaction(IServiceProvider pluginServiceProvider, Plugin plugin, IPluginLogger logger, IConfigService configService, ICacheService cacheService) {
        this.logger = logger;
        this.transactionId = UUID.randomUUID();
        logger.debug("Transaction created: " + transactionId);
        this.pluginServiceProvider = pluginServiceProvider;
        this.serviceProvider = ServiceProviderBuilder.builder()
                .addSingleton(ICacheService.class, (s) -> cacheService)
                .addSingleton(IConfigService.class, (s) -> configService)
                .addSingleton(IServiceTransaction.class, (s) -> this)
                .addSingleton(Plugin.class, (s) -> plugin)
                .addSingleton(IPluginLogger.class, (s) -> logger)
                .addSingleton(IAttributeService.class, AttributeService.class)
                .addSingleton(IUserService.class, UserService.class)
                .addSingleton(IWorldService.class, WorldService.class)
                .addSingleton(IPlotService.class, PlotService.class)
                .build(plugin);
    }

    @Override
    public <S extends ITransactionalService> S getService(Class<S> serviceClass) {
        logger.debug("Getting service " + serviceClass.getSimpleName() + " from transaction " + transactionId);
        return serviceProvider.getRequiredService(serviceClass);
    }

    @Override
    public IUnitOfWork getUnitOfWork() {
        if (unitOfWork == null) this.unitOfWork = pluginServiceProvider.getRequiredService(IUnitOfWork.class);
        return unitOfWork;
    }

    @Override
    public void commit() {
        if (unitOfWork != null) unitOfWork.commit();
    }

    @Override
    public void abort() {
        if (unitOfWork != null) unitOfWork.abort();
    }

    @Override
    public UUID getTransactionId() {
        return transactionId;
    }

    @Override
    public void use() {
        refCount.incrementAndGet();
        logger.debug("Transaction " + transactionId + " has " + refCount.get() + " references.");
    }

    @Override
    public void release() {
        if (refCount.decrementAndGet() == 0 && canClose.get()) {
            logger.debug("Transaction " + transactionId + " has no more references. Closing...");
            try {
                close();
            } catch (Exception e) {
                logger.exception(e);
            }
        } else logger.debug("Transaction " + transactionId + " has " + refCount.get() + " references.");

    }

    @Override
    public void close() throws Exception {
        canClose.set(true);
        if (refCount.get() != 0) {
            logger.debug("Transaction " + transactionId + " has " + refCount.get() + " references. Waiting for references to be released before closing.");
            return;
        }
        if (unitOfWork != null) unitOfWork.close();
        logger.debug("Transaction closed: " + transactionId);
    }
}
