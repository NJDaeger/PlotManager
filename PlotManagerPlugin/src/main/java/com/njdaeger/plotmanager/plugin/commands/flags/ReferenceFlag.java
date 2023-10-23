package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;

import java.util.function.Predicate;

public class ReferenceFlag extends BasePlotFlag {

    public ReferenceFlag(ICacheService cacheService, Predicate<TabContext> onlyAllowWhen) {
        super(cacheService, onlyAllowWhen, "Refer to another plot's attributes when creating this plot.", "-reference <plotId>", "reference");
    }
}
