package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.servicelibrary.models.AttributeType;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigService extends Configuration implements IConfigService {

    public ConfigService(Plugin plugin) {
        super(plugin, ConfigType.YML, "config");

        addEntry("logging.write-exceptions-to-file", true);
        addEntry("logging.debug", true);
        addEntry("database.type", DatabaseType.MYSQL.name());
        addEntry("database.connection-pool-size", 10);
        addEntry("database.credentials.host", "localhost");
        addEntry("database.credentials.port", 3306);
        addEntry("database.credentials.name", "databasename");
        addEntry("database.credentials.user", "username");
        addEntry("database.credentials.password", "password");
        addComment("attributes.types", "This section is used to define the custom attribute types.\n" +
                "Primitive java types are supported by default. Integer, Double, Float, Boolean, String, and Long.");
        addEntry("attributes.types.status", List.of("Hold", "Draft", "Plotted", "Ongoing", "Review", "Complete"));
        addEntry("attributes.types.rank", List.of("Apprentice", "Worker", "Builder", "Architect", "Supervisor", "Admin"));
        addEntry("attributes.types.points", List.of("1", "2", "3", "5", "8", "13", "21"));
        addEntry("attributes.types.building-type", List.of("Apartment Complex", "Strip Mall", "Office Building", "House", "Store Interior"));
        save();
    }

    @Override
    public DatabaseType getDatabaseType() {
        return getValueAs("database.type", DatabaseType.class);
    }

    @Override
    public AttributeType getAttributeType(String name) {
        if (hasSection("attributes.types." + name)) return null;
        var values = getStringList("attributes.types." + name);
        return new AttributeType(name, values);
    }

    @Override
    public List<AttributeType> getAttributeTypes() {
        var typeNames = getSection("attributes.types").getKeys(false);
        var types = new ArrayList<AttributeType>();
        for (var typeName : typeNames) {
            var values = getStringList("attributes.types." + typeName);
            types.add(new AttributeType(typeName, values));
        }
        return types;
    }
}
