package com.njdaeger.taskmanager.plugin.redblock;

import com.njdaeger.taskmanager.servicelibrary.models.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class RedblockTask extends Task {
    public RedblockTask(Task task) {
        super(task.getTaskId(), task.getTaskType(), task.getParent(), task.getProject(), task.getTaskKey(), task.getAttributes());
    }

    public Status getStatus() {
        try {
            return Status.valueOf(getAttribute(RedblockConstants.STATUS_ATTRIBUTE).getValue().toUpperCase());
        } catch (Exception e) {
            return Status.INCOMPLETE;
        }
    }

    public boolean isPending() {
        return getStatus() == Status.PENDING;
    }

    public boolean isIncomplete() {
        return getStatus() == Status.INCOMPLETE;
    }

    public String getMinRank() {
        var attr = getAttribute(RedblockConstants.MINIMUM_RANK);
        return attr == null ? null : attr.getValue();
    }

    public Location getLocation() {
        var locString = getAttribute(RedblockConstants.LOCATION_ATTRIBUTE).getValue();
        var split = locString.split(",");
        return new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
    }

    public enum Status {
        INCOMPLETE, PENDING, APPROVED, DELETED
    }

}
