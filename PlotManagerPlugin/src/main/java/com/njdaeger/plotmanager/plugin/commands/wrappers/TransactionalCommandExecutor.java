package com.njdaeger.plotmanager.plugin.commands.wrappers;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.CommandExecutor;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.plotmanager.plugin.IPlotManagerPlugin;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public interface TransactionalCommandExecutor extends CommandExecutor {

    @Override
    default void execute(CommandContext context) {
        Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
            var sp = ((IPlotManagerPlugin) context.getPlugin()).getServiceProvider();
            try (var transaction = sp.getRequiredService(IServiceTransaction.class)) {
                this.execute(transaction, new CommandContextWrapper(context));
            } catch (PDKCommandException e) {
                context.send(ChatColor.RED + e.getMessage());
            } catch (Exception e) {
                sp.getRequiredService(IPluginLogger.class).exception(e);
            }
        });
    }

    void execute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException;

}
