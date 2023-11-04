package com.njdaeger.plotmanager.plugin;

import com.njdaeger.plotmanager.plugin.commands.AttributeCommands;
import com.njdaeger.plotmanager.plugin.commands.PlotCommands;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IMarkerService;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.CacheService;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.ConfigValidationService;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.InitializationService;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.MarkerService;
import com.njdaeger.pluginlogger.PluginLogger;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.dataaccess.IDatabase;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.databases.mysql.MySqlDatabase;
import com.njdaeger.plotmanager.dataaccess.databases.mysql.MySqlProcedures;
import com.njdaeger.plotmanager.dataaccess.databases.yml.YmlDatabase;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.dataaccess.transactional.UnitOfWork;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.plotmanager.servicelibrary.services.IWorldService;
import com.njdaeger.plotmanager.servicelibrary.transactional.ServiceTransaction;
import com.njdaeger.plotmanager.servicelibrary.services.implementations.ConfigService;
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
                .addSingleton(IPluginLogger.class, PluginLogger.class)
                .addSingleton(ICacheService.class, CacheService.class)
                .addSingleton(IMarkerService.class, MarkerService.class)
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

        var res = serviceProvider.getRequiredService(IDatabase.class).initializeDatabase();
        if (!res) {

            getLogger().severe("""
                    
                    ================================================
                    ==================== NOTICE ====================
                    Failed to initialize database. Additional plugin features will not be enabled until this is resolved.
                    
                    Please check your database configuration in PlotManager/config.yml and try again once it is corrected.
                    ==================== NOTICE ====================
                    ================================================
                    
                    """);
            return;
        }

        serviceProvider.initialize(InitializationService.class).run();
        serviceProvider.initialize(ConfigValidationService.class).run();
        serviceProvider.initialize(AttributeCommands.class);
        serviceProvider.initialize(PlotCommands.class);
        serviceProvider.initialize(PlotManagerListener.class);
    }

    @Override
    public void onDisable() {
        try {
            serviceProvider.getRequiredService(IPluginLogger.class).close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IServiceProvider getServiceProvider() {
        return serviceProvider;
    }

}
