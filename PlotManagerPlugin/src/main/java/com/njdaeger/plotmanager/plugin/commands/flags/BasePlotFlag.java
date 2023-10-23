package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;

import java.util.function.Predicate;

public class BasePlotFlag extends Flag<Plot> {

    private final ICacheService cacheService;

    public BasePlotFlag(ICacheService cacheService, Predicate<TabContext> onlyAllowWhen, String description, String usage, String aliases) {
        super(onlyAllowWhen, Plot.class, description, usage, aliases);
        this.cacheService = cacheService;
    }

    @Override
    public Plot parse(CommandContext context, String argument) throws PDKCommandException {
        if (argument == null || argument.isBlank()) return null;
        int parsed;
        try {
            parsed = Integer.parseInt(argument);
        } catch (NumberFormatException ignored) {
            return null;
        }
        return cacheService.getPlotCache().get(parsed);
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        context.completion(cacheService.getPlotCache().keySet().stream().map(Object::toString).toArray(String[]::new));
    }
}
