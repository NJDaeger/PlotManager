package com.njdaeger.plotmanager.dataaccess;

import com.njdaeger.pdk.utils.Pair;

import java.util.Map;
import java.util.UUID;

public interface IProcedure {

    Pair<String, Map<String, Object>> insertAttribute(int createdBy, String name, String type);

    Pair<String, Map<String, Object>> selectAttributeById(int attributeId);

    Pair<String, Map<String, Object>> selectAttributeByName(String attributeName);

    Pair<String, Map<String, Object>> selectAttributes();

    Pair<String, Map<String, Object>> updateAttribute(int modifiedBy, int attributeId, String name, String type);

    Pair<String, Map<String, Object>> deleteAttribute(int deletedBy, int attributeId);

    Pair<String, Map<String, Object>> selectUsers();

    Pair<String, Map<String, Object>> selectUserById(int userId);

    Pair<String, Map<String, Object>> selectUserByUuid(UUID uuid);

    Pair<String, Map<String, Object>> selectUsersByUsername(String username);

    Pair<String, Map<String, Object>> insertUser(int createdBy, UUID uuid, String username);

    Pair<String, Map<String, Object>> updateUser(int modifiedBy, int userId, UUID uuid, String username);

    Pair<String, Map<String, Object>> deleteUser(int deletedBy, int userId);

}
