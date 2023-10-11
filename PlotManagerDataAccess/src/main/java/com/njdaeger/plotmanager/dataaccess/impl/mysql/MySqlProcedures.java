package com.njdaeger.plotmanager.dataaccess.impl.mysql;

import com.google.common.base.Verify;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.plotmanager.dataaccess.IProcedure;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySqlProcedures implements IProcedure {

    public MySqlProcedures() {
    }

    @Override
    public Pair<String, Map<String, Object>> insertAttribute(int createdBy, String name, String type) {
        return Pair.of("insert into Attribute (name, type, createdBy) values (?, ?, ?)", Map.of(
                "1", name,
                "2", type,
                "3", createdBy
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectAttributeById(int attributeId) {
        return Pair.of("select * from Attribute where id = ?", Map.of("1", attributeId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectAttributeByName(String attributeName) {
        return Pair.of("select * from Attribute where name = ?", Map.of("1", attributeName));
    }

    @Override
    public Pair<String, Map<String, Object>> selectAttributes() {
        return Pair.of("select * from Attribute", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> updateAttribute(int modifiedBy, int attributeId, String name, String type) {
        if (name == null) {
            return Pair.of("update Attribute set type = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", type,
                    "2", modifiedBy,
                    "3", attributeId
            ));
        } else if (type == null) {
            return Pair.of("update Attribute set name = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", name,
                    "2", modifiedBy,
                    "3", attributeId
            ));
        } else {
            return Pair.of("update Attribute set name = ?, type = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", name,
                    "2", type,
                    "3", modifiedBy,
                    "4", attributeId
            ));
        }
    }

    @Override
    public Pair<String, Map<String, Object>> deleteAttribute(int deletedBy, int attributeId) {
        return Pair.of("update Attribute set deleted = 1, modifiedBy = ? where id = ?", Map.of(
                "1", deletedBy,
                "2", attributeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> insertSystemUser() {
        return Pair.of("insert ignore into User (uuid, username, createdBy) values (?, ?, ?)", Map.of(
                "1", UUID.fromString("00000000-0000-0000-0000-000000000000").toString(),
                "2", "#system",
                "3", 0
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectUsers() {
        return Pair.of("select * from User", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectUserById(int userId) {
        return Pair.of("select * from User where id = ?", Map.of("1", userId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectUserByUuid(UUID uuid) {
        return Pair.of("select * from User where uuid = ?", Map.of("1", uuid.toString()));
    }

    @Override
    public Pair<String, Map<String, Object>> selectUsersByUsername(String username) {
        return Pair.of("select * from User where username = ?", Map.of("1", username));
    }

    @Override
    public Pair<String, Map<String, Object>> insertUser(int createdBy, UUID uuid, String username) {
        return Pair.of("insert into User (uuid, username, createdBy) values (?, ?, ?)", Map.of(
                "1", uuid.toString(),
                "2", username,
                "3", createdBy
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateUser(int modifiedBy, int userId, UUID uuid, String username) {
        if (uuid == null) {
            return Pair.of("update User set username = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", username,
                    "2", modifiedBy,
                    "3", userId
            ));
        } else if (username == null) {
            return Pair.of("update User set uuid = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", uuid.toString(),
                    "2", modifiedBy,
                    "3", userId
            ));
        } else {
            return Pair.of("update User set uuid = ?, username = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", uuid.toString(),
                    "2", username,
                    "3", modifiedBy,
                    "4", userId
            ));
        }
    }

    @Override
    public Pair<String, Map<String, Object>> deleteUser(int deletedBy, int userId) {
        return Pair.of("update User set deleted = 1, modifiedBy = ? where id = ?", Map.of(
                "1", deletedBy,
                "2", userId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectWorlds() {
        return Pair.of("select * from World", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectWorldById(int worldId) {
        return Pair.of("select * from World where id = ?", Map.of("1", worldId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectWorldByUuid(UUID uuid) {
        return Pair.of("select * from World where uuid = ?", Map.of("1", uuid.toString()));
    }

    @Override
    public Pair<String, Map<String, Object>> insertWorld(int createdBy, UUID uuid, String worldName) {
        return Pair.of("insert into World (uuid, name, createdBy) values (?, ?, ?)", Map.of(
                "1", uuid.toString(),
                "2", worldName,
                "3", createdBy
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateWorld(int modifiedBy, int worldId, UUID uuid, String worldName) {
        if (uuid == null) {
            return Pair.of("update World set name = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", worldName,
                    "2", modifiedBy,
                    "3", worldId
            ));
        } else if (worldName == null) {
            return Pair.of("update World set uuid = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", uuid.toString(),
                    "2", modifiedBy,
                    "3", worldId
            ));
        } else {
            return Pair.of("update World set uuid = ?, name = ?, modifiedBy = ? where id = ?", Map.of(
                    "1", uuid.toString(),
                    "2", worldName,
                    "3", modifiedBy,
                    "4", worldId
            ));
        }
    }

    @Override
    public Pair<String, Map<String, Object>> deleteWorld(int deletedBy, int worldId) {
        return Pair.of("update World set deleted = 1, modifiedBy = ? where id = ?", Map.of(
                "1", deletedBy,
                "2", worldId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectPlots() {
        return Pair.of("select * from Plot", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectPlotById(int plotId) {
        return Pair.of("select * from Plot where id = ?", Map.of("1", plotId));
    }

    @Override
    public Pair<String, Map<String, Object>> insertPlot(int createdBy, int worldId, int x, int y, int z) {
        return Pair.of("insert into Plot (worldId, x, y, z, createdBy) values (?, ?, ?, ?, ?)", Map.of(
                "1", worldId,
                "2", x,
                "3", y,
                "4", z,
                "5", createdBy
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updatePlot(int modifiedBy, int plotId, Integer newWorldId, Integer newX, Integer newY, Integer newZ) {
        var queryBuilder = new StringBuilder("update Plot set modifiedBy = ?");
        var map = new HashMap<String, Object>();
        map.put("1", modifiedBy);
        var currentParam = 2;

        if (newWorldId != null) {
            queryBuilder.append(", worldId = ?");
            map.put(String.valueOf(currentParam), newWorldId);
            currentParam++;
        }

        if (newX != null) {
            queryBuilder.append(", x = ?");
            map.put(String.valueOf(currentParam), newX);
            currentParam++;
        }

        if (newY != null) {
            queryBuilder.append(", y = ?");
            map.put(String.valueOf(currentParam), newY);
            currentParam++;
        }

        if (newZ != null) {
            queryBuilder.append(", z = ?");
            map.put(String.valueOf(currentParam), newZ);
            currentParam++;
        }

        queryBuilder.append(" where id = ?");
        map.put(String.valueOf(currentParam), plotId);

        return Pair.of(queryBuilder.toString(), map);
    }

    @Override
    public Pair<String, Map<String, Object>> deletePlot(int deletedBy, int plotId) {
        return Pair.of("update Plot set deleted = 1, modifiedBy = ? where id = ?", Map.of(
                "1", deletedBy,
                "2", plotId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectPlotAttributesForPlot(int plotId) {
        return Pair.of("select * from PlotAttribute where plotId = ?", Map.of("1", plotId));
    }

    @Override
    public Pair<String, Map<String, Object>> selectPlotAttributeForPlot(int plotId, int attributeId) {
        return Pair.of("select * from PlotAttribute where plotId = ? and attributeId = ?", Map.of(
                "1", plotId,
                "2", attributeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> insertPlotAttribute(int createdBy, int plotId, int attributeId, String value) {
        return Pair.of("insert into PlotAttribute (plotId, attributeId, value, createdBy) values (?, ?, ?, ?)", Map.of(
                "1", plotId,
                "2", attributeId,
                "3", value,
                "4", createdBy
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updatePlotAttribute(int modifiedBy, int plotId, Integer attributeId, String value) {
        return Pair.of("update PlotAttribute set value = ?, modifiedBy = ? where plotId = ? and attributeId = ?", Map.of(
                "1", value,
                "2", modifiedBy,
                "3", plotId,
                "4", attributeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> deletePlotAttribute(int deletedBy, int plotId, int attributeId) {
        return Pair.of("update PlotAttribute set deleted = 1, modifiedBy = ? where plotId = ? and attributeId = ?", Map.of(
                "1", deletedBy,
                "2", plotId,
                "3", attributeId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> selectGroups() {
        return Pair.of("select * from Group", Map.of());
    }

    @Override
    public Pair<String, Map<String, Object>> selectGroupById(int plotGroupId) {
        return Pair.of("select * from Group where id = ?", Map.of("1", plotGroupId));
    }

    @Override
    public Pair<String, Map<String, Object>> insertGroup(int createdBy, String groupName) {
        return Pair.of("insert into Group (name, createdBy) values (?, ?)", Map.of(
                "1", groupName,
                "2", createdBy
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateGroup(int modifiedBy, int plotGroupId, String groupName) {
        return Pair.of("update Group set name = ?, modifiedBy = ? where id = ?", Map.of(
                "1", groupName,
                "2", modifiedBy,
                "3", plotGroupId
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> deleteGroup(int deletedBy, int plotGroupId) {
        return Pair.of("update Group set deleted = 1, modifiedBy = ? where id = ?", Map.of(
                "1", deletedBy,
                "2", plotGroupId
        ));
    }
}
