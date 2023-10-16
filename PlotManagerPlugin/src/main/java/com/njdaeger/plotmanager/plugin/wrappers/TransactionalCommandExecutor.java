package com.njdaeger.plotmanager.plugin.wrappers;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.CommandExecutor;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.plotmanager.plugin.IPlotManagerPlugin;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;
import org.bukkit.Bukkit;

public interface TransactionalCommandExecutor extends CommandExecutor {

    @Override
    default void execute(CommandContext context) {
        var outer = this;
        Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
            var sp = ((IPlotManagerPlugin) context.getPlugin()).getServiceProvider();
            try (var transaction = sp.getRequiredService(IServiceTransaction.class)) {
                outer.execute(transaction, new CommandContextWrapper(context));
            } catch (PDKCommandException e) {
                e.showError(context.getSender());
            } catch (Exception e) {
                sp.getRequiredService(IPluginLogger.class).exception(e);
            }
        });
    }

    void execute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException;

}
