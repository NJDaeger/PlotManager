package com.njdaeger.taskmanager.dataaccess.models;

import com.njdaeger.taskmanager.dataaccess.Identifiable;

/**
 * Represents a task type in the database.
 * <p> </p>
 * <code><p>
 *     id INT NOT NULL AUTO_INCREMENT,<br>
 *     name varchar(255) NOT NULL,<br>
 *     prefix varchar(4) NOT NULL,<br>
 *     deleted bit NOT NULL DEFAULT 0,<br>
 *     taskTypeVersion int NOT NULL DEFAULT 1,<br>
 *     created bigint NOT NULL DEFAULT unix_timestamp(),<br>
 *     createdBy int NOT NULL,<br>
 *     modified bigint NULL,<br>
 *     modifiedBy int NULL,<br>
 * </p></code>
 */
public class TaskTypeEntity implements Identifiable {

    @Column
    private int id;

    @Column
    private String name;

    @Column
    private String prefix;

    @Column
    private boolean deleted;

    @Column
    private int taskTypeVersion;

    @Column
    private int createdBy;

    @Column
    private Integer modifiedBy;

    @Column
    private long created;

    @Column
    private Long modified;

    public TaskTypeEntity() {}

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getTaskTypeVersion() {
        return taskTypeVersion;
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
