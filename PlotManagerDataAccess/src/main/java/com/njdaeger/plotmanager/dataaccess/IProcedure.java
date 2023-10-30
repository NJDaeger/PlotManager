package com.njdaeger.plotmanager.dataaccess;

import com.njdaeger.pdk.utils.Pair;

import java.util.Map;
import java.util.UUID;

public interface IProcedure {

    //region Attribute Procedures

    Pair<String, Map<String, Object>> insertAttribute(int createdBy, String name, String type);

    Pair<String, Map<String, Object>> selectAttributeById(int attributeId);

    Pair<String, Map<String, Object>> selectAttributeByName(String attributeName);

    Pair<String, Map<String, Object>> selectAttributes();

    Pair<String, Map<String, Object>> updateAttribute(int modifiedBy, int attributeId, String name, String type);

    Pair<String, Map<String, Object>> deleteAttribute(int deletedBy, int attributeId);

    //endregion

    //region User Procedures

    Pair<String, Map<String, Object>> insertSystemUser();

    Pair<String, Map<String, Object>> selectUsers();

    Pair<String, Map<String, Object>> selectUserById(int userId);

    Pair<String, Map<String, Object>> selectUserByUuid(UUID uuid);

    Pair<String, Map<String, Object>> selectUsersByUsername(String username);

    Pair<String, Map<String, Object>> insertUser(int createdBy, UUID uuid, String username);

    Pair<String, Map<String, Object>> updateUser(int modifiedBy, int userId, UUID uuid, String username);

    Pair<String, Map<String, Object>> deleteUser(int deletedBy, int userId);

    //endregion

    //region World Procedures

    Pair<String, Map<String, Object>> selectWorlds();

    Pair<String, Map<String, Object>> selectWorldById(int worldId);

    Pair<String, Map<String, Object>> selectWorldByUuid(UUID uuid);

    Pair<String, Map<String, Object>> insertWorld(int createdBy, UUID uuid, String worldName);

    Pair<String, Map<String, Object>> updateWorld(int modifiedBy, int worldId, UUID uuid, String worldName);

    Pair<String, Map<String, Object>> deleteWorld(int deletedBy, int worldId);

    //endregion

    //region Plot Procedures

    Pair<String, Map<String, Object>> selectPlots();

    Pair<String, Map<String, Object>> selectPlotById(int plotId);

    Pair<String, Map<String, Object>> insertPlot(int createdBy, int worldId, int x, int y, int z);

    Pair<String, Map<String, Object>> updatePlotLocation(int modifiedBy, int plotId, Integer newWorldId, Integer newX, Integer newY, Integer newZ);

    Pair<String, Map<String, Object>> updatePlotParent(int modifiedBy, int plotId, Integer newParentId);

    Pair<String, Map<String, Object>> updatePlotGroup(int modifiedBy, int plotId, Integer newGroupId);

    Pair<String, Map<String, Object>> insertPlotUser(int insertedBy, int plotId, int userId);

    Pair<String, Map<String, Object>> restorePlotUser(int restoredBy, int plotId, int userId);

    Pair<String, Map<String, Object>> selectPlotUser(int plotId, int userId);

    Pair<String, Map<String, Object>> selectPlotUserById(int plotUserId);

    Pair<String, Map<String, Object>> selectPlotUsersForPlot(int plotId);

    Pair<String, Map<String, Object>> deletePlotUser(int deletedBy, int plotId, int userId);

    Pair<String, Map<String, Object>> deletePlot(int deletedBy, int plotId);

    Pair<String, Map<String, Object>> selectPlotAttributesForPlot(int plotId);

    Pair<String, Map<String, Object>> selectPlotAttributeForPlotByAttributeId(int plotId, int attributeId);

    Pair<String, Map<String, Object>> selectPlotAttributeForPlotById(int plotAttributeId);

    Pair<String, Map<String, Object>> insertPlotAttribute(int createdBy, int plotId, int attributeId, String value);

    Pair<String, Map<String, Object>> updatePlotAttribute(int modifiedBy, int plotId, Integer attributeId, String value);

    Pair<String, Map<String, Object>> deletePlotAttribute(int deletedBy, int plotId, int attributeId);

    //endregion

    //region Group Procedures

    Pair<String, Map<String, Object>> selectGroups();

    Pair<String, Map<String, Object>> selectGroupById(int plotGroupId);

    Pair<String, Map<String, Object>> insertGroup(int createdBy, String groupName);

    Pair<String, Map<String, Object>> updateGroup(int modifiedBy, int plotGroupId, String groupName);

    Pair<String, Map<String, Object>> deleteGroup(int deletedBy, int plotGroupId);

    //endregion

}
