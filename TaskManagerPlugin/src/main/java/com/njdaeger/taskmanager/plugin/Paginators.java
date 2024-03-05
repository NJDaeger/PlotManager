package com.njdaeger.taskmanager.plugin;

import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.taskmanager.plugin.commands.help.HelpChatListItem;
import com.njdaeger.taskmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.taskmanager.servicelibrary.ColorUtils;
import org.bukkit.map.MinecraftFont;

public class Paginators {

    public static final ChatPaginator<HelpChatListItem, CommandContextWrapper> helpMenuPaginator = ChatPaginator.<HelpChatListItem, CommandContextWrapper>builder((item, ctx) -> {
                var line = Text.of("| ").setColor(ColorUtils.GRAYED_TEXT).setBold(true);

                var command = item.getCommand();
                var twoArgs = command.trim().contains(" ");
                var isFlag = command.startsWith("-");
                if (twoArgs) {
                    var cmd = Text.of(command.split(" ")[0]).setColor(item.getSubCommands().isEmpty() ? ColorUtils.HIGHLIGHT_TEXT: ColorUtils.ACTION_TEXT);
                    if (isFlag) cmd.setItalic(true).setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This is a flag.").setColor(ColorUtils.REGULAR_TEXT));
                    cmd.appendRoot(" ");
                    var arg = command.split(" ")[1];
                    cmd = cmd.appendRoot(arg).setColor(item.getSubCommands().isEmpty() ? ColorUtils.HIGHLIGHT_TEXT: ColorUtils.ACTION_TEXT);
                    if (isFlag) cmd.setItalic(true);
                    if (arg.startsWith("<")) cmd.setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This is required.").setColor(ColorUtils.REGULAR_TEXT));
                    else if (arg.startsWith("[")) cmd.setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This is optional.").setColor(ColorUtils.REGULAR_TEXT));
                    if (item.getSubCommands().isEmpty()) line.appendRoot(cmd);
                    else line.appendRoot(Text.of("").append(cmd).setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot help " + item.getHelpCommandPath())).setHoverEvent(HoverAction.SHOW_TEXT, Text.of("View subcommands").setColor(ColorUtils.REGULAR_TEXT)));
                } else {
                    line = line.appendRoot(command).setColor(ColorUtils.HIGHLIGHT_TEXT);
                    if (isFlag) line.setItalic(true).setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This is a flag.").setColor(ColorUtils.REGULAR_TEXT));
                    if (command.startsWith("<")) line.setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This is required.").setColor(ColorUtils.REGULAR_TEXT));
                    else if (command.startsWith("[")) line.setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This is optional.").setColor(ColorUtils.REGULAR_TEXT));
                    if (!item.getSubCommands().isEmpty()) line.setColor(ColorUtils.ACTION_TEXT).setItalic(true).setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot help " + item.getHelpCommandPath())).setHoverEvent(HoverAction.SHOW_TEXT, Text.of("View subcommands").setColor(ColorUtils.REGULAR_TEXT));
                    else line.setColor(ColorUtils.HIGHLIGHT_TEXT);
                }
                line.appendRoot(" - ").setColor(ColorUtils.GRAYED_TEXT);
                var desc = trimToLength(item.getDescription().getText(), 210);
                if (!desc.equalsIgnoreCase(item.getDescription().getText())) {
                    line.appendRoot(trimToLength(desc, 210)).setColor(ColorUtils.REGULAR_TEXT)
                            .appendRoot("...").setColor(ColorUtils.ACTION_TEXT).setItalic(true)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(item.getDescription().getText()).setColor(ColorUtils.REGULAR_TEXT));
                } else line.appendRoot(desc).setColor(ColorUtils.REGULAR_TEXT);

                return line;
            })
            .addComponent(Text.of("Plot Command Help").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
            .addComponent(new PageNavigationComponent<>(
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
            ), ComponentPosition.BOTTOM_CENTER)
            .setGrayColor(ColorUtils.REGULAR_TEXT)
            .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
            .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
            .build();

    private static String trimToLength(String input, int maxPixels) {
        var pixelWidth = MinecraftFont.Font.getWidth(input);
        if (pixelWidth <= maxPixels) return input;
        var trimmed = input;
        while (pixelWidth > maxPixels) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
            pixelWidth = MinecraftFont.Font.getWidth(trimmed);
        }
        return trimmed;
    }

}
