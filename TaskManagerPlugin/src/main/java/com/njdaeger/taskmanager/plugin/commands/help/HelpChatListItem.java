package com.njdaeger.taskmanager.plugin.commands.help;

import com.njdaeger.pdk.utils.text.Text;

import java.util.List;

public class HelpChatListItem {

    private final String command;
    private final Text.Section description;
    private final List<HelpChatListItem> subCommands;
    private HelpChatListItem parent;

    public HelpChatListItem(String command, String description, HelpChatListItem... subCommands) {
        this.command = command;
        this.description = Text.of(description);
        this.subCommands = List.of(subCommands);
        this.parent = null;
        this.subCommands.forEach(subCommand -> subCommand.parent = this);
    }

    public HelpChatListItem(String command, Text.Section description, HelpChatListItem... subCommands) {
        this.command = command;
        this.description = description;
        this.subCommands = List.of(subCommands);
        this.parent = null;
        this.subCommands.forEach(subCommand -> subCommand.parent = this);
    }

    public HelpChatListItem getParent() {
        return parent;
    }

    public String getHelpCommandPath() {
        if (parent == null) return command;
        return parent.getHelpCommandPath() + " " + command;
    }

    public String getCommand() {
        return command;
    }

    public Text.Section getDescription() {
        return description;
    }

    public List<HelpChatListItem> getSubCommands() {
        return subCommands;
    }

}
