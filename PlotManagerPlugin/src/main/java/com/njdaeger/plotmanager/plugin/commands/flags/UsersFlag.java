package com.njdaeger.plotmanager.plugin.commands.flags;

import com.njdaeger.pdk.command.TabContext;
import com.njdaeger.pdk.command.flag.OptionalFlag;

import java.util.function.Predicate;

public class UsersFlag extends OptionalFlag {

    public UsersFlag(Predicate<TabContext> onlyAllowWhen) {
        super(onlyAllowWhen, "Specify if this command is to list users of the plot.", "-users", "users");
    }

}
