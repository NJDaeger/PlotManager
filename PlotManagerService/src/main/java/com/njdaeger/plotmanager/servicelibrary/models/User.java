package com.njdaeger.plotmanager.servicelibrary.models;

import java.util.UUID;

public class User {

    private int id;
    private UUID userId;
    private String lastKnownName;

    public User(int id, UUID userId, String lastKnownName) {
        this.id = id;
        this.userId = userId;
        this.lastKnownName = lastKnownName;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public int getId() {
        return id;
    }

}
