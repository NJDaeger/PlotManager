package com.njdaeger.taskmanager.servicelibrary.services;

import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.taskmanager.dataaccess.DatabaseType;

public interface IConfigService extends IConfig {

    DatabaseType getDatabaseType();
//
//    List<String> getRequiredPlotAttributes();
//
//    Map<String, String> getRequiredPlotAttributeDefaults();
//
//    AttributeType getAttributeType(String name);
//
//    List<AttributeType> getAttributeTypes();
//
//    List<AttributeType> getDefaultAttributeTypes();

    boolean isSetupMode();

}
