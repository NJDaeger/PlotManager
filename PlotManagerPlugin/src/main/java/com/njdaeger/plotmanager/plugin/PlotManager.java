package com.njdaeger.plotmanager.plugin;

import com.njdaeger.exceptionpublisher.ExceptionPublisher;
import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.dataaccess.IDatabase;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.impl.mariadb.MariaDbDatabase;
import com.njdaeger.plotmanager.dataaccess.impl.mariadb.MariaDbProcedures;
import com.njdaeger.plotmanager.dataaccess.impl.yml.YmlDatabase;
import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;
import com.njdaeger.plotmanager.dataaccess.transactional.UnitOfWork;
import com.njdaeger.plotmanager.service.IConfigService;
import com.njdaeger.plotmanager.service.impl.ConfigService;
import com.njdaeger.serviceprovider.IServiceProvider;
import com.njdaeger.serviceprovider.ServiceProviderBuilder;
import org.bukkit.plugin.java.JavaPlugin;

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
                    else if (type == DatabaseType.MARIADB) return sp.initialize(MariaDbProcedures.class);
//                    else if (type == DatabaseType.YML) return sp.initialize(YmlDatabase.class);
                    else throw new RuntimeException("Unknown database type: " + type);
                })
                .addSingleton(IDatabase.class, (sp) -> {
                    var type = sp.getRequiredService(IConfigService.class).getDatabaseType();
                    if (type == null) throw new RuntimeException("Database type is null.");
                    else if (type == DatabaseType.MARIADB) return sp.initialize(MariaDbDatabase.class);
                    else if (type == DatabaseType.YML) return sp.initialize(YmlDatabase.class);
                    else throw new RuntimeException("Unknown database type: " + type);
                })
                .addTransient(IUnitOfWork.class, UnitOfWork.class)
                .build(this);

        serviceProvider.initialize(AttributeCommands.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public IServiceProvider getServiceProvider() {
        return serviceProvider;
    }
}
