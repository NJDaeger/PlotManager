package com.njdaeger.plotmanager.servicelibrary.services;

import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.servicelibrary.models.AttributeType;

import java.util.List;

public interface IConfigService extends IConfig {

    DatabaseType getDatabaseType();

    List<AttributeType> getRequiredPlotAttributes();

    AttributeType getAttributeType(String name);

    List<AttributeType> getAttributeTypes();

    List<AttributeType> getDefaultAttributeTypes();

    boolean isSetupMode();

}
