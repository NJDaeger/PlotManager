package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.CommandContext;
import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.exception.ArgumentParseException;
import com.njdaeger.pdk.command.exception.PDKCommandException;
import com.njdaeger.pdk.command.flag.Flag;

import java.util.function.Predicate;
import java.util.stream.IntStream;

public class PageFlag extends Flag<Integer> {

    public PageFlag(Predicate<TabContext> onlyAllowWhen) {
        super(onlyAllowWhen, Integer.class, "Which result page to view", "-page <pageNumber>", "page");
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
