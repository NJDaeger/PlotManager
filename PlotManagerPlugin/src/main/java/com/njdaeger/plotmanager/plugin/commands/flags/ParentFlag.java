package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;

import java.util.function.Predicate;

public class ParentFlag extends BasePlotFlag {

    public ParentFlag(ICacheService cacheService, Predicate<TabContext> onlyAllowWhen) {
        super(cacheService, onlyAllowWhen, "Set the parent of this plot", "-parent <plotId>", "parent");
    }
}
