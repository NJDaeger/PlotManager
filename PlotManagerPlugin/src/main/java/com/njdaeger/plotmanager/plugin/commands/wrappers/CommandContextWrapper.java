package com.njdaeger.plotmanager.plugin.commands.wrappers;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.plotmanager.dataaccess.Util;

import java.util.UUID;
import java.util.function.Predicate;

public class CommandContextWrapper extends CommandContext {

    public CommandContextWrapper(CommandContext context) throws NoSuchFieldException, IllegalAccessException {
        super(context.getPlugin(), context.getCommand(), context.getSender(), context.getAlias(), context.getRawCommandString().split(" "));
        copyContext(context);
    }

    private void copyContext(CommandContext context) throws NoSuchFieldException, IllegalAccessException {
        var typesToStringsField = getClass().getSuperclass().getDeclaredField("typesToStrings");
        typesToStringsField.setAccessible(true);
        var typesToStrings = typesToStringsField.get(context);
        typesToStringsField.set(this, typesToStrings);

        var parsedFlagsField = getClass().getSuperclass().getDeclaredField("parsedFlags");
        parsedFlagsField.setAccessible(true);
        var parsedFlags = parsedFlagsField.get(context);
        parsedFlagsField.set(this, parsedFlags);

        var argsField = getClass().getSuperclass().getDeclaredField("args");
        argsField.setAccessible(true);
        var args = argsField.get(context);
        argsField.set(this, args);
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

    public UUID getUUID() {
        return isPlayer() ? asPlayer().getUniqueId() : Util.SYSTEM_UUID;
    }

    public void send(Text.Section text) {
        text.sendTo(getSender());
    }

    public String argAtOrThrow(int index, String message) throws PDKCommandException {
        var res = argAt(index);
        if (res == null) throw new PDKCommandException(message);
        return res;
    }

}
