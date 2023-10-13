package com.njdaeger.plotmanager.plugin;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.plotmanager.dataaccess.Util;
import com.njdaeger.plotmanager.plugin.commands.CommandBuilderWrapper;
import com.njdaeger.plotmanager.plugin.commands.CommandContextWrapper;
import com.njdaeger.plotmanager.servicelibrary.services.IAttributeService;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.plotmanager.servicelibrary.transactional.IServiceTransaction;
import com.njdaeger.serviceprovider.IServiceProvider;

import java.util.UUID;

import static com.njdaeger.plotmanager.dataaccess.Util.await;

public class AttributeCommands {

    private final IServiceProvider provider;
    private final IPlotManagerPlugin plugin;
    private final IConfigService configService;

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
        if (context.subCommandAt(1, "create", true, this::createAttribute)) return;
        if (context.subCommandAt(1, "list", true, this::listAttributes)) return;
    }

    // /attribute create <name> <type>
    private void createAttribute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var attribServ = transaction.getService(IAttributeService.class);
        var creator = context.isPlayer() ? context.asPlayer().getUniqueId() : Util.SYSTEM_UUID;
        var attrib = await(attribServ.createAttribute(creator, "test", "string"));
        if (attrib.successful()) {
            context.send("Success");
        } else {
            context.error("Failure");
        }
    }

    private void listAttributes(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {

    }

    private void deleteAttribute(IServiceTransaction transaction, CommandContextWrapper context) throws PDKCommandException {
        var attributeServ = transaction.getService(IAttributeService.class);
        var deleter = context.isPlayer() ? context.asPlayer().getUniqueId() : Util.SYSTEM_UUID;
        var res = await(attributeServ.deleteAttribute(deleter, "test"));
        if (res.successful()) {

        }
    }

}
