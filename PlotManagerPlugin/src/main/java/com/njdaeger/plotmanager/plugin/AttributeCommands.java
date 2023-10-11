package com.njdaeger.plotmanager.plugin;

import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.plotmanager.servicelibrary.services.IConfigService;
import com.njdaeger.serviceprovider.IServiceProvider;

public class AttributeCommands {

    private final IServiceProvider provider;
    private final IPlotManagerPlugin plugin;
    private final IConfigService configService;

    public AttributeCommands(IPlotManagerPlugin plugin, IServiceProvider provider, IConfigService configService) {
        this.plugin = plugin;
        this.provider = provider;
        this.configService = configService;

        CommandBuilder.of("attribute")
                .executor(this::attributeCommands)
                .build()
                .register(plugin);
    }

    private void attributeCommands(CommandContext ctx) throws PDKCommandException {
        if (ctx.subCommandAt(0, "list", true, this::listAttributes)) return;
        if (ctx.subCommandAt(0, "create", true, this::createAttribute)) return;
    }

    private void listAttributes(CommandContext ctx) {

    }

    private void createAttribute(CommandContext ctx) {

    }

    private void deleteAttribute(CommandContext ctx) {

    }

}
