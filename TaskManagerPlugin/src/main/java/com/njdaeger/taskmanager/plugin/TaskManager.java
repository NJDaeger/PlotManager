package com.njdaeger.taskmanager.plugin;

import com.njdaeger.taskmanager.plugin.commands.ProjectCommands;
import com.njdaeger.taskmanager.plugin.redblock.RedblockService;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.taskmanager.servicelibrary.services.IMarkerService;
import com.njdaeger.taskmanager.servicelibrary.services.implementations.CacheService;
import com.njdaeger.taskmanager.servicelibrary.services.implementations.ConfigValidationService;
import com.njdaeger.taskmanager.servicelibrary.services.implementations.InitializationService;
import com.njdaeger.taskmanager.servicelibrary.services.implementations.MarkerService;
import com.njdaeger.pluginlogger.PluginLogger;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.taskmanager.dataaccess.DatabaseType;
import com.njdaeger.taskmanager.dataaccess.IDatabase;
import com.njdaeger.taskmanager.dataaccess.IProcedure;
import com.njdaeger.taskmanager.dataaccess.databases.mysql.MySqlDatabase;
import com.njdaeger.taskmanager.dataaccess.databases.mysql.MySqlProcedures;
import com.njdaeger.taskmanager.dataaccess.databases.yml.YmlDatabase;
import com.njdaeger.taskmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.taskmanager.dataaccess.transactional.UnitOfWork;
import com.njdaeger.taskmanager.servicelibrary.services.IConfigService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.taskmanager.servicelibrary.transactional.ServiceTransaction;
import com.njdaeger.taskmanager.servicelibrary.services.implementations.ConfigService;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.serviceprovider.ServiceProviderBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class TaskManager extends JavaPlugin implements ITaskManagerPlugin {

    private IServiceProvider serviceProvider;

    @Override
    public void onEnable() {

        this.serviceProvider = ServiceProviderBuilder.builder()
                .addSingleton(ITaskManagerPlugin.class, (sp) -> this)
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
                    
                    Please check your database configuration in TaskManager/config.yml and try again once it is corrected.
                    ==================== NOTICE ====================
                    ================================================
                    
                    """);
            return;
        }

        serviceProvider.initialize(InitializationService.class).run();
        serviceProvider.initialize(ConfigValidationService.class).run();
        serviceProvider.initialize(TaskManagerListener.class);
        serviceProvider.initialize(ProjectCommands.class);

        serviceProvider.initialize(RedblockService.class);
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
