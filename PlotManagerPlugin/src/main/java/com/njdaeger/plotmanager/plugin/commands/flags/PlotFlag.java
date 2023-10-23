package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;

import java.util.function.Predicate;

public class PlotFlag extends BasePlotFlag {

    public PlotFlag(ICacheService cacheService, Predicate<TabContext> onlyAllowWhen) {
        super(cacheService, onlyAllowWhen, "Which plot to edit", "-plot <plotId>", "plot");
    }
}
