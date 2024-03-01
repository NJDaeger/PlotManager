package com.njdaeger.taskmanager.servicelibrary.models;

public class Project {

    private final int projectId;
    private final String projectName;
    private final String projectDescription;
    private final String projectPrefix;

    public Project(int projectId, String projectName, String projectDescription, String projectPrefix) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectPrefix = projectPrefix;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public String getProjectPrefix() {
        return projectPrefix;
    }

}
