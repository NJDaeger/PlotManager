package com.njdaeger.plotmanager.plugin.commands;

import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickEvent;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import com.njdaeger.plotmanager.plugin.commands.help.HelpChatListItem;
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.plotmanager.servicelibrary.ColorUtils;
import com.njdaeger.plotmanager.servicelibrary.PlotChatListItems;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotAttribute;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import org.bukkit.Color;
import org.bukkit.map.MinecraftFont;

import java.text.DecimalFormat;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public final class Paginators {

    private static final DecimalFormat df = new DecimalFormat("#.##");

    //region Parent Selector
    static final ChatPaginator<Plot, CommandContextWrapper> parentSelectorPaginator = ChatPaginator.<Plot, CommandContextWrapper>builder((plt, ctx) -> {
                var editSession = ctx.hasFlag("session");
                var plot = ctx.resolvePlotUnchecked();
                if (plot == null && !editSession) return Text.of("No plot found.").setColor(ColorUtils.ERROR_TEXT);
                return Text.of("| ").setColor(ColorUtils.GRAYED_TEXT).setBold(true)
                        .appendRoot(String.valueOf(plt.getId())).setColor(ColorUtils.HIGHLIGHT_TEXT)
                        .setHoverEvent(HoverAction.SHOW_TEXT,
                                Text.of("Description: ").setColor(ColorUtils.REGULAR_TEXT)
                                        .appendRoot(plt.getAttribute("description") == null ? "No description provided." : plt.getAttribute("description").getValue()).setColor(ColorUtils.HIGHLIGHT_TEXT)
                                        .appendRoot("\nLocation: ").setColor(ColorUtils.REGULAR_TEXT)
                                        .appendRoot(String.valueOf(plt.getLocation().getBlockX())).setColor(ColorUtils.HIGHLIGHT_TEXT)
                                        .appendRoot(", ").setColor(ColorUtils.REGULAR_TEXT)
                                        .appendRoot(String.valueOf(plt.getLocation().getBlockY())).setColor(ColorUtils.HIGHLIGHT_TEXT)
                                        .appendRoot(", ").setColor(ColorUtils.REGULAR_TEXT)
                                        .appendRoot(String.valueOf(plt.getLocation().getBlockZ())).setColor(ColorUtils.HIGHLIGHT_TEXT)
                                        .appendRoot("\nWorld: ").setColor(ColorUtils.REGULAR_TEXT)
                                        .appendRoot(plt.getLocation().getWorld().getName()).setColor(ColorUtils.HIGHLIGHT_TEXT))
                        .appendRoot(" ")
                        .appendRoot("[select]").setItalic(true).setUnderlined(true).setColor(ColorUtils.ACTION_TEXT)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to select this plot to be the parent.").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit parent set " + plt.getId() + (editSession ? " -session " : " -plot " + plot.getId())));
            })
            .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
            .addComponent(new PageNavigationComponent<>(
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
            ), ComponentPosition.BOTTOM_CENTER)
            .addComponent(Text.of("Parent Plot Selector").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
            .setGrayColor(ColorUtils.REGULAR_TEXT)
            .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
            .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
            .build();
    //endregion

    //region Attribute Selector
    static final ChatPaginator<String, CommandContextWrapper> attributeSelectListPaginator = ChatPaginator.<String, CommandContextWrapper>builder((value, ctx) -> {
                var editSession = ctx.hasFlag("session");
                var plot = ctx.resolvePlotUnchecked();
                if (plot == null && !editSession) return Text.of("No plot found.").setColor(ColorUtils.ERROR_TEXT);
                var cfg = ctx.getServiceProvider().getRequiredService(IConfigService.class);
                var attrSvc = ctx.getServiceProvider().getRequiredService(IAttributeService.class);
                var attr = await(attrSvc.getAttribute(value));
                var attrType = attr.successful() ? cfg.getAttributeType(attr.getOrThrow().getType()) : null;
                ClickEvent<?> clickEvent;
                if (!attr.successful() || attrType == null || attrType.getValues().isEmpty()) clickEvent = new ClickEvent<>(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot edit attribute " + value + " set " + (editSession ? " -session" : " -plot " + plot.getId())));
                else clickEvent = new ClickEvent<>(ClickAction.RUN_COMMAND, ClickString.of("/plot edit attribute " + value + " select " + (editSession ? " -session" : " -plot " + plot.getId())));
                return Text.of("| ").setColor(ColorUtils.GRAYED_TEXT).setBold(true)
                        .appendRoot(value).setColor(ColorUtils.HIGHLIGHT_TEXT)
                        .appendRoot(" ")
                        .appendRoot("[select]").setItalic(true).setUnderlined(true).setColor(ColorUtils.ACTION_TEXT)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to select this attribute").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(clickEvent);
            })
            .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
            .addComponent(new PageNavigationComponent<>(
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
            ), ComponentPosition.BOTTOM_CENTER)
            .addComponent(Text.of("Attribute Selector").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
            .setGrayColor(ColorUtils.REGULAR_TEXT)
            .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
            .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
            .build();
    //endregion

    //region Attribute Value Selector
    static final ChatPaginator<String, CommandContextWrapper> attributeValueSelectListPaginator = ChatPaginator.<String, CommandContextWrapper>builder((value, ctx) -> {

                var editSession = ctx.hasFlag("session");
                var plot = ctx.resolvePlotUnchecked();
                if (plot == null && !editSession) return Text.of("No plot found.").setColor(ColorUtils.ERROR_TEXT);
                return Text.of("| ").setColor(ColorUtils.GRAYED_TEXT).setBold(true)
                        .appendRoot(value).setColor(ColorUtils.HIGHLIGHT_TEXT)
                        .appendRoot(" ")
                        .appendRoot("[select]").setItalic(true).setUnderlined(true).setColor(ColorUtils.ACTION_TEXT)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to select this value for this attribute").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit attribute " + ctx.argAt(2) + " set " + value + (editSession ? " -session" : " -plot " + plot.getId())));
            })
            .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
            .addComponent(new PageNavigationComponent<>(
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
            ), ComponentPosition.BOTTOM_CENTER)
            .addComponent(Text.of("Attribute Value Selector").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
            .setGrayColor(ColorUtils.REGULAR_TEXT)
            .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
            .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
            .build();
    //endregion

    //region Plot Creation Menu
    static final ChatPaginator<PlotChatListItems.PlotChatListItem, CommandContextWrapper> plotCreationMenuPaginator = ChatPaginator.<PlotChatListItems.PlotChatListItem, CommandContextWrapper>builder((item, ctx) -> {
                var line = Text.of("| ").setColor(ColorUtils.GRAYED_TEXT).setBold(true);
                if (item.isRequired()) {
                    line.appendRoot("*").setColor(ColorUtils.ERROR_TEXT)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This attribute is required.").setColor(ColorUtils.REGULAR_TEXT));
                }
                if (!item.getName().isBlank())
                    line.appendRoot(item.getName()).setColor(ColorUtils.REGULAR_TEXT).appendRoot(": ").setColor(ColorUtils.REGULAR_TEXT);
                line.appendRoot(item.getValue());
                return line;
            })
            .addComponent(new PageNavigationComponent<>(
                    (ctx, res, pg) -> "/plot -session create -page " + 1,
                    (ctx, res, pg) -> "/plot -session create -page " + (pg - 1),
                    (ctx, res, pg) -> "/plot -session create -page " + (pg + 1),
                    (ctx, res, pg) -> "/plot -session create -page " + ((int) Math.ceil(res.size() / 8.0))
            ), ComponentPosition.BOTTOM_CENTER)
            .addComponent(Text.of("Plot Creation Menu").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
            .addComponent(Text.of("[Finish]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setBold(true)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to finish the plot creation.").setColor(ColorUtils.REGULAR_TEXT))
                    .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot finish")), ComponentPosition.BOTTOM_LEFT)
            .addComponent(Text.of("[Cancel]").setColor(ColorUtils.ERROR_TEXT).setUnderlined(true).setBold(true)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to cancel the plot creation.").setColor(ColorUtils.REGULAR_TEXT))
                    .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot cancel")), ComponentPosition.BOTTOM_RIGHT)
            .setGrayColor(ColorUtils.REGULAR_TEXT)
            .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
            .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
            .build();
    //endregion

    //region Plot Edit Menu
    static final ChatPaginator<PlotChatListItems.PlotChatListItem, CommandContextWrapper> plotEditMenuPaginator = ChatPaginator.<PlotChatListItems.PlotChatListItem, CommandContextWrapper>builder((item, ctx) -> {
                var line = Text.of("| ").setColor(ColorUtils.GRAYED_TEXT).setBold(true);
                if (item.isRequired()) {
                    line.appendRoot("*").setColor(ColorUtils.ERROR_TEXT)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This attribute is required.").setColor(ColorUtils.REGULAR_TEXT));
                }
                if (!item.getName().isBlank())
                    line.appendRoot(item.getName()).setColor(ColorUtils.REGULAR_TEXT).appendRoot(": ").setColor(ColorUtils.REGULAR_TEXT);
                line.appendRoot(item.getValue());
                return line;
            })
            .addComponent(new PageNavigationComponent<>(
                    (ctx, res, pg) -> "/plot edit -plot " + ctx.resolvePlotUnchecked().getId() + " -page " + 1,
                    (ctx, res, pg) -> "/plot edit -plot " + ctx.resolvePlotUnchecked().getId() + " -page " + (pg - 1),
                    (ctx, res, pg) -> "/plot edit -plot " + ctx.resolvePlotUnchecked().getId() + " -page " + (pg + 1),
                    (ctx, res, pg) -> "/plot edit -plot " + ctx.resolvePlotUnchecked().getId() + " -page " + ((int) Math.ceil(res.size() / 8.0))
            ), ComponentPosition.BOTTOM_CENTER)
            .addComponent(Text.of("Plot Edit Menu").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
//            .addComponent(Text.of("[Finish]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setBold(true)
//                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to finish the plot edit.").setColor(ColorUtils.REGULAR_TEXT))
//                    .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot finish")), ComponentPosition.BOTTOM_LEFT)
//            .addComponent(Text.of("[Cancel]").setColor(ColorUtils.ERROR_TEXT).setUnderlined(true).setBold(true
            .setGrayColor(ColorUtils.REGULAR_TEXT)
            .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
            .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
            .build();

    //endregion

    //region Plot Info Menu
    static final ChatPaginator<PlotChatListItems.PlotChatListItem, CommandContextWrapper> plotInfoMenuPaginator = ChatPaginator.<PlotChatListItems.PlotChatListItem, CommandContextWrapper>builder((item, ctx) -> {
                var found = ctx.resolvePlotUnchecked();
                var line = Text.of("| ").setBold(true);
                if (found == null || found.isDeleted()) line.setColor(ColorUtils.ERROR_TEXT);
                else line.setColor(ColorUtils.GRAYED_TEXT);
                if (item.isRequired()) {
                    line.appendRoot("*").setColor(ColorUtils.ERROR_TEXT)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This attribute is required.").setColor(ColorUtils.REGULAR_TEXT));
                }
                if (!item.getName().isBlank())
                    line.appendRoot(item.getName()).setColor(ColorUtils.REGULAR_TEXT).appendRoot(": ").setColor(ColorUtils.REGULAR_TEXT);
                line.appendRoot(item.getValue());
                return line;
            })
            .addComponent(new PageNavigationComponent<>(
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                    (ctx, res, pg) -> "/plot " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
            ), ComponentPosition.BOTTOM_CENTER)
            .addComponent(Text.of("Plot Info Menu").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
            .addComponent((ctx, p, r, c) -> {
                var userList = ctx.hasFlag("users");
                if (!userList) {
                    return Text.of("[Users]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view the users of this plot.").setColor(ColorUtils.REGULAR_TEXT))
                            .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot info -users -plot " + ctx.resolvePlotUnchecked().getId() + " -page 1"));
                }
                return Text.of("[Attributes]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view the attributes of this plot.").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot info -plot " + ctx.resolvePlotUnchecked().getId() + " -page 1"));
            }, ComponentPosition.BOTTOM_RIGHT)
            .addComponent((ctx, p, r, c) -> {
                var found = ctx.resolvePlotUnchecked();
                if (found == null) return Text.of("Unknown Plot").setColor(ColorUtils.ERROR_TEXT);
                else if (found.isDeleted()) return Text.of("Deleted - ID #" + found.getId()).setColor(ColorUtils.ERROR_TEXT);
                return Text.of("Plot ID #" + found.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT);
            }, ComponentPosition.TOP_RIGHT)
            .setGrayColor(ColorUtils.REGULAR_TEXT)
            .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
            .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
            .build();
    //endregion

    //region Plot List
    static final ChatPaginator<Plot, CommandContextWrapper> plotListPaginator = ChatPaginator.<Plot, CommandContextWrapper>builder((plot, ctx) -> {
                var statusAttr = plot.getAttribute("status");
                var statusString = statusAttr == null ? "Unknown" : statusAttr.getValue();
                if (plot.isDeleted()) statusString = "Deleted";
                var statusColor = switch (statusString.toLowerCase()) {
                    case "hold" -> Color.fromRGB(0x6b6b6b);
                    case "draft" -> Color.fromRGB(0xc49c45);
                    case "plotted" -> Color.fromRGB(0x00d9ff);
                    case "review" -> Color.fromRGB(0x1eb076);
                    case "complete" -> Color.fromRGB(0x0fb800);
                    default -> ColorUtils.ERROR_TEXT;
                };
                var descriptionAttr = plot.getAttribute("description");
                var descriptionString = descriptionAttr == null ? "No description provided." : descriptionAttr.getValue();
                var trimmed = trimToLength(descriptionString, 190);
                var text = Text.of("| ").setColor(statusColor).setBold(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Status: ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(statusString).setColor(statusColor))
                        .appendRoot("[T]").setColor(ColorUtils.ACTION_TEXT).setItalic(true).setUnderlined(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Distance: ").setColor(ColorUtils.REGULAR_TEXT)
                                .appendRoot(df.format(plot.getLocation().distance(ctx.asPlayer().getLocation())) + "m").setColor(ColorUtils.HIGHLIGHT_TEXT)
                                .appendRoot("\nClick to teleport to this plot.").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot goto " + plot.getId()))
                        .appendRoot(" ")
                        .appendRoot(String.format("#%5s", plot.getId())).setItalic(true).setUnderlined(true).setColor(ColorUtils.ACTION_TEXT)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view additional plot information.").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot info -plot " + plot.getId()))
                        .appendRoot(" ")
                        .appendRoot(trimToLength(trimmed, 190).trim()).setColor(ColorUtils.REGULAR_TEXT);

                if (!trimmed.equalsIgnoreCase(descriptionString)) {
                    text.appendRoot("...").setColor(ColorUtils.ACTION_TEXT).setItalic(true)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(descriptionString).setColor(ColorUtils.REGULAR_TEXT));
                }
                return text;
            })
            .addComponent(Text.of("Plot List").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
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
    //endregion

    //region Help command menu
    static final ChatPaginator<HelpChatListItem, CommandContextWrapper> helpMenuPaginator = ChatPaginator.<HelpChatListItem, CommandContextWrapper>builder((item, ctx) -> {
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
