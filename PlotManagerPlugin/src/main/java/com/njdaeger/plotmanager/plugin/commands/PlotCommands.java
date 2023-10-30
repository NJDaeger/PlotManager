package com.njdaeger.plotmanager.plugin.commands;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import com.njdaeger.plotmanager.plugin.IPlotManagerPlugin;
import com.njdaeger.plotmanager.plugin.commands.flags.PageFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.ParentFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.PlotFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.PlotGroupFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.RadiusFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.ReferenceFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.SessionFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.UsersFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.WorldFlag;
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandBuilderWrapper;
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.plotmanager.servicelibrary.ColorUtils;
import com.njdaeger.plotmanager.servicelibrary.PlotChatListItems;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotGroup;
import com.njdaeger.plotmanager.servicelibrary.models.User;
import com.njdaeger.plotmanager.servicelibrary.models.World;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IPlotService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.serviceprovider.IServiceProvider;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.map.MinecraftFont;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class PlotCommands {

    private final IServiceProvider provider;
    private final IPlotManagerPlugin plugin;
    private final IConfigService configService;
    private final ICacheService cacheService;
    private final ChatPaginator<Plot, CommandContextWrapper> parentSelectorPaginator;
    private final ChatPaginator<String, CommandContextWrapper> attributeValueSelectListPaginator;
    private final ChatPaginator<PlotChatListItems.PlotChatListItem, CommandContextWrapper> plotCreationMenuPaginator;
    private final ChatPaginator<PlotChatListItems.PlotChatListItem, CommandContextWrapper> plotInfoMenuPaginator;
    private final ChatPaginator<Plot, CommandContextWrapper> plotListPaginator;

    public PlotCommands(IPlotManagerPlugin plugin, IServiceProvider provider, IConfigService configService, ICacheService cacheService) {
        this.provider = provider;
        this.plugin = plugin;
        this.cacheService = cacheService;
        this.configService = configService;

        var df = new DecimalFormat("#.##");

        CommandBuilderWrapper.of("plot")
                .executor(this::plotCommand)
                .completer(this::plotTabCompletion)
                .min(1)
                .flag(new ParentFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new ReferenceFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new PlotGroupFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new SessionFlag(ctx -> ctx.argAt(0).equalsIgnoreCase("edit") || ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new PlotFlag(cacheService, ctx ->
                        ctx.argAt(0).equalsIgnoreCase("info") ||
                        ctx.argAt(0).equalsIgnoreCase("claim") ||
                        ctx.argAt(0).equalsIgnoreCase("add") ||
                        ctx.argAt(0).equalsIgnoreCase("remove") ||
                        ctx.argAt(0).equalsIgnoreCase("leave") ||
                        ctx.argAt(0).equalsIgnoreCase("delete") ||
                        ctx.argAt(0).equalsIgnoreCase("edit") && !ctx.hasFlag("session")))
                .flag(new RadiusFlag(ctx -> ctx.argAt(0).equalsIgnoreCase("list")))
                .flag(new UsersFlag(ctx -> ctx.argAt(0).equalsIgnoreCase("info")))
                .flag(new WorldFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("list")))
                .flag(new PageFlag(ctx ->
                        ctx.argAt(0).equalsIgnoreCase("info") ||
                        ctx.argAt(0).equalsIgnoreCase("list") ||
                        ctx.argAt(0).equalsIgnoreCase("create") ||
                        ctx.argAt(0).equalsIgnoreCase("edit") &&
                            ((ctx.hasArgAt(1) && ctx.argAt(1).equalsIgnoreCase("attribute") && ctx.hasArgAt(3) && ctx.argAt(3).equalsIgnoreCase("select")) ||
                            (ctx.hasArgAt(1) && ctx.argAt(1).equalsIgnoreCase("parent") && ctx.hasArgAt(2) && ctx.argAt(2).equalsIgnoreCase("select")))))
                .permissions("plotmanager.plot.create", "plotmanager.plot.edit", "plotmanager.plot.cancel", "plotmanager.plot.finish")
                .build()
                .register(plugin);

        this.parentSelectorPaginator = ChatPaginator.<Plot, CommandContextWrapper>builder((plt, ctx) -> {
                var editSession = ctx.hasFlag("session");
                var plot = ctx.resolvePlotUnchecked();
                if (plot == null && !editSession) return Text.of("No plot found.").setColor(ColorUtils.ERROR_TEXT);
                return Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true)
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
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit parent set " + plt.getId() + (editSession ? " -session" : "-plot " + plot.getId())));
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

        this.attributeValueSelectListPaginator = ChatPaginator.<String, CommandContextWrapper>builder((value, ctx) -> {
                    var editSession = ctx.hasFlag("session");
                    var plot = ctx.resolvePlotUnchecked();
                    if (plot == null && !editSession) return Text.of("No plot found.").setColor(ColorUtils.ERROR_TEXT);
                    return Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true)
                            .appendRoot(value).setColor(ColorUtils.HIGHLIGHT_TEXT)
                            .appendRoot(" ")
                            .appendRoot("[select]").setItalic(true).setUnderlined(true).setColor(ColorUtils.ACTION_TEXT)
                            .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to select this value for this attribute").setColor(ColorUtils.REGULAR_TEXT))
                            .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit attribute " + ctx.argAt(2) + " set " + value + (editSession ? " -session" : "-plot " + plot.getId())));
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

        this.plotCreationMenuPaginator = ChatPaginator.<PlotChatListItems.PlotChatListItem, CommandContextWrapper>builder((item, ctx) -> {
                        var line = Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true);
                        if (item.isRequired()) {
                            line.appendRoot("*").setColor(ColorUtils.ERROR_TEXT)
                                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This attribute is required.").setColor(ColorUtils.REGULAR_TEXT));
                        }
                        if (!item.getName().isBlank()) line.appendRoot(item.getName()).setColor(ColorUtils.REGULAR_TEXT).appendRoot(": ").setColor(ColorUtils.REGULAR_TEXT);
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
                        .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot finish")), ComponentPosition.BOTTOM_LEFT)
                .addComponent(Text.of("[Cancel]").setColor(ColorUtils.ERROR_TEXT).setUnderlined(true).setBold(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to cancel the plot creation.").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot cancel")), ComponentPosition.BOTTOM_RIGHT)
                .setGrayColor(ColorUtils.REGULAR_TEXT)
                .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
                .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
                .build();

        this.plotInfoMenuPaginator = ChatPaginator.<PlotChatListItems.PlotChatListItem, CommandContextWrapper>builder((item, ctx) -> {
                    var line = Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true);
                    if (item.isRequired()) {
                        line.appendRoot("*").setColor(ColorUtils.ERROR_TEXT)
                                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This attribute is required.").setColor(ColorUtils.REGULAR_TEXT));
                    }
                    if (!item.getName().isBlank()) line.appendRoot(item.getName()).setColor(ColorUtils.REGULAR_TEXT).appendRoot(": ").setColor(ColorUtils.REGULAR_TEXT);
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
                .addComponent((ctx, p, r, c) -> Text.of(ctx.resolvePlotUnchecked() == null ? "Unknown Plot" : "Plot ID #" + ctx.resolvePlotUnchecked().getId()).setColor(ctx.resolvePlotUnchecked() == null ? ColorUtils.ERROR_TEXT : ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_RIGHT)
                .setGrayColor(ColorUtils.REGULAR_TEXT)
                .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
                .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
                .build();

        this.plotListPaginator = ChatPaginator.<Plot, CommandContextWrapper>builder((plot, ctx) -> {
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


    }

    /*

    /plot create
        -reference <plotId>
        -group <group>
        -parent <plotId>

    /plot edit
        -session
        -plot <plotId>
    /plot edit attribute <attribute> remove
    /plot edit attribute <attribute> select
        -page <page>
    /plot edit attribute <attribute> set <value>
    /plot edit parent remove
    /plot edit parent select
        -page <page>
    /plot edit parent set <parentId>

    /plot cancel
    /plot finish


    /plot list [<attr>=<value> ...]
        -page <page>        the page to view
        -group <group>      list plots in a group
        -user <user>        list plots that a user is a part of
        -world <world>      list plots in a world
        -parent <plotId>    list plots that are children of a plot
        -radius <radius>    list plots within a radius of the current location

    | [T] #12345 Description
    click on T to teleport to the plot
    click on the plot Id to view the plot info

    ? [T] | Description
    color of the ? is based on the status of the plot

    /plot info -plot <plotId> -users

    /plot claim -plot <plotId>          adds the current user to the plot (if the plot has no claimer)
    /plot add <user> -plot <plotId>     adds a user to the plot (only if the executor is a user on the plot)
    /plot remove <user> -plot <plotId>  removes a user from the plot (only if the executor is a user on the plot)
    /plot leave -plot <plotId>          removes the current user from the plot (only if the executor is a user on the plot)

    /plot delete -plot <plotId>

     */

    private void plotCommand(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        if (context.argAt(0).equalsIgnoreCase("create")) {
            createPlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("edit")) {
            editPlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("cancel")) {
            cancelPlotCreation(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("finish")) {
            finishPlotCreation(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("delete")) {
            deletePlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("list")) {
            listPlots(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("info")) {
            plotInfo(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("claim")) {
            claimPlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("add")) {
            addPlotMember(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("remove")) {
            removePlotMember(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("leave")) {
            leavePlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("goto")) {
            gotoPlot(transaction, context);
        } else {
            context.error("Invalid argument " + context.argAt(0) + ".");
        }
    }

    private void plotTabCompletion(TabContext context) {
        context.completionAt(0, "create", "edit", "cancel", "finish", "delete", "list", "info", "claim", "add", "remove", "leave", "goto");
        if (context.hasArgAt(0) && context.argAt(0).equalsIgnoreCase("edit")) {
            context.completionAt(1, "attribute", "parent");
            if (context.hasArgAt(1) && context.argAt(1).equalsIgnoreCase("attribute")) {
                context.completionAt(2, cacheService.getAttributeCache().keySet().toArray(String[]::new));
                context.completionAt(3, "remove", "select", "set");
                if (context.hasArgAt(3) && context.argAt(3).equalsIgnoreCase("set")) {
                    var attr = cacheService.getAttributeCache().get(context.argAt(2));
                    if (attr == null) return;
                    var type = configService.getAttributeType(attr.getType());
                    if (type == null) return;
                    if (type.getValues().isEmpty()) {
                        if (type.getName().equalsIgnoreCase("integer")) {
                            if (context.getCurrent() == null || context.getCurrent().isEmpty()) {
                                context.completion(IntStream.rangeClosed(0, 9).mapToObj(String::valueOf).toArray(String[]::new));
                                return;
                            }
                            try {
                                int cur = Integer.parseInt(context.getCurrent());
                                context.completion(IntStream.rangeClosed(cur * 10, (cur * 10) + 10).mapToObj(String::valueOf).toArray(String[]::new));
                            } catch (NumberFormatException ignored) {
                            }
                        } else if (type.getName().equalsIgnoreCase("decimal")) {
                            var intList = IntStream.rangeClosed(0, 9).mapToObj(String::valueOf).toList();
                            if (context.getCurrent() == null || context.getCurrent().isEmpty()) {
                                context.completion(intList.toArray(String[]::new));
                                return;
                            }
                            try {
                                Double.parseDouble(context.getCurrent());
                                var suggestions = new ArrayList<>(intList);
                                if (!context.getCurrent().contains(".")) suggestions.add(".");
                                context.completion(suggestions.stream().map(s -> context.getCurrent() + s).toArray(String[]::new));
                                return;
                            } catch (NumberFormatException ignored) {
                            }
                        } else if (type.getName().equalsIgnoreCase("boolean")) {
                            context.completion("true", "false");
                            return;
                        }
                    }
                    context.completionAt(4, type.getValues().stream().map(String::valueOf).toArray(String[]::new));
                }
            } else if (context.hasArgAt(1) && context.argAt(1).equalsIgnoreCase("parent")) {
                context.completionAt(2, "remove", "select", "set");
                if (context.hasArgAt(2) && context.argAt(2).equalsIgnoreCase("set")) {
                    context.completionAt(3, cacheService.getPlotCache().keySet().stream().map(String::valueOf).toArray(String[]::new));
                }
            }
        }
        if (context.hasArgAt(0) && context.argAt(0).equalsIgnoreCase("goto")) {
            context.completionAt(1, cacheService.getPlotCache().keySet().stream().map(String::valueOf).toArray(String[]::new));
        }
        if (context.hasArgAt(0) && (context.argAt(0).equalsIgnoreCase("add"))) {
            context.completionAt(1, cacheService.getUserCache().values().stream().map(User::getLastKnownName).toArray(String[]::new));
        }
        if (context.hasArgAt(0) && context.argAt(0).equalsIgnoreCase("remove")) {
            context.completionAt(1, cacheService.getUserCache().values().stream().map(User::getLastKnownName).toArray(String[]::new));
        }
        if (context.hasArgAt(0) && context.argAt(0).equalsIgnoreCase("list")) {
            context.completion(cacheService.getAttributeCache().keySet().stream().map(s -> s.concat("=")).toArray(String[]::new));
            if (context.getCurrent() != null && context.getCurrent().contains("=")) {
                var attribute = context.getCurrent().substring(0, context.getCurrent().indexOf("="));
                var split = context.getCurrent().split("=");
                var curVal = split.length == 2 ? split[1] : "";
                var attr = cacheService.getAttributeCache().get(attribute);
                if (attr == null) return;
                var type = configService.getAttributeType(attr.getType());
                if (type == null) return;
                if (type.getName().equalsIgnoreCase("string")) {
                    context.completion(context.getCurrent() + "\"text here\"");
                } else if (type.getName().equalsIgnoreCase("boolean")) {
                    context.completion(context.getCurrent() + "true", context.getCurrent() + "false");
                } else if (type.getName().equalsIgnoreCase("integer")) {
                    if (curVal == null || curVal.isEmpty()) {
                        context.completion(IntStream.rangeClosed(0, 9).mapToObj(String::valueOf).map(s -> context.getCurrent() + s).toArray(String[]::new));
                        return;
                    }
                    try {
                        Integer.parseInt(curVal);
                        context.completion(IntStream.rangeClosed(0, 9).mapToObj(String::valueOf).map(s -> context.getCurrent() + s).toArray(String[]::new));
                    } catch (NumberFormatException ignored) {
                    }
                } else if (type.getName().equalsIgnoreCase("decimal")) {
                    if (curVal == null || curVal.isEmpty()) {
                        context.completion(IntStream.rangeClosed(0, 9).mapToObj(String::valueOf).map(s -> context.getCurrent() + s).toArray(String[]::new));
                        return;
                    }
                    try {
                        Double.parseDouble(curVal);
                        var suggestions = new ArrayList<>(IntStream.rangeClosed(0, 9).mapToObj(String::valueOf).toList());
                        if (!curVal.contains(".")) suggestions.add(".");
                        context.completion(suggestions.stream().map(s -> context.getCurrent() + s).toArray(String[]::new));
                    } catch (NumberFormatException ignored) {
                    }
                } else if (!type.getValues().isEmpty()) {
                    context.completion(type.getValues().stream().map(s -> context.getCurrent() + s).toArray(String[]::new));
                }
            }
        }
    }

    /*

    plot commands



    /plot create
        * Starts a plot creation builder at the current location with the current user.
        Flags:
        - reference <plotId> - The plot to copy attributes from.
        - group <group> - The group this plot should be a part of. Will automatically pull all attributes from the group. Any edits done to the attributes will be done at the plot level and not the group level.
        - parent <plotId> - The parent plot.
        Examples:
        - /plot create -reference 102
            creates a plot with the same attributes as plot 102
        - /plot create -group los_llanos_bungalow_group
            creates a plot with attributes automatically pulled from the group los_llanos_bungalow_group
            if edits are done to any plot in a given group, all plots in that group will be updated.
        Chat:
        [PlotManager] Plot creation started. Use /plot cancel to cancel the creation process.
        Location: x, y, z, world
        Parent: "<parentId> [remove]" or "none [select]"
            remove: /plot edit parent remove -session
            select: /plot edit parent select -session (select parent opens a chat list gui to select the parent)
          then we go through the list of required plot attributes, setting necessary defaults
        Status: "<status> [change]"
            change: /plot edit attribute <attribute> -select -session
        Rank: "<rank> [change]"
            change: /plot edit attribute <attribute> -select -session
        Points: "<points> [change]"
            change: /plot edit attribute <attribute> -select -session
        Building-Type: "<buildingType> [change]"
            change: /plot edit attribute <attribute> -select -session
        Floors: "<floors> [change]"
            change: /plot edit attribute <attribute> -select -session
        Description: "[set]"
            set: /plot edit -session attribute <attribute> set <value>

     */

    /*
    /plot create
    -reference
    -parent

    eventually:
    -group
     */
    private void createPlot(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var attributeService = transaction.getService(IAttributeService.class);
        var useSession = context.hasFlag("session");
        var page = context.getFlag("page", 1);

        if (useSession) {
            var builder = await(plotService.getPlotBuilder(context.getUUID())).getOrThrow(new PDKCommandException("You do not have a plot creation session open."));
            var requiredAttributes = configService.getRequiredPlotAttributes();
            var menu = plotCreationMenuPaginator.generatePage(context, PlotChatListItems.getListItemsForBuilder(requiredAttributes, attributeService, configService, builder), page);
            if (menu == null) context.error("Invalid page number.");
            else menu.sendTo(context.getSender());
            return;
        }

        var res = await(plotService.createPlotBuilder(context.getUUID(), context.getLocation()));
        if (res.successful()) {
            var builder = res.getOrThrow();
            var ref = context.<Plot>getFlag("reference");
            var parent = context.<Plot>getFlag("parent");
            var requiredAttributes = configService.getRequiredPlotAttributes();

            if (parent != null) builder.setParent(parent);
            if (ref != null) builder.copyAttributes(ref);
            //after adding attributes from the reference, ensure that all defaults have also been added
            requiredAttributes.forEach(attr -> {
                if (builder.hasAttribute(attr)) return;
                var def = configService.getRequiredPlotAttributeDefaults().get(attr);
                if (def == null) return;
                builder.addAttribute(attr, def);
            });

            var menu = plotCreationMenuPaginator.generatePage(context, PlotChatListItems.getListItemsForBuilder(requiredAttributes, attributeService, configService, builder), page);
            if (menu == null) context.error("No data to show.");
            else menu.sendTo(context.getSender());
        } else context.error(res.message());
    }

    /*

    alternatively, we could do something like this:
    /plot edit

    -session                   if this should be editing a plot builder, rather than an existing plot
    -plot <plotId>             the plot to edit
    -page <page>               the page to view (selects only)
    flags are mutually exclusive, so if -plot is used and -session is used, an error is thrown.

    /plot edit attribute <attribute>                fails
    /plot edit attribute <attribute> remove         removes the attribute from the plot
    /plot edit attribute <attribute> select         opens a select list in chat to select a new value for the attribute. Or it fails if the attribute type does not have a list of values.
        selects also have the page flag
    /plot edit attribute <attribute> set <value>    sets the value of the attribute, or adds the attribute if it does not exist
    /plot edit parent remove                        removes the parent from the plot
    /plot edit parent select                        opens a select list in chat to select a new parent for the plot
        selects also have the page flag
    /plot edit parent set <parentId>                sets the parent of the plot
     */
    private void editPlot(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var editSession = context.hasFlag("session");
        var editPlot = context.<Plot>getFlag("plot");

        if (editSession && editPlot != null) context.error("Flags -session and -plot are mutually exclusive. Please only use one of them.");
        else if (!editSession && editPlot == null) editPlot = context.resolvePlot();
        if (!editSession && editPlot == null) context.error("No plot to edit. Please use the -plot flag to specify a plot to edit, or use the -session flag to edit the plot builder.");

        var editing = context.argAt(1);
        if (editing.equalsIgnoreCase("attribute")) {
            editAttribute(transaction, context, editPlot);
        } else if (editing.equalsIgnoreCase("parent")) {
            editParent(transaction, context, editPlot);
        } else {
            context.error("Invalid argument " + editing + ". Expected 'attribute' or 'parent'.");
        }
    }

    private void editAttribute(IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
        var attribute = context.argAt(2);
        var action = context.argAt(3);
        if (action == null) context.error("No action provided. Expected 'remove', 'select', or 'set'.");

        if (action.equalsIgnoreCase("remove")) {
            removeAttribute(transaction, context, attribute, plotToEdit);
        } else if (action.equalsIgnoreCase("select")) {
            selectAttribute(transaction, context, attribute, plotToEdit);
        } else if (action.equalsIgnoreCase("set")) {
            setAttribute(transaction, context, attribute, plotToEdit);
        } else {
            context.error("Invalid action " + action + ". Expected 'remove', 'select', or 'set'.");
        }
    }

    private void removeAttribute(IServiceTransaction transaction, CommandContextWrapper context, String attribute, Plot plotToEdit) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);

        if (plotToEdit == null) {
            var builderRes = await(plotService.getPlotBuilder(context.getUUID()));
            if (builderRes.successful()) {
                var builder = builderRes.getOrThrow();
                builder.addAttribute(attribute, null);
                var menu = plotCreationMenuPaginator.generatePage(context, PlotChatListItems.getListItemsForBuilder(configService.getRequiredPlotAttributes(), transaction.getService(IAttributeService.class), configService, builder), 1);
                if (menu == null) context.error("No data to show.");
                else menu.sendTo(context.getSender());
            }
            else context.error(builderRes.message());
        } else {
            var res = await(plotService.removePlotAttribute(context.getUUID(), plotToEdit.getId(), attribute));
            if (!res.successful()) context.error(res.message());
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Attribute ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(attribute).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" removed.").setColor(ColorUtils.REGULAR_TEXT));
        }
    }

    private void selectAttribute(IServiceTransaction transaction, CommandContextWrapper context, String attribute, Plot plotToEdit) throws PDKCommandException {
        var page = context.getFlag("page", 1);
        var plotService = transaction.getService(IPlotService.class);
        var attributeService = transaction.getService(IAttributeService.class);

        if (plotToEdit == null) {
            var builder = await(plotService.getPlotBuilder(context.getUUID()));
            if (!builder.successful()) context.error(builder.message());
        }

        var attr = await(attributeService.getAttribute(attribute)).getOrThrow(new PDKCommandException("Invalid attribute " + attribute + "."));
        var type = configService.getAttributeType(attr.getType());
        if (type == null) context.error("Invalid attribute type " + attr.getType() + ".");

        if (type.getValues().isEmpty()) context.error("Attribute " + attribute + " does not have a defined list of values. It is of type " + attr.getType() + ".");
        var res = attributeValueSelectListPaginator.generatePage(context, type.getValues(), page);

        if (res == null) context.error("No pages to display.");
        context.send(res.getMessage());
    }

    private void setAttribute(IServiceTransaction transaction, CommandContextWrapper context, String attribute, Plot plotToEdit) throws PDKCommandException {
        var attributeService = transaction.getService(IAttributeService.class);

        var value = context.joinArgs(4);
        var attr = await(attributeService.getAttribute(attribute)).getOrThrow(new PDKCommandException("Invalid attribute " + attribute + "."));
        var type = configService.getAttributeType(attr.getType());
        if (type == null) context.error("Invalid attribute type " + attr.getType() + ".");
        if (!type.isValidValue(value)) context.error("Invalid value '" + value + "' for attribute " + attribute + ". Expected a " + type.getName() + " datatype.");

        if (plotToEdit == null) {
            var builderRes = await(transaction.getService(IPlotService.class).getPlotBuilder(context.getUUID()));
            if (!builderRes.successful()) context.error(builderRes.message());
            var builder = builderRes.getOrThrow();
            builder.addAttribute(attribute, value);
            var menu = plotCreationMenuPaginator.generatePage(context, PlotChatListItems.getListItemsForBuilder(configService.getRequiredPlotAttributes(), attributeService, configService, builder), 1);
            if (menu == null) context.error("No data to show.");
            else menu.sendTo(context.getSender());
        } else {
            var res = await(transaction.getService(IPlotService.class).setPlotAttribute(context.getUUID(), plotToEdit.getId(), attribute, value));
            if (!res.successful()) context.error(res.message());
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Attribute ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(attribute).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" set to ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(value).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));

        }
    }

    private void editParent(IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
        var action = context.argAt(2);
        if (action == null) context.error("Invalid action " + action + ". Expected 'remove', 'select', or 'set'.");

        if (action.equalsIgnoreCase("remove")) {
            removeParent(transaction, context, plotToEdit);
        } else if (action.equalsIgnoreCase("select")) {
            selectParent(transaction, context, plotToEdit);
        } else if (action.equalsIgnoreCase("set")) {
            setParent(transaction, context, plotToEdit);
        } else {
            context.error("Invalid action " + action + ". Expected 'remove', 'select', or 'set'.");
        }
    }

    private void removeParent(IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);

        if (plotToEdit == null) {
            var builderRes = await(plotService.getPlotBuilder(context.getUUID()));
            if (!builderRes.successful()) context.error(builderRes.message());
            var builder = builderRes.getOrThrow();
            builder.setParent(null);
            var menu = plotCreationMenuPaginator.generatePage(context, PlotChatListItems.getListItemsForBuilder(configService.getRequiredPlotAttributes(), transaction.getService(IAttributeService.class), configService, builder), 1);
            if (menu == null) context.error("No data to show.");
            else menu.sendTo(context.getSender());
        } else {
            var res = await(plotService.removePlotParent(context.getUUID(), plotToEdit.getId()));
            if (!res.successful()) context.error(res.message());
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Parent removed.").setColor(ColorUtils.REGULAR_TEXT));
        }
    }

    private void selectParent(IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
        var page = context.getFlag("page", 1);
        var plotService = transaction.getService(IPlotService.class);

        if (plotToEdit == null) {
            var builder = await(plotService.getPlotBuilder(context.getUUID()));
            if (!builder.successful()) context.error(builder.message());
        }

        var res = parentSelectorPaginator.generatePage(context, new ArrayList<>(cacheService.getPlotCache().values()), page);

        if (res == null) context.error("Invalid page number.");
        context.send(res.getMessage());
    }

    private void setParent(IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);

        var parentId = context.integerAt(3, -1);
        if (parentId == -1) context.error("No valid parent provided. Expected a plot id.");
        var parent = await(plotService.getPlot(parentId)).getOrThrow(new PDKCommandException("Invalid parent id " + parentId + "."));

        if (plotToEdit == null) {
            var builderRes = await(plotService.getPlotBuilder(context.getUUID()));
            if (!builderRes.successful()) context.error(builderRes.message());
            var builder = builderRes.getOrThrow();
            builder.setParent(parent);
            var menu = plotCreationMenuPaginator.generatePage(context, PlotChatListItems.getListItemsForBuilder(configService.getRequiredPlotAttributes(), transaction.getService(IAttributeService.class), configService, builder), 1);
            if (menu == null) context.error("No data to show.");
            else menu.sendTo(context.getSender());
        } else {
            var res = await(plotService.setPlotParent(context.getUUID(), plotToEdit.getId(), parentId));
            if (!res.successful()) context.error(res.message());
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Parent set to ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + parent.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        }
    }

    // /plot finish
    private void finishPlotCreation(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);

        var res = await(plotService.finishPlotBuilder(context.getUUID()));
        if (res.successful()) {
            var plot = res.getOrThrow();
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot("Plot id ").setColor(ColorUtils.REGULAR_TEXT)
                    .appendRoot(String.valueOf(plot.getId())).setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot(" created.").setColor(ColorUtils.REGULAR_TEXT));
        } else {
            if (cacheService.getPlotBuilderCache().containsKey(context.getUUID()) && context.isPlayer()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    context.asPlayer().performCommand("plot create -session");
                });
            }
            context.error(res.message());
        }
    }

    private void cancelPlotCreation(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);

        var res = await(plotService.cancelPlotBuilder(context.getUUID()));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Plot creation cancelled.").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }

    private void deletePlot(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        var res = await(plotService.deletePlot(context.getUUID(), plot.getId()));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" deleted.").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }

    private void claimPlot(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.hasAnyActiveUsers()) context.error("This plot is already claimed. Ask to be added by a member.");
        var res = await(plotService.addPlotUser(context.getUUID(), plot.getId(), context.getUUID()));

        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("You have claimed plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }

    private void addPlotMember(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.getUser(context.getUUID()) == null) context.error("You cannot add people to this plot. You are not a member of plot #" + plot.getId() + ".");

        var username = context.argAt(1);
        var user = context.resolveUser(username);

        var res = await(plotService.addPlotUser(context.getUUID(), plot.getId(), user.getUserId()));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("User ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(user.getLastKnownName()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" added to plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());

    }

    private void removePlotMember(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.getUser(context.getUUID()) == null) context.error("You cannot remove people from this plot. You are not a member of plot #" + plot.getId() + ".");

        var username = context.argAt(1);
        var user = context.resolveUser(username);

        var res = await(plotService.removePlotUser(context.getUUID(), plot.getId(), user.getUserId()));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("User ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(user.getLastKnownName()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(" removed from plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }

    private void leavePlot(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);
        var plot = context.resolvePlot();

        if (plot.getUser(context.getUUID()) == null) context.error("You cannot leave this plot. You are not a member of plot #" + plot.getId() + ".");

        var res = await(plotService.removePlotUser(context.getUUID(), plot.getId(), context.getUUID()));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("You have left plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + plot.getId()).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }

    /*

        /plot list [<attr>=<value> ...]
        -page <page>        the page to view
        -group <group>      list plots in a group
        -user <user>        list plots that a user is a part of
        -world <world>      list plots in a world
        -parent <plotId>    list plots that are children of a plot
        -radius <radius>    list plots within a radius of the current location

     */
    private void listPlots(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var page = context.getFlag("page", 1);
        var group = context.<PlotGroup>getFlag("group");
        var user = context.<User>getFlag("user");
        var world = context.<World>getFlag("world");
        var parent = context.<Plot>getFlag("parent");
        var radius = context.getFlag("radius", -1);

        //attributes in the command can be formatted like attribute=value or attribute="value of attribute"
        //parse these and put their values into a map key: attributeName, value: Pair<value, isExactMatch>
        //first set of values to look for are ([a-zA-Z]\w+=\w+) then ([a-zA-Z]\w+="[\w\s]+")
        var attribs = new HashMap<String, Pair<String, Boolean>>();

        var attribRegex = Pattern.compile("([a-zA-Z]\\w+)=(\\w+)|([a-zA-Z]\\w+)=\"([\\w\\s]+)\"");
        var matcher = attribRegex.matcher(context.getRawCommandString());
        while (matcher.find()) {
            var attrib = matcher.group(1);
            var value = matcher.group(2);
            if (attrib == null) {
                attrib = matcher.group(3);
                value = matcher.group(4);
            }
            attribs.put(attrib, Pair.of(value, !value.contains(" ")));
        }

        var lookInWorld = world != null ? world.getWorldUuid() : context.isPlayer() ? context.asPlayer().getWorld().getUID() : null;

        if (radius != -1) {
            if (!context.isPlayer()) context.error("You must be a player to use the -radius flag.");
            if (lookInWorld == null) context.error("You must specify a valid world to use the -radius flag.");
            if (!lookInWorld.equals(context.asPlayer().getWorld().getUID())) context.error("You must be in the same world as the plots you are looking for to use the -radius flag.");
        }

        var plotService = transaction.getService(IPlotService.class);
        var plotsRes = await(plotService.getPlots((p) -> {
            if (lookInWorld != null && !p.getLocation().getWorld().getUID().equals(lookInWorld)) return false;
            if (radius != -1 && p.getLocation().distanceSquared(context.getLocation()) > radius * radius) return false;
            if (group != null && !p.getPlotGroup().getName().equalsIgnoreCase(group.getName())) return false;
            if (parent != null && p.getParent() != null && p.getParent().getId() != parent.getId()) return false;
            if (user != null && !p.getUsers().contains(user)) return false;
            return true;
        }));

        if (!plotsRes.successful()) context.error(plotsRes.message());

        List<Plot> plots = new ArrayList<>(plotsRes.getOrThrow());

        //we want to sort by distance if its in the same world, otherwise sort by plot id
        if (lookInWorld != null && lookInWorld.equals(context.getLocation().getWorld().getUID())) {
            plots.sort(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(context.getLocation())));
        } else {
            plots.sort(Comparator.comparingInt(Plot::getId));
        }

        //now we want to filter by attributes
        plots = plots.stream().filter(p -> {
            for (var entry : attribs.entrySet()) {
                var attrib = entry.getKey();
                var value = entry.getValue().getFirst();
                var exact = entry.getValue().getSecond();
                var attr = p.getAttribute(attrib);
                if (attr == null) return false;
                if (exact && !attr.getValue().equalsIgnoreCase(value)) return false;
                if (!exact && !attr.getValue().toLowerCase().contains(value.toLowerCase())) return false;
            }
            return true;
        }).toList();

        var listMenu = plotListPaginator.generatePage(context, plots, page);
        if (listMenu != null) listMenu.sendTo(context.getSender());
        else context.error("No data to show.");
    }

    // /plot goto <plotId>
    private void gotoPlot(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var providedPlotId = context.integerAt(1, -1);
        var plot = await(transaction.getService(IPlotService.class).getPlot(providedPlotId));
        if (!plot.successful()) {
            context.error(plot.message());
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                context.asEntity().teleport(plot.getOrThrow().getLocation());
                context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Teleported to plot ").setColor(ColorUtils.REGULAR_TEXT).appendRoot("#" + providedPlotId).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
            });
        }
    }

    private void plotInfo(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plot = context.resolvePlot();
        var page = context.getFlag("page", 1);
        List<PlotChatListItems.PlotChatListItem> items;
        if (context.hasFlag("users")) items = PlotChatListItems.getPlotUserList(plot);
        else items = PlotChatListItems.getListItemsForPlot(configService.getRequiredPlotAttributes(), transaction.getService(IAttributeService.class), configService, plot, true);
        var res = plotInfoMenuPaginator.generatePage(context, items, page);
        if (res == null) context.error("No data to show.");
        else res.sendTo(context.getSender());
    }

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
