package com.njdaeger.taskmanager.plugin;

import com.njdaeger.serviceprovider.IServiceProvider;
import org.bukkit.plugin.Plugin;

public interface ITaskManagerPlugin extends Plugin {

    IServiceProvider getServiceProvider();

}
