package com.njdaeger.plotmanager.service.models;

import java.util.UUID;

public class World {

    private final int id;
    private final UUID worldUuid;
    private final String worldName;

    public World(int id, UUID worldUuid, String worldName) {
        this.id = id;
        this.worldUuid = worldUuid;
        this.worldName = worldName;
    }

    public int getId() {
        return id;
    }

    public String getWorldName() {
        return worldName;
    }

    public UUID getWorldUuid() {
        return worldUuid;
    }

}
