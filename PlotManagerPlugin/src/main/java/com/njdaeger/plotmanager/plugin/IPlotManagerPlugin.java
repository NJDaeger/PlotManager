package com.njdaeger.plotmanager.plugin;

import com.njdaeger.serviceprovider.IServiceProvider;
import org.bukkit.plugin.Plugin;

public interface IPlotManagerPlugin extends Plugin {

    IServiceProvider getServiceProvider();

}
