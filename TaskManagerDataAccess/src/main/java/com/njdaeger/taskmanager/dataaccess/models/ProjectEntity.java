package com.njdaeger.taskmanager.dataaccess.models;

import com.njdaeger.taskmanager.dataaccess.Identifiable;

/**
 * Represents a project in the database.
 * <p> </p>
 * <code><p>
 *     id INT NOT NULL AUTO_INCREMENT,<br>
 *     name VARCHAR(255) NOT NULL,<br>
 *     description VARCHAR(255) NOT NULL,<br>
 *     prefix varchar(4) NOT NULL,<br>
 *     deleted bit NOT NULL DEFAULT 0,<br>
 *     created bigint NOT NULL DEFAULT unix_timestamp(),<br>
 *     createdBy int NOT NULL,<br>
 *     modified bigint NULL,<br>
 *     modifiedBy int NULL,<br>
 * </p></code>
 */
public class ProjectEntity implements Identifiable {

    @Column
    private int id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String prefix;

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

    public ProjectEntity() {}

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

    public String getDescription() {
        return description;
    }

    public String getPrefix() {
        return prefix;
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
