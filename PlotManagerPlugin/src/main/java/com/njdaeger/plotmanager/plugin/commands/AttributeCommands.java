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
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandBuilderWrapper;
import com.njdaeger.plotmanager.plugin.commands.wrappers.CommandContextWrapper;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.AttributeType;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.serviceprovider.IServiceProvider;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class AttributeCommands {

    private final IServiceProvider provider;
    private final IPlotManagerPlugin plugin;
    private final IConfigService configService;
    private final ChatPaginator<Attribute, CommandContextWrapper> attributeListPaginator;
    private final ChatPaginator<AttributeType, CommandContextWrapper> attributeTypeListPaginator;
    private final ChatPaginator<String, CommandContextWrapper> attributeValueListPaginator;

    public AttributeCommands(IPlotManagerPlugin plugin, IServiceProvider provider, IConfigService configService) {
        this.plugin = plugin;
        this.provider = provider;
        this.configService = configService;

        CommandBuilderWrapper.of("attribute", "pmattribute")
                .executor(this::attributeCommands)
                .completer(this::attributeCommandCompletions)
                .description("Manage attributes.")
                .usage("/attribute create attribute <name> <type> | /attribute list | /attribute delete <name>")
                .permissions("plotmanager.attribute.create", "plotmanager.attribute.list", "plotmanager.attribute.delete")
                .flag(new PageFlag(ctx -> ctx.hasArgAt(0, "list") && (ctx.hasArgAt(1, "attributes") || ctx.hasArgAt(1, "types"))))
                .build()
                .register(plugin);

        this.attributeListPaginator = ChatPaginator.<Attribute, CommandContextWrapper>builder((attr, ctx) ->
                Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true)
                    .appendRoot(attr.getName()).setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot(" (").setColor(ColorUtils.REGULAR_TEXT)
                    .appendRoot(attr.getType()).setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view possible values for this type").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/attribute list types " + attr.getType()))
                    .appendRoot(")").setColor(ColorUtils.REGULAR_TEXT))
                .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .addComponent(Text.of("Attribute List").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
                .setGrayColor(ColorUtils.REGULAR_TEXT)
                .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
                .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
                .build();

        this.attributeTypeListPaginator = ChatPaginator.<AttributeType, CommandContextWrapper>builder((type, ctx) ->
                Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true)
                    .appendRoot(type.getName()).setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view possible values for this type").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/attribute list types " + type.getName())))
                .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .addComponent(Text.of("Attribute Type List").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
                .setGrayColor(ColorUtils.REGULAR_TEXT)
                .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
                .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
                .build();

        this.attributeValueListPaginator = ChatPaginator.<String, CommandContextWrapper>builder((value, ctx) ->
                Text.of("| ").setColor(ColorUtils.REGULAR_TEXT).setBold(true)
                    .appendRoot(value).setColor(ColorUtils.HIGHLIGHT_TEXT))
                .addComponent(new ResultCountComponent<>(true), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .addComponent(Text.of("Attribute Value List").setColor(ColorUtils.HIGHLIGHT_TEXT), ComponentPosition.TOP_CENTER)
                .setGrayColor(ColorUtils.REGULAR_TEXT)
                .setHighlightColor(ColorUtils.HIGHLIGHT_TEXT)
                .setGrayedOutColor(ColorUtils.GRAYED_TEXT)
                .build();
    }

    private void attributeCommandCompletions(TabContext context) throws PDKCommandException {
        context.completionAt(0, "create", "list", "delete");
        context.subCompletionAt(1, "create", (c) -> c.completion("attribute"));
        context.subCompletionAt(2, "attribute", (c) -> c.completion(configService.getAttributeTypes().stream().map(AttributeType::getName).toArray(String[]::new)));

        context.subCompletionAt(1, "list", (c) -> c.completion("attributes", "types"));
        context.subCompletionAt(1, "delete", (c) -> c.completion("attribute"));
    }

    private void attributeCommands(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        if (context.hasArgAt(0, "create") && context.hasArgAt(1, "attribute")) {
            createAttribute(transaction, context);
            return;
        }
        if (context.hasArgAt(0, "list") && context.hasArgAt(1, "attributes")) {
            listAttributes(transaction, context);
            return;
        }
        if (context.hasArgAt(0, "list") && context.hasArgAt(1, "types")) {
            listAttributeTypes(transaction, context);
            return;
        }
        if (context.hasArgAt(0, "delete")) {
            deleteAttribute(transaction, context);
            return;
        }
        context.error("Unknown subcommand.");
    }

    // /attribute create attribute <name> <type>
    private void createAttribute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var attribServ = transaction.getService(IAttributeService.class);
        var name = context.argAtOrThrow(2, "You must specify a unique name for the attribute.");
        var type = context.argAtOrThrow(3, "You must specify a type for the attribute.");
        var attrib = await(attribServ.createAttribute(context.getUUID(), name, type));
        if (attrib.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot("Attribute ").setColor(ColorUtils.REGULAR_TEXT)
                    .appendRoot(name).setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view all attributes").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/attribute list attributes"))
                    .appendRoot(" created using type ").setColor(ColorUtils.REGULAR_TEXT)
                    .appendRoot(type).setColor(ColorUtils.ACTION_TEXT).setUnderlined(true).setItalic(true)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view possible values for this type").setColor(ColorUtils.REGULAR_TEXT))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/attribute list types " + type))
                    .appendRoot(".").setColor(ColorUtils.REGULAR_TEXT)
            );
        } else context.error(attrib.message());
    }

    // /attribute list attributes
    private void listAttributes(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var attribServ = transaction.getService(IAttributeService.class);
        int page = context.getFlag("page", 1);
        var attribs = await(attribServ.getAttributes());
        if (attribs.successful()) {
            var res = attributeListPaginator.generatePage(context, attribs.getOrThrow(), page);
            if (res != null) res.sendTo(context.getSender());
            else context.error("No results to show.");
        } else context.error(attribs.message());
    }

    // /attribute list types [type]
    // without the [type] parameter just lists all the types
    // with the type parameter, it lists all the values possible for the given type.
    private void listAttributeTypes(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        int page = context.getFlag("page", 1);
        var type = context.argAt(2);
        if (type == null) {
            var res = attributeTypeListPaginator.generatePage(context, configService.getAttributeTypes(), page);
            if (res != null) res.sendTo(context.getSender());
            else context.error("No results to show.");
        } else {
            var attribType = configService.getAttributeType(type);
            if (attribType == null) {
                context.error("An attribute type with that name does not exist.");
                return;
            }
            var res = attributeValueListPaginator.generatePage(context, attribType.getValues(), page);
            if (res != null) res.sendTo(context.getSender());
            else context.error("No results to show.");
        }
    }

    // /attribute delete attribute <name>
    /*
    if this attribute is required by plots, attribute cannot be deleted until it is removed from the requirements
     */
    private void deleteAttribute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var attributeServ = transaction.getService(IAttributeService.class);
        var deleter = context.getUUID();
        var name = context.argAtOrThrow(2, "You must specify a name for the attribute.");
        var res = await(attributeServ.deleteAttribute(deleter, name));
        if (res.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot("Attribute ").setColor(ColorUtils.REGULAR_TEXT)
                    .appendRoot(name).setColor(ColorUtils.HIGHLIGHT_TEXT)
                    .appendRoot(" deleted.").setColor(ColorUtils.REGULAR_TEXT)
            );
        } else context.error(res.message());

    }

}
