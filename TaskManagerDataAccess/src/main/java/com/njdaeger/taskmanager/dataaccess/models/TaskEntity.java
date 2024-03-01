package com.njdaeger.taskmanager.dataaccess.models;

import com.njdaeger.taskmanager.dataaccess.Identifiable;

/**
 * Represents a task in the database.
 * <p> </p>
 * <code><p>
 *     id int NOT NULL AUTO_INCREMENT,<br>
 *     typeId int NOT NULL,<br>
 *     parentId int,<br>
 *     projectId int NOT NULL,<br>
 *     taskKey int NOT NULL,<br>
 *     deleted bit NOT NULL DEFAULT 0,<br>
 *     created bigint NOT NULL DEFAULT unix_timestamp(),<br>
 *     createdBy int NOT NULL,<br>
 *     modified bigint NULL,<br>
 *     modifiedBy int NULL,<br>
 * </p></code>
 */
public class TaskEntity implements Identifiable {

    @Column
    private int id;
    @Column
    private int typeId;
    @Column
    private Integer parentId;
    @Column
    private int projectId;
    @Column
    private int taskKey;
    @Column
    private boolean deleted;
    @Column
    private int createdBy;
    @Column
    private Integer modifiedBy;
    @Column
    private long created;
    @Column
    private Long modified;

    public TaskEntity() {}

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getTypeId() {
        return typeId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public int getProjectId() {
        return projectId;
    }

    public int getTaskKey() {
        return taskKey;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public Integer getModifiedBy() {
        return modifiedBy;
    }

    public long getCreated() {
        return created;
    }

    public Long getModified() {
        return modified;
    }

}
