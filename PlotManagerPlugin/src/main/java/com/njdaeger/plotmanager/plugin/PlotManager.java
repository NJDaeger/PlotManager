package com.njdaeger.plotmanager.plugin;

import com.njdaeger.exceptionpublisher.ExceptionPublisher;
import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.dataaccess.IDatabase;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.impl.mysql.MySqlDatabase;
import com.njdaeger.plotmanager.dataaccess.impl.mysql.MySqlProcedures;
import com.njdaeger.plotmanager.dataaccess.impl.yml.YmlDatabase;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.dataaccess.transactional.UnitOfWork;
import com.njdaeger.plotmanager.service.IAttributeService;
import com.njdaeger.plotmanager.service.IConfigService;
import com.njdaeger.plotmanager.service.IServiceTransaction;
import com.njdaeger.plotmanager.service.IUserService;
import com.njdaeger.plotmanager.service.IWorldService;
import com.njdaeger.plotmanager.service.ServiceTransaction;
import com.njdaeger.plotmanager.service.impl.AttributeService;
import com.njdaeger.plotmanager.service.impl.ConfigService;
import com.njdaeger.plotmanager.service.impl.UserService;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.serviceprovider.ServiceProviderBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class PlotManager extends JavaPlugin implements IPlotManagerPlugin {

    private IServiceProvider serviceProvider;

    @Override
    public void onEnable() {

        this.serviceProvider = ServiceProviderBuilder.builder()
                .addSingleton(IPlotManagerPlugin.class, (sp) -> this)
                .addSingleton(IConfigService.class, ConfigService.class)
                .addSingleton(IConfig.class, (sp) -> sp.getRequiredService(IConfigService.class))
                .addSingleton(IExceptionPublisher.class, ExceptionPublisher.class)
                .addSingleton(IProcedure.class, (sp) -> {
                    var type = sp.getRequiredService(IConfigService.class).getDatabaseType();
                    if (type == null) throw new RuntimeException("Database type is null.");
                    else if (type == DatabaseType.MYSQL) return sp.initialize(MySqlProcedures.class);
//                    else if (type == DatabaseType.YML) return sp.initialize(YmlDatabase.class);
                    else throw new RuntimeException("Unknown database type: " + type);
                })
                .addSingleton(IDatabase.class, (sp) -> {
                    var type = sp.getRequiredService(IConfigService.class).getDatabaseType();
                    if (type == null) throw new RuntimeException("Database type is null.");
                    else if (type == DatabaseType.MYSQL) return sp.initialize(MySqlDatabase.class);
                    else if (type == DatabaseType.YML) return sp.initialize(YmlDatabase.class);
                    else throw new RuntimeException("Unknown database type: " + type);
                })
                .addTransient(IServiceTransaction.class, ServiceTransaction.class)
                .addTransient(IUnitOfWork.class, UnitOfWork.class)
                .build(this);

        serviceProvider.getRequiredService(IDatabase.class).createDatabases();

        serviceProvider.initialize(AttributeCommands.class);
        serviceProvider.initialize(PlotManagerListener.class);

        insertWorlds();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public IServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    private void insertWorlds() {
        getLogger().info("Loading worlds...");
        try (var transaction = serviceProvider.getRequiredService(IServiceTransaction.class)) {
            var worldService = transaction.getService(IWorldService.class);

            //load database worlds into cache
            await(worldService.getWorlds());

            var userService = transaction.getService(IUserService.class);
            var system = await(userService.getSystemUser()).getOrThrow();

            Bukkit.getWorlds().forEach(w -> await(worldService.getWorldByUuid(w.getUID()).thenApplyAsync(wRes -> {
                if (!wRes.successful()) return await(worldService.createWorld(system.getUserId(), w)).getOrThrow();
                else {
                    var world = wRes.getOrThrow();
                    if (!world.getWorldName().equals(w.getName())) world = await(worldService.updateWorld(system.getUserId(), w.getUID(), w.getName())).getOrThrow();
                    return world;
                }
            })));

        } catch (Exception e) {
            serviceProvider.getRequiredService(IExceptionPublisher.class).publishException(e);
        }
        getLogger().info("World loading complete!");
    }

}
