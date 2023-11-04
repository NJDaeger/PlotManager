package com.njdaeger.plotmanager.servicelibrary.services.implementations;

import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.services.IMarkerService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.plugin.Plugin;

public class MarkerService implements IMarkerService {

    private final Plugin plugin;

    public MarkerService(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void createPlotMarker(Plot plot) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            var status = plot.getAttributeValueOrDefault("status", "Unknown");
            var type = Material.DIAMOND_BLOCK;
            if (status.equalsIgnoreCase("Unknown") || status.equalsIgnoreCase("Hold")) type = Material.RED_WOOL;
            else if (status.equalsIgnoreCase("Draft")) type = Material.GOLD_BLOCK;
            plot.getLocation().getBlock().setType(type);
            var signLocation = plot.getLocation().clone().add(0, 1, 0);
            signLocation.getBlock().setType(Material.OAK_SIGN);
            var sign = (org.bukkit.block.Sign)signLocation.getBlock().getState();
            sign.getSide(Side.BACK).setLine(0, ChatColor.BLUE + "Plot #" + plot.getId());
            sign.getSide(Side.FRONT).setLine(0, ChatColor.BLUE + "Plot #" + plot.getId());
            sign.getSide(Side.BACK).setLine(1, ChatColor.LIGHT_PURPLE.toString() + ChatColor.ITALIC + plot.getAttributeValueOrDefault("building-type", "Unknown"));
            sign.getSide(Side.FRONT).setLine(1, ChatColor.LIGHT_PURPLE.toString() + ChatColor.ITALIC + plot.getAttributeValueOrDefault("building-type", "Unknown"));
            sign.getSide(Side.BACK).setLine(2, ChatColor.BLACK.toString() + ChatColor.ITALIC + "Right click to");
            sign.getSide(Side.FRONT).setLine(2, ChatColor.BLACK.toString() + ChatColor.ITALIC + "Right click to");
            sign.getSide(Side.BACK).setLine(3, ChatColor.BLACK.toString() + ChatColor.ITALIC + "view plot info");
            sign.getSide(Side.FRONT).setLine(3, ChatColor.BLACK.toString() + ChatColor.ITALIC + "view plot info");
            sign.setWaxed(true);
            sign.getSide(Side.FRONT).setGlowingText(true);
            sign.getSide(Side.BACK).setGlowingText(true);
            var bd = ((Sign)signLocation.getBlock().getBlockData());
            signLocation.getBlock().setBlockData(bd, false);
            sign.update(true);
        });
    }

    @Override
    public void deletePlotMarker(Plot plot) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plot.getLocation().getBlock().setType(Material.AIR);
            var signLocation = plot.getLocation().clone().add(0, 1, 0);
            signLocation.getBlock().setType(Material.AIR);
        });
    }
}
