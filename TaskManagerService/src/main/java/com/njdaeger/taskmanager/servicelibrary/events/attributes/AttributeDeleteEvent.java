package com.njdaeger.taskmanager.servicelibrary.events.attributes;

import com.njdaeger.taskmanager.servicelibrary.events.UserActionedEvent;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class AttributeDeleteEvent extends UserActionedEvent {

    private static final HandlerList handlers = new HandlerList();
    private String deletedAttribute;

    public AttributeDeleteEvent(UUID actionedBy, String attribute) {
        super(actionedBy);
        this.deletedAttribute = attribute;
    }

    /**
     * Gets the attribute that was deleted.
     * @return The attribute that was deleted.
     */
    public String getDeletedAttribute() {
        return deletedAttribute;
    }

    /**
     * Sets the attribute to be deleted.
     * @param attribute The attribute to be deleted.
     */
    public void setDeletedAttribute(String attribute) {
        this.deletedAttribute = attribute;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
