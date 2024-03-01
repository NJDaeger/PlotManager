package com.njdaeger.taskmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.ArgumentParseException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;

import java.util.function.Predicate;
import java.util.stream.IntStream;

public class RadiusFlag extends Flag<Integer> {

    public RadiusFlag(Predicate<TabContext> onlyAllowWhen) {
        super(onlyAllowWhen, Integer.class, "Filter the maximum distance a plot can be to show up in a search", "-radius <radius>", "radius");
    }

    @Override
    public Integer parse(CommandContext context, String argument) throws PDKCommandException {
        int parsed;
        try {
            parsed = Integer.parseInt(argument);
        } catch (NumberFormatException ignored) {
            throw new ArgumentParseException("Integer argument unable to be parsed. Input: " + argument, true);
        }
        return parsed;
    }

    @Override
    public void complete(TabContext context) throws PDKCommandException {
        if (context.getCurrent() == null || context.getCurrent().isEmpty()) {
            context.completion(IntStream.rangeClosed(1, 9).mapToObj(String::valueOf).toArray(String[]::new));
            return;
        }
        try {
            int cur = Integer.parseInt(context.getCurrent());
            context.completion(IntStream.rangeClosed(cur * 10, (cur * 10) + 10).mapToObj(String::valueOf).toArray(String[]::new));
        } catch (NumberFormatException ignored) {
        }
    }
}
