package com.njdaeger.plotmanager.plugin.commands;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;

import java.util.function.Predicate;

public class CommandContextWrapper extends CommandContext {

    public CommandContextWrapper(CommandContext context) {
        super(context.getPlugin(), context.getCommand(), context.getSender(), context.getAlias(), context.getRawCommandString().split(" "));
    }

    public boolean subCommandAt(int index, TransactionalCommandExecutor executor) throws PDKCommandException {
        return super.subCommandAt(index, executor);
    }

    public boolean subCommandAt(int index, String match, boolean ignoreCase, TransactionalCommandExecutor executor) throws PDKCommandException {
        return super.subCommandAt(index, match, ignoreCase, executor);
    }

    public boolean subCommand(Predicate<CommandContext> predicate, TransactionalCommandExecutor executor) throws PDKCommandException {
        return super.subCommand(predicate, executor);
    }
}
