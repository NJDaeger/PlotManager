package com.njdaeger.taskmanager.plugin.commands.wrappers;

import com.njdaeger.pdk.command.CommandBuilder;
import com.njdaeger.pdk.command.PDKCommand;
import com.njdaeger.pdk.command.TabExecutor;
import com.njdaeger.pdk.command.flag.Flag;
import org.bukkit.plugin.Plugin;

public class CommandBuilderWrapper {

    private final CommandBuilder builder;

    private CommandBuilderWrapper(String... aliases) {
        this.builder = CommandBuilder.of(aliases);
    }

    public static CommandBuilderWrapper of(String... aliases) {
        return new CommandBuilderWrapper(aliases);
    }

    public CommandBuilderWrapper executor(TransactionalCommandExecutor executor) {
        builder.executor(executor);
        return this;
    }

    public CommandBuilderWrapper description(String description) {
        builder.description(description);
        return this;
    }

    public CommandBuilderWrapper usage(String usage) {
        builder.usage(usage);
        return this;
    }

    public CommandBuilderWrapper permissions(String... permissions) {
        builder.permissions(permissions);
        return this;
    }

    public CommandBuilderWrapper completer(TabExecutor executor) {
        builder.completer(executor);
        return this;
    }

    public <V, T extends Flag<V>> CommandBuilderWrapper flag(T flag) {
        builder.flag(flag);
        return this;
    }

    public CommandBuilderWrapper min(int min) {
        builder.min(min);
        return this;
    }

    public CommandBuilderWrapper max(int max) {
        builder.max(max);
        return this;
    }

    public PDKCommand build() {
        return builder.build();
    }

    public void register(Plugin plugin) {
        builder.register(plugin);
    }

}
