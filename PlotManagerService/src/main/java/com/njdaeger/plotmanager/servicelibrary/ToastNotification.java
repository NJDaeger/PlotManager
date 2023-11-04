package com.njdaeger.plotmanager.servicelibrary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.njdaeger.pdk.utils.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

//
// Credit for this class goes to https://github.com/MrSweeter/DreamAPI/
//
public class ToastNotification {

    private final NamespacedKey id;
    private final String icon;
    private final Text.Section title;
    private final String description;
    private final Plugin pl;

    public static ToastNotification error(String message, Plugin pl) {
        return error(Text.of(message), pl);
    }

    public static ToastNotification error(Text.Section message, Plugin pl) {
        return new ToastNotification(new NamespacedKey(pl, UUID.randomUUID().toString()), message, "minecraft:barrier", pl);
    }

    public static ToastNotification success(String message, Plugin pl) {
        return success(Text.of(message), pl);
    }

    public static ToastNotification success(Text.Section message, Plugin pl) {
        return new ToastNotification(new NamespacedKey(pl, UUID.randomUUID().toString()), message, "minecraft:totem_of_undying", pl);
    }

    /**
     * Create a Toast/Advancement display (Top right corner)
     * @param title Message to show/send
     * @param icon minecraft id of display item (minecraft:...)
     * @param pl Your plugin instance
     */
    public ToastNotification(Text.Section title, String icon, Plugin pl)	{
        this(new NamespacedKey(pl, UUID.randomUUID().toString()), title, icon, pl);
    }

    /**
     * Create a Toast/Advancement display (Top right corner)
     * @param id A unique id for this Advancement, will be the name if advancement file
     * @param title Message to show/send
     * @param icon minecraft id of display item (minecraft:...)
     * @param pl Your plugin instance
     */
    public ToastNotification(NamespacedKey id, Text.Section title, String icon, Plugin pl) {
        this.id = id;
        this.title = title;
        this.description = "§7This Toast was created with DreamAPI";
        this.icon = icon;
        this.pl = pl;
    }

    public void showTo(Player... player)	{
        showTo(Arrays.asList(player));
    }

    public void showTo(Collection<? extends Player> players)	{
        Bukkit.getScheduler().runTask(pl, () -> {
            add();
            grant(players);
            Bukkit.getScheduler().runTaskLater(pl, () -> {
                revoke(players);
                remove();
            }, 20);
        });
    }

    @SuppressWarnings("deprecation")
    private void add()	{
        try {
            Bukkit.getUnsafe().loadAdvancement(id, getJson());
        } catch (IllegalArgumentException ignored){
        }
    }

    @SuppressWarnings("deprecation")
    private void remove()	{
        Bukkit.getUnsafe().removeAdvancement(id);
    }

    private void grant(Collection<? extends Player> players) {
        Advancement advancement = Bukkit.getAdvancement(id);
        AdvancementProgress progress;
        for (Player player : players)	{

            progress = player.getAdvancementProgress(advancement);
            if (!progress.isDone())	{
                for (String criteria : progress.getRemainingCriteria())	{
                    progress.awardCriteria(criteria);
                }
            }
        }
    }

    private void revoke(Collection<? extends Player> players)	{
        Advancement advancement = Bukkit.getAdvancement(id);
        AdvancementProgress progress;
        for (Player player : players)	{

            progress = player.getAdvancementProgress(advancement);
            if (progress.isDone())	{
                for (String criteria : progress.getAwardedCriteria())	{
                    progress.revokeCriteria(criteria);
                }
            }
        }
    }

    public String getJson()	{

        JsonObject json = new JsonObject();

        JsonObject icon = new JsonObject();
        icon.addProperty("item", this.icon);

        JsonObject display = new JsonObject();
        display.add("icon", icon);
        display.add("title", title.getJson());
        display.addProperty("description", this.description);
        display.addProperty("background", "minecraft:textures/gui/advancements/backgrounds/adventure.png");
        display.addProperty("frame", "goal");
        display.addProperty("announce_to_chat", false);
        display.addProperty("show_toast", true);
        display.addProperty("hidden", true);

        JsonObject criteria = new JsonObject();
        JsonObject trigger = new JsonObject();

        trigger.addProperty("trigger", "minecraft:impossible");
        criteria.add("impossible", trigger);

        json.add("criteria", criteria);
        json.add("display", display);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(json);

    }

}
