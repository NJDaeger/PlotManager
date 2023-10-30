package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.dataaccess.Util;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IPlotService;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.services.IWorldService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import org.bukkit.Bukkit;

import static com.njdaeger.plotmanager.dataaccess.Util.await;
import static com.njdaeger.plotmanager.dataaccess.Util.awaitAll;

public class InitializationService implements Runnable {

    private final IServiceTransaction transaction;
    private final IConfigService configService;
    private final IPluginLogger logger;

    public InitializationService(IServiceTransaction transaction, IConfigService configService, IPluginLogger logger) {
        this.transaction = transaction;
        this.configService = configService;
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.info("Initializing...");
        insertWorlds();
        if (isSetupMode()) {
            logger.info("Plugin is in setup mode. This may take a little longer than normal...");
            insertInitialAttributes();
            configService.setEntry("setup-mode", false);
            configService.save();
            logger.info("Setup complete!");
        }
        warmup();
        try {
            transaction.close();
        } catch (Exception e) {
            logger.exception(e);
        }
        logger.info("Initialization complete!");
    }

    private boolean isSetupMode() {
        return configService.isSetupMode();
    }

    private void insertWorlds() {
        logger.info("Loading worlds...");
        var worldService = transaction.getService(IWorldService.class);

        //load database worlds into cache
        await(worldService.getWorlds());

        var userService = transaction.getService(IUserService.class);
        var system = await(userService.getSystemUser()).getOrThrow();

        Bukkit.getWorlds().forEach(w -> await(worldService.getWorldByUuid(w.getUID()).thenApply(wRes -> {
            if (!wRes.successful()) return await(worldService.createWorld(system.getUserId(), w)).getOrThrow();
            else {
                var world = wRes.getOrThrow();
                if (!world.getWorldName().equals(w.getName())) world = await(worldService.updateWorld(system.getUserId(), w.getUID(), w.getName())).getOrThrow();
                return world;
            }
        })));
        transaction.commit();
        logger.info("World loading complete!");
    }

    private void insertInitialAttributes() {
        logger.info("Inserting initial attributes...");

        var attributeService = transaction.getService(IAttributeService.class);
        awaitAll(
                attributeService.createAttribute(Util.SYSTEM_UUID, "status", "status"),
                attributeService.createAttribute(Util.SYSTEM_UUID, "rank", "rank"),
                attributeService.createAttribute(Util.SYSTEM_UUID, "points", "points"),
                attributeService.createAttribute(Util.SYSTEM_UUID, "building-type", "building-type"),
                attributeService.createAttribute(Util.SYSTEM_UUID, "description", "string"),
                attributeService.createAttribute(Util.SYSTEM_UUID, "floors", "integer")
        );

        transaction.commit();
        logger.info("Initial attributes inserted!");
    }

    private void warmup() {
        logger.info("Warming up...");
        var userService = transaction.getService(IUserService.class);
        var worldService = transaction.getService(IWorldService.class);
        var attributeService = transaction.getService(IAttributeService.class);
        var plotService = transaction.getService(IPlotService.class);

        await(userService.initializeCache());
        await(worldService.initializeCache());
        await(attributeService.initializeCache());
        await(plotService.initializeCache());
        logger.info("Warmup complete!");
    }

}
