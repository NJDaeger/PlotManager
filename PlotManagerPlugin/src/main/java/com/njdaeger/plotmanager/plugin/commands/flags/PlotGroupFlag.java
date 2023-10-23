package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;
import com.njdaeger.plotmanager.servicelibrary.models.PlotGroup;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;

import java.util.function.Predicate;

public class PlotGroupFlag extends Flag<PlotGroup> {

    private final ICacheService cacheService;

    public PlotGroupFlag(ICacheService cacheService, Predicate<TabContext> onlyAllowWhen) {
        super(onlyAllowWhen, PlotGroup.class, "What PlotGroup to use for this plot", "-group <plotGroupName>", "group");
        this.cacheService = cacheService;
    }

    @Override
    public PlotGroup parse(CommandContext context, String argument) throws PDKCommandException {
        if (argument == null || argument.isBlank()) return null;
        return cacheService.getGroupCache().get(argument);
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        context.completion(cacheService.getGroupCache().keySet().toArray(new String[0]));
    }
}
