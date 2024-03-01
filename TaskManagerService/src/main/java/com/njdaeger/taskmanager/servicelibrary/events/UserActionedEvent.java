package com.njdaeger.taskmanager.servicelibrary.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.UUID;

public abstract class UserActionedEvent extends Event implements Cancellable {

    protected final UUID actionedBy;
    private boolean cancelled;
    private String cancelMessage;

    public UserActionedEvent(UUID actionedBy) {
        this.actionedBy = actionedBy;
        this.cancelled = false;
        this.cancelMessage = "User actioned event was cancelled.";
    }

    public UUID getActionedBy() {
        return actionedBy;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public String getCancelMessage() {
        return cancelMessage;
    }

    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

}
