package com.njdaeger.plotmanager.plugin.commands;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.utils.text.Text;
import com.njdaeger.pdk.utils.text.click.ClickAction;
import com.njdaeger.pdk.utils.text.click.ClickString;
import com.njdaeger.pdk.utils.text.hover.HoverAction;
import com.njdaeger.pdk.utils.text.pager.ChatPaginator;
import com.njdaeger.pdk.utils.text.pager.ChatPaginatorBuilder;
import com.njdaeger.pdk.utils.text.pager.ComponentPosition;
import com.njdaeger.pdk.utils.text.pager.components.PageNavigationComponent;
import com.njdaeger.pdk.utils.text.pager.components.ResultCountComponent;
import com.njdaeger.plotmanager.dataaccess.Util;
import com.njdaeger.plotmanager.plugin.IPlotManagerPlugin;
import com.njdaeger.plotmanager.plugin.wrappers.CommandBuilderWrapper;
import com.njdaeger.plotmanager.plugin.wrappers.CommandContextWrapper;
import com.njdaeger.plotmanager.servicelibrary.models.Attribute;
import com.njdaeger.plotmanager.servicelibrary.models.AttributeType;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.serviceprovider.IServiceProvider;
import org.bukkit.ChatColor;
import org.bukkit.Color;

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

        CommandBuilderWrapper.of("attribute")
                .executor(this::attributeCommands)
                .description("test command")
                .usage("/attribute create <name> <type> | /attribute list | /attribute delete <name>")
                .permissions("plotmanager.attribute.create", "plotmanager.attribute.list", "plotmanager.attribute.delete")
                .build()
                .register(plugin);

        this.attributeListPaginator = ChatPaginator.<Attribute, CommandContextWrapper>builder((attr, ctx) ->
                Text.of("| ")
                        .setColor(ChatColor.GRAY)
                        .setBold(true)
                    .appendRoot(attr.getName())
                        .setColor(ChatColor.LIGHT_PURPLE)
                    .appendRoot(" (")
                        .setColor(ChatColor.GRAY)
                    .appendRoot(attr.getType())
                        .setColor(ChatColor.LIGHT_PURPLE)
                    .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view possible values for this type").setColor(ChatColor.GRAY))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/attribute list types " + attr.getType()))
                    .appendRoot(")")
                        .setColor(ChatColor.GRAY))
                .addComponent(new ResultCountComponent<>(false), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .addComponent(Text.of("Attribute List").setColor(ChatColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER)
                .build();

        this.attributeTypeListPaginator = ChatPaginator.<AttributeType, CommandContextWrapper>builder((type, ctx) ->
                Text.of("| ")
                        .setColor(ChatColor.GRAY)
                        .setBold(true)
                    .appendRoot(type.getName())
                        .setColor(ChatColor.LIGHT_PURPLE)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view possible values for this type").setColor(ChatColor.GRAY))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/attribute list types " + type.getName())))
                .addComponent(new ResultCountComponent<>(false), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .addComponent(Text.of("Attribute Type List").setColor(ChatColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER)
                .build();

        this.attributeValueListPaginator = ChatPaginator.<String, CommandContextWrapper>builder((value, ctx) ->
                Text.of("| ")
                        .setColor(ChatColor.GRAY)
                        .setBold(true)
                    .appendRoot(value)
                        .setColor(ChatColor.LIGHT_PURPLE))
                .addComponent(new ResultCountComponent<>(false), ComponentPosition.TOP_LEFT)
                .addComponent(new PageNavigationComponent<>(
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + 1,
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg - 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + (pg + 1),
                        (ctx, res, pg) -> "/attribute " + ctx.getRawCommandString().replace("-page " + pg, "") + " -page " + ((int) Math.ceil(res.size() / 8.0))
                ), ComponentPosition.BOTTOM_CENTER)
                .addComponent(Text.of("Attribute Value List").setColor(ChatColor.LIGHT_PURPLE), ComponentPosition.TOP_CENTER)
                .build();
    }

    /*
    Attribute Commands:

    /attribute list
    /attribute create <name> <type>
//    /attribute edit <attributeName> name|type <value>
//        type edits are special:
//            when a type is chnaged, all assigned plots will be verified to ensure compatibility
//            whichever plots do not conform will have a few options
//                if the type is required, a default value must be specified. (which may be based off of a predicate or mapping)
//                if the type is not required, the attribute can be deleted, mapped to a new value, or defaulted
//        type edits will verify all plots conform to the new type
//        if there are plots that do not conform to the new type, there will be a few options
//
//        1. Delete all plots that do not conform to the new type
    /attribute delete <name>


    Group Commands:

    /group list
    /group create <name>
    /group edit <groupName> <columnBeingEdited> <value>
    /group delete <name>

     */

//    private void attributeCommands(CommandContext ctx) throws PDKCommandException {
//        async2(ctx, (c) -> {
//            try (var transaction = provider.getRequiredService(IServiceTransaction.class)) {
//
//            } catch (Exception e) {
//                throw new PDKCommandException(e.getMessage());
//            }
//        });
//    }
//
//    private CompletableFuture<?> async2(CommandContext ctx, CommandExecutor exec) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                exec.execute(ctx);
//            } catch (PDKCommandException e) {
//                ctx.reply(e.getMessage());
//            }
//            return null;
//        });
//    }

    private void attributeCommands(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        if (context.subCommand((ctx) -> ctx.hasArgAt(0, "create") && ctx.hasArgAt(1, "attribute"), this::createAttribute)) return;

        if (context.subCommandAt(1, "list", true, this::listAttributes)) return;
        if (context.subCommandAt(1, "delete", true, this::deleteAttribute)) return;
        context.error("Unknown subcommand.");
    }

    // /attribute create attribute <name> <type>
    private void createAttribute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var attribServ = transaction.getService(IAttributeService.class);
        var name = context.argAtOrThrow(2, "You must specify a unique name for the attribute.");
        var type = context.argAtOrThrow(3, "You must specify a type for the attribute.");
        var attrib = await(attribServ.createAttribute(context.getUUID(), name, type));
        if (attrib.successful()) {
            context.send(Text.of("[PlotManager] ").setColor(ChatColor.LIGHT_PURPLE)
                    .appendRoot("Attribute ").setColor(ChatColor.GRAY)
                    .appendRoot(name).setColor(ChatColor.LIGHT_PURPLE)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view all attributes").setColor(ChatColor.GRAY))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/attribute list"))
                    .appendRoot(" created using type ").setColor(ChatColor.GRAY)
                    .appendRoot(type).setColor(ChatColor.LIGHT_PURPLE)
                        .setHoverEvent(HoverAction.SHOW_TEXT, Text.of("Click to view possible values for this type").setColor(ChatColor.GRAY))
                        .setClickEvent(ClickAction.RUN_COMMAND, ClickString.of("/attribute list types " + type))
                    .appendRoot(".").setColor(ChatColor.GRAY)
            );
        } else context.error(attrib.message());
    }

    // /attribute list attributes
    private void listAttributes(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var attribServ = transaction.getService(IAttributeService.class);
        int page = context.getFlag("page", 1);
        var attribs = await(attribServ.getAttributes());
        if (attribs.successful()) {
            attributeListPaginator.generatePage(context, attribs.getOrThrow(), page).sendTo(context.getSender());
        } else context.error(attribs.message());
    }

    // /attribute list types [type]
    // without the [type] parameter just lists all the types
    // with the type parameter, it lists all the values possible for the given type.
    private void listAttributeTypes(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        int page = context.getFlag("page", 1);
        var type = context.argAt(2);
        if (type == null) {
            configService
        }
    }

    private void deleteAttribute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var attributeServ = transaction.getService(IAttributeService.class);
        var deleter = context.isPlayer() ? context.asPlayer().getUniqueId() : Util.SYSTEM_UUID;
        var res = await(attributeServ.deleteAttribute(deleter, "test"));
        if (res.successful()) {

        }
    }

}
