package com.njdaeger.plotmanager.servicelibrary.services;

import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;

public interface IConfigService extends IConfig {

    DatabaseType getDatabaseType();

}
