package com.njdaeger.taskmanager.plugin.commands.wrappers;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.CommandExecutor;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.taskmanager.plugin.ITaskManagerPlugin;
import com.njdaeger.taskmanager.servicelibrary.ColorUtils;
import com.njdaeger.taskmanager.servicelibrary.ToastNotification;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface TransactionalCommandExecutor extends CommandExecutor {

    DateFormat YMDHMS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    default void execute(CommandContext context) {
        Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
            var sp = ((ITaskManagerPlugin) context.getPlugin()).getServiceProvider();
            try (var transaction = sp.getRequiredService(IServiceTransaction.class)) {
                this.execute(transaction, new CommandContextWrapper(context, transaction));
            } catch (PDKCommandException e) {
                if (context.isPlayer()) ToastNotification.error(Text.of(e.getMessage()).setColor(ColorUtils.ERROR_TEXT), context.getPlugin()).showTo(context.asPlayer());
                else Text.of(e.getMessage()).setColor(ColorUtils.ERROR_TEXT).sendTo(context.getSender());
            } catch (Exception e) {
                var at = YMDHMS_FORMAT.format(System.currentTimeMillis());
                Text.of( "Time: " + at + " -- There was an uncaught exception while executing this command. Please report this to an administrator.").setColor(ChatColor.RED).sendTo(context.getSender());
                sp.getRequiredService(IPluginLogger.class).exception(e);
            }
        });
    }

    void execute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException;

}
