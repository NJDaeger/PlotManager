package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.flag.OptionalFlag;

import java.util.function.Predicate;

public class ForceFlag extends OptionalFlag {

    public ForceFlag(Predicate<TabContext> onlyAllowWhen) {
        super(onlyAllowWhen, "Force an action to be performed.", "-force", "force");
    }
}
