package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;
import com.njdaeger.plotmanager.servicelibrary.models.World;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;

import java.util.function.Predicate;

public class WorldFlag extends Flag<World> {

    private final ICacheService cacheService;

    public WorldFlag(ICacheService cacheService, Predicate<TabContext> onlyAllowWhen) {
        super(onlyAllowWhen, World.class, "World to search in", "-world <world>", "world");
        this.cacheService = cacheService;
    }

    @Override
    public World parse(CommandContext context, String argument) throws PDKCommandException {
        if (argument == null || argument.isBlank()) return null;
        return cacheService.getWorldCache().values().stream().filter(world -> world.getWorldName().equalsIgnoreCase(argument)).findFirst().orElse(null);
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        context.completion(cacheService.getWorldCache().values().stream().map(World::getWorldName).toArray(String[]::new));
    }
}
