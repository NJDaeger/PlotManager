package com.njdaeger.plotmanager.plugin.commands;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import com.njdaeger.plotmanager.servicelibrary.ColorUtils;
import com.njdaeger.plotmanager.plugin.IPlotManagerPlugin;
import com.njdaeger.plotmanager.plugin.commands.flags.PageFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.ParentFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.PlotFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.PlotGroupFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.ReferenceFlag;
import com.njdaeger.plotmanager.plugin.commands.flags.SessionFlag;
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandBuilderWrapper;
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.plotmanager.servicelibrary.PlotBuilder;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IPlotService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.serviceprovider.IServiceProvider;

import java.util.ArrayList;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class PlotCommands {

    private final IServiceProvider provider;
    private final IPlotManagerPlugin plugin;
    private final IConfigService configService;
    private final ICacheService cacheService;
    private final ChatPaginator<Plot, CommandContextWrapper> parentSelectorPaginator;
    private final ChatPaginator<String, CommandContextWrapper> attributeValueSelectListPaginator;
    private final ChatPaginator<PlotBuilder.PlotBuilderListItem, CommandContextWrapper> plotCreationMenuPaginator;

    public PlotCommands(IPlotManagerPlugin plugin, IServiceProvider provider, IConfigService configService, ICacheService cacheService) {
        this.provider = provider;
        this.plugin = plugin;
        this.cacheService = cacheService;
        this.configService = configService;

        CommandBuilderWrapper.of("plot")
                .executor(this::plotCommand)
                .completer(this::plotTabCompletion)
                .min(1)
                .flag(new ParentFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new ReferenceFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new PlotGroupFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new SessionFlag(ctx -> ctx.argAt(0).equalsIgnoreCase("edit") || ctx.argAt(0).equalsIgnoreCase("create")))
                .flag(new PlotFlag(cacheService, ctx -> ctx.argAt(0).equalsIgnoreCase("edit")))
                .flag(new PageFlag(ctx -> ctx.argAt(0).equalsIgnoreCase("create") || ctx.argAt(0).equalsIgnoreCase("edit") &&
                        ((ctx.hasArgAt(1) && ctx.argAt(1).equalsIgnoreCase("attribute") && ctx.hasArgAt(3) && ctx.argAt(3).equalsIgnoreCase("select")) ||
                        (ctx.hasArgAt(1) && ctx.argAt(1).equalsIgnoreCase("parent") && ctx.hasArgAt(2) && ctx.argAt(2).equalsIgnoreCase("select")))))
                .permissions("plotmanager.plot.create", "plotmanager.plot.edit", "plotmanager.plot.cancel", "plotmanager.plot.finish")
                .build()
                .register(plugin);

        this.parentSelectorPaginator = ChatPaginator.<Plot, CommandContextWrapper>builder((plt, ctx) -> {
                var editSession = ctx.hasFlag("session");
                return Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true)
                        .appendRoot(String.valueOf(plt.getId())).setColor(ColorUtils.HIGHLIGHT_TEXT)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of(plt.getAttribute("description").getValue()).setColor(ColorUtils.REGULAR_TEXT))
                        .appendRoot(" ")
                        .appendRoot("[select]").setBold(true).setUnderlined(true).setColor(ColorUtils.ACTION_TEXT)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to select this plot to be the parent.").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit parent set " + plt.getId() + (editSession ? " -session" : "")));
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

        this.attributeValueSelectListPaginator = ChatPaginator.<String, CommandContextWrapper>builder((value, ctx) ->
                        Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true)
                                .appendRoot(value).setColor(ColorUtils.HIGHLIGHT_TEXT)
                                .appendRoot(" ")
                                .appendRoot("[select]").setBold(true).setUnderlined(true).setColor(ColorUtils.ACTION_TEXT)
                                .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to select this value for this attribute").setColor(ColorUtils.REGULAR_TEXT))
                                .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/plot edit attribute " + ctx.argAt(2) + " set " + value + (ctx.hasFlag("session") ? " -session" : "")))
                )
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

        this.plotCreationMenuPaginator = ChatPaginator.<PlotBuilder.PlotBuilderListItem, CommandContextWrapper>builder((item, ctx) -> {
                        var line = Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true);
                        if (item.isRequired()) {
                            line.appendRoot("*").setColor(ColorUtils.ERROR_TEXT)
                                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("This attribute is required.").setColor(ColorUtils.REGULAR_TEXT));
                        }
                        line.appendRoot(item.getItemName()).setColor(ColorUtils.REGULAR_TEXT)
                                .appendRoot(": ").setColor(ColorUtils.REGULAR_TEXT)
                                .appendRoot(item.getValue());
                        return line;
                })
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/plot -session create -page " + 1,
                        (ctx, res, pg) -> "/plot -session create -page " + (pg - 1),
                        (ctx, res, pg) -> "/plot -session create -page " + (pg + 1),
                        (ctx, res, pg) -> "/plot -session create -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .addComponent(Text.of("Plot Creation Menu").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
                .addComponent(Text.of("[Finish]").setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to finish the plot creation.").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot finish")), ComponentPosition.BOTTOM_LEFT)
                .addComponent(Text.of("[Cancel]").setColor(ColorUtils.ERROR_TEXT).setUnderlined(true).setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to cancel the plot creation.").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.SUGGEST_COMMAND, ClickString.of("/plot cancel")), ComponentPosition.BOTTOM_RIGHT)
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
        } else {
            context.error("Invalid argument " + context.argAt(0) + ". Expected 'create', 'edit', 'cancel', or 'finish'.");
        }
    }

    private void plotTabCompletion(TabContext context) {
        context.completionAt(0, "create", "edit", "cancel", "finish");
        if (context.hasArgAt(0) && context.argAt(0).equalsIgnoreCase("edit")) {
            context.completionAt(1, "attribute", "parent");
            if (context.hasArgAt(1) && context.argAt(1).equalsIgnoreCase("attribute")) {
                context.completionAt(2, cacheService.getAttributeCache().keySet().toArray(String[]::new));
                context.completionAt(3, "remove", "select", "set");
                if (context.hasArgAt(3) && context.argAt(3).equalsIgnoreCase("set")) {
                    var attr = cacheService.getAttributeCache().get(context.argAt(2));
                    if (attr == null) return;
                    var type = configService.getAttributeType(attr.getType());
                    if (type == null || type.getValues().isEmpty()) return;
                    context.completionAt(4, type.getValues().stream().map(String::valueOf).toArray(String[]::new));
                }
            } else if (context.hasArgAt(1) && context.argAt(1).equalsIgnoreCase("parent")) {
                context.completionAt(2, "remove", "select", "set");
                if (context.hasArgAt(2) && context.argAt(2).equalsIgnoreCase("set")) {
                    context.completionAt(3, cacheService.getPlotCache().keySet().stream().map(String::valueOf).toArray(String[]::new));
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
            var menu = plotCreationMenuPaginator.generatePage(context, builder.getBuilderListItems(requiredAttributes, attributeService, configService), page);
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

            var menu = plotCreationMenuPaginator.generatePage(context, builder.getBuilderListItems(requiredAttributes, attributeService, configService), page);
            if (menu == null) context.error("Invalid page number.");
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
        else if (!editSession && editPlot == null) context.error("No plot to edit. Please use the -plot flag to specify a plot to edit, or use the -session flag to edit the plot builder.");

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
                var menu = plotCreationMenuPaginator.generatePage(context, builder.getBuilderListItems(configService.getRequiredPlotAttributes(), transaction.getService(IAttributeService.class), configService), 1);
                if (menu == null) context.error("Invalid page number.");
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

        if (res == null) context.error("Invalid page " + page + ".");
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
            var menu = plotCreationMenuPaginator.generatePage(context, builder.getBuilderListItems(configService.getRequiredPlotAttributes(), attributeService, configService), 1);
            if (menu == null) context.error("Invalid page number.");
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
            var menu = plotCreationMenuPaginator.generatePage(context, builder.getBuilderListItems(configService.getRequiredPlotAttributes(), transaction.getService(IAttributeService.class), configService), 1);
            if (menu == null) context.error("Invalid page number.");
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

        if (res == null) context.error("Invalid page " + page + ".");
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
            var menu = plotCreationMenuPaginator.generatePage(context, builder.getBuilderListItems(configService.getRequiredPlotAttributes(), transaction.getService(IAttributeService.class), configService), 1);
            if (menu == null) context.error("Invalid page number.");
            else menu.sendTo(context.getSender());
        } else {
            var res = await(plotService.setPlotParent(context.getUUID(), plotToEdit.getId(), parentId));
            if (!res.successful()) context.error(res.message());
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Parent set to ").setColor(ColorUtils.REGULAR_TEXT).appendRoot(String.valueOf(parent.getId())).setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot(".").setColor(ColorUtils.REGULAR_TEXT));
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
        } else context.error(res.message());
    }

    private void cancelPlotCreation(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var plotService = transaction.getService(IPlotService.class);

        var res = await(plotService.cancelPlotBuilder(context.getUUID()));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT).appendRoot("Plot creation cancelled.").setColor(ColorUtils.REGULAR_TEXT));
        } else context.error(res.message());
    }
}
