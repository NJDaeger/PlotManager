package com.njdaeger.plotmanager.service.impl;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.service.IConfigService;
import org.bukkit.plugin.Plugin;

public class ConfigService extends Configuration implements IConfigService {

    public ConfigService(Plugin plugin) {
        super(plugin, ConfigType.YML, "config");

        addEntry("database.type", DatabaseType.MARIADB.name());
        addEntry("database.credentials.host", "localhost");
        addEntry("database.credentials.port", 3306);
        addEntry("database.credentials.name", "databasename");
        addEntry("database.credentials.user", "username");
        addEntry("database.credentials.password", "password");
        save();
    }

    @Override
    public DatabaseType getDatabaseType() {
        return getValueAs("database.type", DatabaseType.class);
    }
}
