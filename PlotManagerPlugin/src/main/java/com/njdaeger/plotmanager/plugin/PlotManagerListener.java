package com.njdaeger.plotmanager.plugin;

import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.plotmanager.servicelibrary.services.IUserService;
import com.njdaeger.serviceprovider.IServiceProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class PlotManagerListener implements Listener {

    private final IServiceProvider provider;
    private final IPluginLogger logger;
    private final IPlotManagerPlugin plugin;

    public PlotManagerListener(IPlotManagerPlugin plugin, IPluginLogger logger, IServiceProvider provider) {
        this.provider = provider;
        this.logger = logger;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        try (var transaction = provider.getRequiredService(IServiceTransaction.class)) {
            var userService = transaction.getService(IUserService.class);

            var system = await(userService.getSystemUser()).getOrThrow();

            var userSearch = await(userService.getUserByUuid(e.getPlayer().getUniqueId()));
            if (userSearch.successful()) {
                var username = userSearch.getOrThrow().getLastKnownName();
                if (!username.equals(e.getPlayer().getName())) {
                    userService.updateUsername(system.getUserId(), e.getPlayer().getUniqueId(), e.getPlayer().getName());
                }
            } else {
                userService.createUser(system.getUserId(), e.getPlayer().getUniqueId(), e.getPlayer().getName()).whenCompleteAsync((r, t) -> {
                    if (t != null) logger.exception(new RuntimeException(t));
                    else  plugin.getLogger().info("Created user " + e.getPlayer().getUniqueId() + " with id " + r.getOrThrow().getUserId());
                });
            }
        } catch (Exception ex) {
            logger.exception(ex);
        }
    }
}
