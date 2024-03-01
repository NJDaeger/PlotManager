package com.njdaeger.taskmanager.servicelibrary.events.attributes;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class AttributeCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final UUID createdBy;
    private String attributeName;
    private String attributeType;
    private boolean cancelled;
    private String cancelMessage;

    /**
     * Called when an attribute is created.
     * @param createdBy The UUID of the user who created the attribute.
     * @param attributeName The name of the attribute.
     */
    public AttributeCreateEvent(UUID createdBy, String attributeName) {
        this.createdBy = createdBy;
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.cancelled = false;
        this.cancelMessage = "Attribute creation was cancelled.";
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
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
