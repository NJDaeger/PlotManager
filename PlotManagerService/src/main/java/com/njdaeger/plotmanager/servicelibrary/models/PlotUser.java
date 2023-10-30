package com.njdaeger.plotmanager.servicelibrary.models;

public class PlotUser {

    private int id;
    private User user;
    private boolean deleted;

    public PlotUser(int id, User user, boolean deleted) {
        this.id = id;
        this.user = user;
        this.deleted = deleted;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
