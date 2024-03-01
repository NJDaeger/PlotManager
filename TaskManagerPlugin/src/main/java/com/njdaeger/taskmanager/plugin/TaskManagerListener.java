package com.njdaeger.taskmanager.plugin;

import com.njdaeger.taskmanager.servicelibrary.services.ICacheService;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.taskmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.taskmanager.servicelibrary.services.IUserService;
import com.njdaeger.serviceprovider.IServiceProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.njdaeger.taskmanager.dataaccess.Util.async;
import static com.njdaeger.taskmanager.dataaccess.Util.await;

public class TaskManagerListener implements Listener {

    private final IServiceProvider provider;
    private final IPluginLogger logger;
    private final ITaskManagerPlugin plugin;
    private final ICacheService cacheService;

    public TaskManagerListener(ITaskManagerPlugin plugin, IPluginLogger logger, ICacheService cacheService, IServiceProvider provider) {
        this.provider = provider;
        this.logger = logger;
        this.plugin = plugin;
        this.cacheService = cacheService;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        async(() -> {
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
            return null;
        });
    }

//    @EventHandler
//    public void onPlotSignClick(PlayerInteractEvent e) {
//        //if the block clicked is the sign above a plot location, give the player the plot info by making them run the command /plot info -plot <plotId>
//        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getBlockData() instanceof Sign) {
//            cacheService.getPlotCache().values().stream().filter((p) -> !p.isDeleted() && p.getLocation().clone().add(0, 1, 0).equals(e.getClickedBlock().getLocation())).findFirst().ifPresent((p) -> e.getPlayer().performCommand("plot info -plot " + p.getId()));
//        }
//    }
//
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onBlockPlace(BlockPlaceEvent e) {
//        //if the block is placed on a plot location or on a plot sign, cancel the event
//        if (e.getBlockPlaced().getLocation().getBlock().getType() == Material.OAK_SIGN || e.getBlockPlaced().getLocation().getBlock().getType() == Material.DIAMOND_BLOCK) {
//            cacheService.getPlotCache().values().stream().filter((p) -> !p.isDeleted() &&
//                    p.getLocation().clone().add(0, 1, 0).equals(e.getBlock().getLocation()) ||
//                    p.getLocation().equals(e.getBlock().getLocation())).findFirst().ifPresent((p) -> {
//                        e.setCancelled(true);
//                        Text.of("You cannot edit a plot sign.").setColor(ColorUtils.ERROR_TEXT).sendTo(e.getPlayer());
//            });
//        }
//    }
//
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onPlotSignEdit(PlayerSignOpenEvent e) {
//        //if the block is placed on a plot location or on a plot sign, cancel the event
//        if (e.getSign().getBlock().getType() == Material.OAK_SIGN) {
//            cacheService.getPlotCache().values().stream().filter((p) -> !p.isDeleted() &&
//                    p.getLocation().clone().add(0, 1, 0).equals(e.getSign().getBlock().getLocation())).findFirst().ifPresent((p) -> {
//                        e.setCancelled(true);
//                        Text.of("You cannot edit a plot sign.").setColor(ColorUtils.ERROR_TEXT).sendTo(e.getPlayer());
//            });
//        }
//    }
//
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onBlockBreak(BlockBreakEvent e) {
//        //if the block is broken on a plot location or on a plot sign, cancel the event
//        if (e.getBlock().getType() == Material.OAK_SIGN || e.getBlock().getType() == Material.DIAMOND_BLOCK) {
//            cacheService.getPlotCache().values().stream().filter((p) -> !p.isDeleted() &&
//                    (p.getLocation().clone().add(0, 1, 0).equals(e.getBlock().getLocation()) ||
//                    p.getLocation().equals(e.getBlock().getLocation()))).findFirst().ifPresent((p) -> {
//                        e.setCancelled(true);
//                        Text.of("You cannot edit a plot sign.").setColor(ColorUtils.ERROR_TEXT).sendTo(e.getPlayer());
//            });
//        }
//    }


}
