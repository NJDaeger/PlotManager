package com.njdaeger.taskmanager.servicelibrary.events.attributes;

import com.njdaeger.taskmanager.servicelibrary.events.UserActionedEvent;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class AttributeUpdateTypeEvent extends UserActionedEvent {

    private static final HandlerList handlers = new HandlerList();

    private String modifiedAttribute;
    private String newAttributeType;
    private String oldAttributeType;

    public AttributeUpdateTypeEvent(UUID updater, String modifiedAttribute, String newAttributeType, String oldAttributeType) {
        super(updater);
        this.modifiedAttribute = modifiedAttribute;
        this.newAttributeType = newAttributeType;
        this.oldAttributeType = oldAttributeType;
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
     * Gets the new type of the attribute.
     * @return The new type of the attribute.
     */
    public String getNewType() {
        return newAttributeType;
    }

    /**
     * Sets the new type of the attribute.
     * @param newType The new type of the attribute.
     */
    public void setNewType(String newType) {
        this.newAttributeType = newType;
    }

    /**
     * Gets the old type of the attribute.
     * @return The old type of the attribute.
     */
    public String getOldType() {
        return oldAttributeType;
    }

    /**
     * Sets the old type of the attribute.
     * @param oldType The old type of the attribute.
     */
    public void setOldType(String oldType) {
        this.oldAttributeType = oldType;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
