package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.flag.OptionalFlag;

import java.util.function.Predicate;

public class SessionFlag extends OptionalFlag {

    public SessionFlag(Predicate<TabContext> onlyAllowWhen) {
        super(onlyAllowWhen, "Specify if this command is to modify the current plot being made.", "-session", "session");
    }
}
