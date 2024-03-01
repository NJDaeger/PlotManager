package com.njdaeger.taskmanager.plugin.commands.wrappers;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.taskmanager.plugin.ITaskManagerPlugin;
import com.njdaeger.taskmanager.servicelibrary.models.User;
import com.njdaeger.taskmanager.servicelibrary.services.IUserService;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.serviceprovider.IServiceProvider;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.function.Predicate;

import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class CommandContextWrapper extends CommandContext {

    private IServiceTransaction transaction;

    public CommandContextWrapper(CommandContext context, IServiceTransaction transaction) throws NoSuchFieldException, IllegalAccessException {
        super(context.getPlugin(), context.getCommand(), context.getSender(), context.getAlias(), context.getRawCommandString().split(" "));
        copyContext(context);
        this.transaction = transaction;
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

    public IServiceProvider getServiceProvider() {
        return ((ITaskManagerPlugin)getPlugin()).getServiceProvider();
    }

    public IServiceTransaction getTransaction() {
        return transaction;
    }

    public User resolveUser(String username) throws PDKCommandException {
        var userService = transaction.getService(IUserService.class);
        var usersFound = await(userService.getUserByName(username)).getOrThrow(new PDKCommandException("No users found with provided name."));
        if (usersFound.isEmpty()) error("No user found with the username " + username + ".");
        if (usersFound.size() > 1) usersFound = usersFound.stream().filter(u -> Bukkit.getServer().getOfflinePlayer(u.getUserId()).isOnline()).toList();
        if (usersFound.isEmpty()) error("There have been multiple users last seen with username " + username + ", but none of them are online. Have that user join the server continue your action.");

        return usersFound.get(0);
    }

}
