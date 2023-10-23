package com.njdaeger.plotmanager.servicelibrary.services;

import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.servicelibrary.models.AttributeType;

import java.util.List;
import java.util.Map;

public interface IConfigService extends IConfig {

    DatabaseType getDatabaseType();

    List<String> getRequiredPlotAttributes();

    Map<String, String> getRequiredPlotAttributeDefaults();

    AttributeType getAttributeType(String name);

    List<AttributeType> getAttributeTypes();

    List<AttributeType> getDefaultAttributeTypes();

    boolean isSetupMode();

}
