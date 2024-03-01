package com.njdaeger.taskmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;
import com.njdaeger.taskmanager.servicelibrary.models.User;
import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;

import java.util.function.Predicate;

public class UserFlag extends Flag<User> {

    private final ICacheService cacheService;

    public UserFlag(ICacheService cacheService, Predicate<TabContext> onlyAllowWhen) {
        super(onlyAllowWhen, "Specify a user.", "-user <userName>", "user");
        this.cacheService = cacheService;
    }

    @Override
    public User parse(CommandContext context, String argument) throws PDKCommandException {
        if (argument == null || argument.isBlank()) return null;
        return cacheService.getUserCache().values().stream().filter(user -> user.getLastKnownName().equalsIgnoreCase(argument)).findFirst().orElse(null);
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        context.completion(cacheService.getUserCache().values().stream().map(User::getLastKnownName).toArray(String[]::new));
    }
}
