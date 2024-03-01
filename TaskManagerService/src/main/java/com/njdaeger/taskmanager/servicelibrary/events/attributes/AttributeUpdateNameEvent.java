package com.njdaeger.taskmanager.servicelibrary.events.attributes;

import com.njdaeger.taskmanager.servicelibrary.events.UserActionedEvent;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class AttributeUpdateNameEvent extends UserActionedEvent {

    private static final HandlerList handlers = new HandlerList();
    private String modifiedAttribute;
    private String newAttributeName;

    public AttributeUpdateNameEvent(UUID updater, String attribute, String newAttributeName) {
        super(updater);
        this.modifiedAttribute = attribute;
        this.newAttributeName = newAttributeName;
    }

    /**
     * Gets the attribute that was modified.
     * @return The attribute that was modified.
     */
    public String getModifiedAttribute() {
        return modifiedAttribute;
    }

    /**
     * Sets the attribute to be modified.
     * @param attribute The attribute to be modified.
     */
    public void setModifiedAttribute(String attribute) {
        this.modifiedAttribute = attribute;
    }

    /**
     * Gets the new name of the attribute.
     * @return The new name of the attribute.
     */
    public String getNewName() {
        return newAttributeName;
    }

    /**
     * Sets the new name of the attribute.
     * @param newName The new name of the attribute.
     */
    public void setNewName(String newName) {
        this.newAttributeName = newName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
