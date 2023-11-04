package com.njdaeger.plotmanager.plugin.commands;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.plotmanager.plugin.IPlotManagerPlugin;
import com.njdaeger.plotmanager.plugin.PermissionConstants;
import com.njdaeger.plotmanager.plugin.commands.flags.*;
import com.njdaeger.plotmanager.plugin.commands.help.HelpChatListItem;
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
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.njdaeger.plotmanager.dataaccess.Util.await;
import static com.njdaeger.plotmanager.plugin.commands.Paginators.*;

public class PlotCommands {

    private final IPlotManagerPlugin plugin;
    private final IConfigService configService;
    private final ICacheService cacheService;
    private final List<HelpChatListItem> commandList;

    public PlotCommands(IPlotManagerPlugin plugin, IConfigService configService, ICacheService cacheService) {
        this.plugin = plugin;
        this.cacheService = cacheService;
        this.configService = configService;

        this.commandList = new ArrayList<>() {{
            add(new HelpChatListItem("create", "Create a new plot at your current location.",
                    new HelpChatListItem("-reference <plotId>", "Copy attributes from the specified plot to the new plot."),
                    //new HelpChatListItem("-group <group>", "Mark the plot as a part of the specified plot group."),
                    new HelpChatListItem("-parent <plotId>", "Set the parent of the plot to the specified plot."),
                    new HelpChatListItem("-session", "If specified, it will show the information about the current plot being created. (Only usable if there is a plot actively being created)"),
                    new HelpChatListItem("-page <page>", "If specified, it will show the specified page of the plot creation session. (Only usable if there is a plot actively being created)")
            ));
            add(new HelpChatListItem("edit", "Edit an existing plot.",
                    new HelpChatListItem("-session", "If specified, it will show the information about the current plot being created. (Only usable if there is a plot actively being created)"),
                    new HelpChatListItem("-plot <plotId>", "The plot to edit. (Only usable if -session is not specified)"),
                    new HelpChatListItem("attribute", "Edit the specified attribute.",
                            new HelpChatListItem("<attribute>", "The attribute to edit.",
                                    new HelpChatListItem("remove", "Remove the attribute from the plot."),
                                    new HelpChatListItem("select", "Select a new value for the attribute.",
                                            new HelpChatListItem("-page <page>", "Show the specified page of the attribute selection menu.")
                                    ),
                                    new HelpChatListItem("set", "Set the value of the attribute.",
                                            new HelpChatListItem("<value>", "The value to set the attribute to.")
                                    )
                            )
                    ),
                    new HelpChatListItem("parent", "Edit the parent of the plot.",
                            new HelpChatListItem("remove", "Remove the parent from the plot."),
                            new HelpChatListItem("select", "Select a new parent for the plot.",
                                    new HelpChatListItem("-page <page>", "Show the specified page of the parent plot selection menu.")
                            ),
                            new HelpChatListItem("set", "Set the parent of the plot.",
                                    new HelpChatListItem("<parentId>", "The plot to set as the parent.")
                            )
                    )
            ));
            add(new HelpChatListItem("cancel", "Cancel the current plot creation session."));
            add(new HelpChatListItem("finish", "Finish the current plot creation session and create the plot."));
            add(new HelpChatListItem("delete", "Delete the nearest (10m radius) plot.",
                    new HelpChatListItem("-plot <plotId>", "The exact plot to delete.")
            ));
            add(new HelpChatListItem("list", "List plots.",
                    new HelpChatListItem("-page <page>", "Show the specified page of the plot list."),
                    //new HelpChatListItem("-group <group>", "List plots in the specified group."),
                    new HelpChatListItem("-user <user>", "List plots that the specified user is a part of."),
                    new HelpChatListItem("-world <world>", "List plots in the specified world."),
                    new HelpChatListItem("-parent <plotId>", "List plots that are children of the specified plot."),
                    new HelpChatListItem("-radius <radius>", "List plots within the specified radius of the current location.")
            ));
            add(new HelpChatListItem("info", "View information about the nearest (10m radius) plot.",
                    new HelpChatListItem("-plot <plotId>", "The plot to view information about."),
                    new HelpChatListItem("-users", "If specified, it will show a list of users on the plot.")
            ));
            add(new HelpChatListItem("claim", "Claim the nearest (10m radius) plot.",
                    new HelpChatListItem("[forUser]", "The user to claim the plot for."),
                    new HelpChatListItem("-plot <plotId>", "The plot to claim.")
            ));
            add(new HelpChatListItem("add", "Add a user to the nearest (10m radius) plot.",
                    new HelpChatListItem("<user>", "The user to add to the plot."),
                    new HelpChatListItem("-plot <plotId>", "The plot to add the user to."),
                    new HelpChatListItem("-force", "If specified, it will add the user to the plot even if command sender is not a member of the plot.")
            ));
            add(new HelpChatListItem("remove", "Remove a user from the nearest (10m radius) plot.",
                    new HelpChatListItem("<user>", "The user to remove from the plot."),
                    new HelpChatListItem("-plot <plotId>", "The plot to remove the user from."),
                    new HelpChatListItem("-force", "If specified, it will remove the user from the plot even if command sender is not a member of the plot.")
            ));
            add(new HelpChatListItem("leave", "Leave the nearest (10m radius) plot.",
                    new HelpChatListItem("[forUser]", "The user to remove from the plot."),
                    new HelpChatListItem("-plot <plotId>", "The plot to leave.")
            ));
            add(new HelpChatListItem("goto", "Teleport to a specified plot.",
                    new HelpChatListItem("<plotId>", "The plot to teleport to.")
            ));
            add(new HelpChatListItem("open", "Moves the nearest (10m radius) plot from status Draft to Plotted",
                    new HelpChatListItem("-plot <plotId>", "The exact plot to open.")
            ));
            add(new HelpChatListItem("start", "Moves the nearest (10m radius) plot from status Plotted to Ongoing",
                    new HelpChatListItem("-plot <plotId>", "The exact plot to start.")
            ));
            add(new HelpChatListItem("finish", "Moves the nearest (10m radius) plot from status Ongoing to Review",
                    new HelpChatListItem("-plot <plotId>", "The exact plot to finish.")
            ));
            add(new HelpChatListItem("review", "Review the nearest (10m radius) plot",
                    new HelpChatListItem("pass", "Moves the nearest (10m radius) plot from status Review to Complete"),
                    new HelpChatListItem("fail", "Moves the nearest (10m radius) plot from status Review to Ongoing"),
                    new HelpChatListItem("-plot <plotId>", "The exact plot to review.")
            ));
            add(new HelpChatListItem("help", "Show this help menu."));
        }};

        CommandBuilderWrapper.of("plot")
                .executor(this::plotCommand)
                .completer(this::plotTabCompletion)
                .description("The base plot command.")
                .usage("'/plot help' for usage information.")
                .min(1)
                .flag(new ParentFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create") || ctx.argAt(0).equalsIgnoreCase("list")))
                .flag(new ReferenceFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new PlotGroupFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create") || ctx.argAt(0).equalsIgnoreCase("list")))
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
                .flag(new UserFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("list")))
                .flag(new UsersFlag(ctx -> ctx.argAt(0).equalsIgnoreCase("info")))
                .flag(new WorldFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("list")))
                .flag(new ForceFlag(ctx -> ctx.argAt(0).equalsIgnoreCase("remove") || ctx.argAt(0).equalsIgnoreCase("add")))
                .flag(new PageFlag(ctx ->
                        ctx.argAt(0).equalsIgnoreCase("info") ||
                        ctx.argAt(0).equalsIgnoreCase("list") ||
                        ctx.argAt(0).equalsIgnoreCase("create") ||
                        ctx.argAt(0).equalsIgnoreCase("edit") &&
                            ((ctx.hasArgAt(1) && ctx.argAt(1).equalsIgnoreCase("attribute") && ctx.hasArgAt(3) && ctx.argAt(3).equalsIgnoreCase("select")) ||
                            (ctx.hasArgAt(1) && ctx.argAt(1).equalsIgnoreCase("parent") && ctx.hasArgAt(2) && ctx.argAt(2).equalsIgnoreCase("select")))))
                .permissions(
                        PermissionConstants.PLOT_INFO_COMMAND,
                        PermissionConstants.PLOT_GOTO_COMMAND,
                        PermissionConstants.PLOT_LIST_COMMAND,
                        PermissionConstants.PLOT_CREATE_COMMAND,
                        PermissionConstants.PLOT_CLAIM_COMMAND,
                        PermissionConstants.PLOT_CLAIM_COMMAND_OTHER,
                        PermissionConstants.PLOT_LEAVE_COMMAND,
                        PermissionConstants.PLOT_LEAVE_COMMAND_OTHER,
                        PermissionConstants.PLOT_ADD_MEMBER_COMMAND,
                        PermissionConstants.PLOT_ADD_MEMBER_COMMAND_FORCE,
                        PermissionConstants.PLOT_REMOVE_MEMBER_COMMAND,
                        PermissionConstants.PLOT_REMOVE_MEMBER_COMMAND_FORCE,
                        PermissionConstants.PLOT_MODIFY_ATTRIBUTE_COMMAND,
                        PermissionConstants.PLOT_DELETE_ATTRIBUTE_COMMAND,
                        PermissionConstants.PLOT_MODIFY_PARENT_COMMAND,
                        PermissionConstants.PLOT_DELETE_PARENT_COMMAND,
                        PermissionConstants.PLOT_MODIFY_GROUP_COMMAND,
                        PermissionConstants.PLOT_DELETE_GROUP_COMMAND,
                        PermissionConstants.PLOT_DELETE_COMMAND
                )
                .build()
                .register(plugin);
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
            if (!context.hasPermission(PermissionConstants.PLOT_CREATE_COMMAND)) context.noPermission();
            createPlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("edit")) {
            if (!context.hasAnyPermission(
                    PermissionConstants.PLOT_MODIFY_ATTRIBUTE_COMMAND,
                    PermissionConstants.PLOT_DELETE_ATTRIBUTE_COMMAND,
                    PermissionConstants.PLOT_MODIFY_PARENT_COMMAND,
                    PermissionConstants.PLOT_DELETE_PARENT_COMMAND,
                    PermissionConstants.PLOT_MODIFY_GROUP_COMMAND,
                    PermissionConstants.PLOT_DELETE_GROUP_COMMAND
                    )) context.noPermission();
            PlotEditingCommands.editPlot(transaction.getService(IAttributeService.class), configService, cacheService, transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("cancel")) {
            if (!context.hasPermission(PermissionConstants.PLOT_CREATE_COMMAND)) context.noPermission();
            cancelPlotCreation(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("finish")) {
            if (!context.hasPermission(PermissionConstants.PLOT_CREATE_COMMAND)) context.noPermission();
            finishPlotCreation(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("delete")) {
            if (!context.hasPermission(PermissionConstants.PLOT_DELETE_COMMAND)) context.noPermission();
            deletePlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("list")) {
            if (!context.hasPermission(PermissionConstants.PLOT_LIST_COMMAND)) context.noPermission();
            listPlots(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("info")) {
            if (!context.hasPermission(PermissionConstants.PLOT_INFO_COMMAND)) context.noPermission();
            plotInfo(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("claim")) {
            if (!context.hasAnyPermission(PermissionConstants.PLOT_CLAIM_COMMAND, PermissionConstants.PLOT_CLAIM_COMMAND_OTHER)) context.noPermission();
            PlotUserCommands.claimPlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("add")) {
            if (!context.hasAnyPermission(PermissionConstants.PLOT_ADD_MEMBER_COMMAND, PermissionConstants.PLOT_ADD_MEMBER_COMMAND_FORCE)) context.noPermission();
            PlotUserCommands.addPlotMember(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("remove")) {
            if (!context.hasAnyPermission(PermissionConstants.PLOT_REMOVE_MEMBER_COMMAND, PermissionConstants.PLOT_REMOVE_MEMBER_COMMAND_FORCE)) context.noPermission();
            PlotUserCommands.removePlotMember(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("leave")) {
            if (!context.hasAnyPermission(PermissionConstants.PLOT_LEAVE_COMMAND, PermissionConstants.PLOT_LEAVE_COMMAND_OTHER)) context.noPermission();
            PlotUserCommands.leavePlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("goto")) {
            if (!context.hasPermission(PermissionConstants.PLOT_GOTO_COMMAND)) context.noPermission();
            gotoPlot(transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("open")) {
            PlotUserCommands.openPlot(configService, transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("start")) {
            PlotUserCommands.startPlot(configService, transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("complete")) {
            PlotUserCommands.completePlot(configService, transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("review")) {
            PlotUserCommands.reviewPlot(configService, transaction, context);
        } else if (context.argAt(0).equalsIgnoreCase("help")) {
            helpCommand(transaction, context);
        } else {
            context.error("Invalid argument " + context.argAt(0) + ".");
        }
    }

    private void plotTabCompletion(TabContext context) {
        context.completionAt(0, "help", "create", "edit", "cancel", "finish", "delete", "list", "info", "claim", "add", "remove", "leave", "goto", "open", "review", "start", "finish");
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
                    context.completionAt(4, type.getValues().stream().map(String::valueOf).map(s -> s.contains(" ") ? '"' + s + '"' : s).toArray(String[]::new));
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
        if (context.hasArgAt(0) && context.argAt(0).equalsIgnoreCase("review")) {
            context.completionAt(1, "pass", "fail");
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

    // /plot help

    private void helpCommand(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var page = context.getFlag("page", 1);
        var path = context.joinArgs(1).trim().split(" ");
        var currentList = commandList;
        if (!path[0].isBlank()) {
            for (String cmd : path) {
                if (currentList.isEmpty()) break;
                currentList = currentList.stream().filter(c -> c.getCommand().equalsIgnoreCase(cmd)).findFirst().map(HelpChatListItem::getSubCommands).orElse(List.of());
            }
        }

        var res = helpMenuPaginator.generatePage(context, currentList, page);
        if (res == null) context.error("No data to show.");
        else res.sendTo(context.getSender());
    }
}
