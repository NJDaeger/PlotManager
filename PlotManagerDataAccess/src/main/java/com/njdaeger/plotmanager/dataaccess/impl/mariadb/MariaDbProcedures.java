package com.njdaeger.plotmanager.dataaccess.impl.mariadb;

import com.google.common.base.Verify;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.plotmanager.dataaccess.IProcedure;

import java.util.Map;
import java.util.UUID;

public class MariaDbProcedures implements IProcedure {

    public MariaDbProcedures() {
    }

    @Override
    public Pair<String, Map<String, Object>> insertAttribute(int createdBy, String name, String type) {
        Verify.verify(name != null && type != null, "Neither name nor type can be null.");
        return Pair.of("insert into Attribute (name, type, createdBy, modifiedBy) values (?, ?, ?, ?)", Map.of(
                "1", name,
                "2", type,
                "3", createdBy,
                "4", createdBy
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
        Verify.verify(name != null || type != null, "Name and type cannot both be null.");
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
        Verify.verify(uuid != null && username != null, "Uuid and username cannot both be null.");
        return Pair.of("insert into User (uuid, username, createdBy, modifiedBy) values (?, ?, ?, ?)", Map.of(
                "1", uuid.toString(),
                "2", username,
                "3", createdBy,
                "4", createdBy
        ));
    }

    @Override
    public Pair<String, Map<String, Object>> updateUser(int modifiedBy, int userId, UUID uuid, String username) {
        Verify.verify(uuid != null || username != null, "Uuid and username cannot both be null.");
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
}
