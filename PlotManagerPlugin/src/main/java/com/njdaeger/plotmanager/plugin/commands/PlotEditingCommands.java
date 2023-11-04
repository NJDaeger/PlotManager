package com.njdaeger.plotmanager.plugin.commands;

import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.plotmanager.plugin.PermissionConstants;
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.plotmanager.servicelibrary.ColorUtils;
import com.njdaeger.plotmanager.servicelibrary.PlotChatListItems;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.Plot;
import com.njdaeger.plotmanager.servicelibrary.models.PlotAttribute;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.ICacheService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.services.IPlotService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;

import java.util.ArrayList;
import java.util.List;

import static com.njdaeger.plotmanager.dataaccess.Util.await;
import static com.njdaeger.plotmanager.plugin.commands.Paginators.*;

/**
 * Commands that are used to edit plot information
 */
public class PlotEditingCommands {

    static void editPlot(IAttributeService attributeService, IConfigService configService, ICacheService cacheService, IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var editSession = context.hasFlag("session");
        var editPlot = context.<Plot>getFlag("plot");

        if (editSession && editPlot != null) context.error("Flags -session and -plot are mutually exclusive. Please only use one of them.");
        else if (!editSession && editPlot == null) editPlot = context.resolvePlot();
        if (!editSession && editPlot == null) context.error("No plot to edit. Please use the -plot flag to specify a plot to edit, or use the -session flag to edit the plot builder.");

        var editing = context.argAt(1);
        if (editing == null) {
            if (editPlot == null) {
                if (!editSession) context.error("No plot to edit. Please use the -plot flag to specify a plot to edit, or use the -session flag to edit the plot creation session.");
                var builderRes = await(transaction.getService(IPlotService.class).getPlotBuilder(context.getUUID()));
                if (!builderRes.successful()) context.error(builderRes.message());
                var builder = builderRes.getOrThrow();
                var menu = plotCreationMenuPaginator.generatePage(context, PlotChatListItems.getListItemsForBuilder(configService.getRequiredPlotAttributes(), transaction.getService(IAttributeService.class), configService, builder), 1);
                if (menu == null) context.error("No data to show.");
                else menu.sendTo(context.getSender());
            } else {
                var res = plotEditMenuPaginator.generatePage(context, PlotChatListItems.getListItemsForPlot(configService.getRequiredPlotAttributes(), attributeService, configService, editPlot, false), 1);
                if (res == null) context.error("No data to show.");
                else res.sendTo(context.getSender());
            }
        } else if (editing.equalsIgnoreCase("attribute")) {
            editAttribute(configService, transaction, context, editPlot);
        } else if (editing.equalsIgnoreCase("parent")) {
            editParent(configService, cacheService, transaction, context, editPlot);
        } else {
            context.error("Invalid edit type " + editing + ". Expected 'attribute' or 'parent'.");
        }
    }

    private static void editAttribute(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
        if (!context.hasArgAt(2)) {
            selectAttributeToAddOrEdit(configService, transaction, context, plotToEdit);
            return;
        }
        var attribute = context.argAt(2);
        var action = context.argAt(3);
        if (action == null) context.error("No action provided. Expected 'remove', 'select', or 'set'.");

        if (action.equalsIgnoreCase("remove")) {
            if (!context.hasPermission(PermissionConstants.PLOT_DELETE_ATTRIBUTE_COMMAND)) context.noPermission();
            removeAttribute(configService, transaction, context, attribute, plotToEdit);
        } else if (action.equalsIgnoreCase("select")) {
            selectAttribute(configService, transaction, context, attribute, plotToEdit);
        } else if (action.equalsIgnoreCase("set")) {
            if (!context.hasPermission(PermissionConstants.PLOT_MODIFY_ATTRIBUTE_COMMAND)) context.noPermission();
            setAttribute(configService, transaction, context, attribute, plotToEdit);
        } else {
            context.error("Invalid action " + action + ". Expected 'remove', 'select', or 'set'.");
        }
    }

    private static void selectAttributeToAddOrEdit(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
        var page = context.getFlag("page", 1);
        var attributeService = transaction.getService(IAttributeService.class);

        var attributesRes = await(attributeService.getAttributes());
        var attributes = List.<Attribute>of();
        if (attributesRes.successful()) attributes = new ArrayList<>(attributesRes.getOrThrow());

        var res = attributeSelectListPaginator.generatePage(context, attributes.stream().map(Attribute::getName).toList() , page);

        if (res == null) context.error("No pages to display.");
        context.send(res.getMessage());
    }

    private static void removeAttribute(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context, String attribute, Plot plotToEdit) throws PDKCommandException {
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

    private static void selectAttribute(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context, String attribute, Plot plotToEdit) throws PDKCommandException {
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

    private static void setAttribute(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context, String attribute, Plot plotToEdit) throws PDKCommandException {
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

    private static void editParent(IConfigService configService, ICacheService cacheService, IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
        var action = context.argAt(2);
        if (action == null) context.error("Invalid action " + action + ". Expected 'remove', 'select', or 'set'.");

        if (action.equalsIgnoreCase("remove")) {
            if (!context.hasPermission(PermissionConstants.PLOT_DELETE_PARENT_COMMAND)) context.noPermission();
            removeParent(configService, transaction, context, plotToEdit);
        } else if (action.equalsIgnoreCase("select")) {
            selectParent(cacheService, transaction, context, plotToEdit);
        } else if (action.equalsIgnoreCase("set")) {
            if (!context.hasPermission(PermissionConstants.PLOT_MODIFY_PARENT_COMMAND)) context.noPermission();
            setParent(configService, transaction, context, plotToEdit);
        } else {
            context.error("Invalid action " + action + ". Expected 'remove', 'select', or 'set'.");
        }
    }

    private static void removeParent(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
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

    private static void selectParent(ICacheService cacheService, IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
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

    private static void setParent(IConfigService configService, IServiceTransaction transaction, CommandContextWrapper context, Plot plotToEdit) throws PDKCommandException {
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


}
