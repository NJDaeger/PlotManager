package com.njdaeger.taskmanager.plugin.redblock;

import com.njdaeger.pdk.config.ConfigType;
import com.njdaeger.pdk.config.Configuration;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RedblockConfig extends Configuration {

    private List<String> redblockProjects;
    private Map<UUID, String> worldRedblockProjectDefaults;

    public RedblockConfig(Plugin plugin) {
        super(plugin, ConfigType.YML, "redblock-config");

        redblockProjects = getStringList("redblock-projects");
        /*

        redblock-projects:
        - GF
        - AX
        - HRK

        redblock-world-default-redblock-project:
        - world:GF
        - flat_world:AX
        - harkenberg:HRK

         */
    }

    public List<String> getRedblockProjects() {
        return redblockProjects;
    }

    public String getDefualtProject() {
        return redblockProjects.get(0);
    }

    public void addProject(String project) {
        redblockProjects.add(project);
        save();
    }

    public void removeProject(String project) {
        redblockProjects.remove(project);
        save();
    }

}
