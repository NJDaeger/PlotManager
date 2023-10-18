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

    private final List<AttributeType> defaultTypes;

    public ConfigService(Plugin plugin) {
        super(plugin, ConfigType.YML, "config");

        this.defaultTypes = new ArrayList<>(){{
            add(new AttributeType("word", List.of(), (obj) -> !obj.toString().contains(" "))); //single word, no spaces
            add(new AttributeType("string", List.of(), (obj) -> true)); //any string
            add(new AttributeType("integer", List.of(), (obj) -> {//i understand that this a long, end users understand what an integer is more than they do the difference between an integer and a long
                try {
                    Long.parseLong(obj.toString());
                    return true;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }));
            add(new AttributeType("decimal", List.of(), (obj) -> {
                try {
                    Double.parseDouble(obj.toString());
                    return true;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }));
            add(new AttributeType("boolean", List.of(), (obj) -> {
                var str = obj.toString().toLowerCase();
                return str.equals("true") || str.equals("false");
            }));
        }};

        addEntry("setup-mode", true);
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
                "Primitive java types are supported by default. word (single word), string (any length of text), integer (any non decimal number), decimal (decimal or integer), boolean (true or false)");
        addEntry("attributes.types.status", List.of("Hold", "Draft", "Plotted", "Ongoing", "Review", "Complete"));
        addEntry("attributes.types.rank", List.of("Apprentice", "Worker", "Builder", "Architect", "Supervisor", "Admin"));
        addEntry("attributes.types.points", List.of("1", "2", "3", "5", "8", "13", "21"));
        addEntry("attributes.types.building-type", List.of("Apartment Complex", "Strip Mall", "Office Building", "House", "Store Interior", "Undefined"));
        addEntry("plots.required-attributes", List.of("status", "rank", "points", "building-type", "description", "floors"));
        addEntry("plots.required-attribute-defaults", List.of("status:Hold", "rank:Apprentice", "points:1", "building-type:Undefined", "floors:1"));

        save();
    }

    @Override
    public boolean isSetupMode() {
        return getValueAs("setup-mode", Boolean.class);
    }

    @Override
    public DatabaseType getDatabaseType() {
        return getValueAs("database.type", DatabaseType.class);
    }

    @Override
    public List<AttributeType> getRequiredPlotAttributes() {
        var required = getStringList("plots.attributes.required");
        var types = new ArrayList<AttributeType>();
        for (var requiredAttribute : required) {
            types.add(getAttributeType(requiredAttribute));
        }
        return types;
    }

    @Override
    public AttributeType getAttributeType(String name) {
        var defType = defaultTypes.stream().filter(t -> t.getName().equalsIgnoreCase(name)).findFirst();
        if (defType.isPresent()) return defType.get();
        var list = getValue("attributes.types." + name);
        if (list == null) return null;
        return new AttributeType(name, getStringList("attributes.types." + name));
    }

    @Override
    public List<AttributeType> getAttributeTypes() {
        var typeNames = getSection("attributes.types").getKeys(false);
        var types = new ArrayList<>(defaultTypes);
        for (var typeName : typeNames) {
            var values = getStringList("attributes.types." + typeName);
            types.add(new AttributeType(typeName, values));
        }
        return types;
    }

    @Override
    public List<AttributeType> getDefaultAttributeTypes() {
        return defaultTypes;
    }
}
