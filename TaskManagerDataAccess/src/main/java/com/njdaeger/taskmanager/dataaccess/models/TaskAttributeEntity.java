package com.njdaeger.taskmanager.dataaccess.models;

import com.njdaeger.taskmanager.dataaccess.Identifiable;

/**
 * Represents a task attribute in the database.
 * <p> </p>
 * <code><p>
 *     id INT NOT NULL AUTO_INCREMENT,
 *     taskId int not null,
 *     taskTypeAttributeId int not null,
 *     valueJson longtext not null,
 *     created bigint NOT NULL DEFAULT unix_timestamp(),
 *     createdBy int NOT NULL,
 *     modified bigint NULL,
 *     modifiedBy int NULL,
 * </p></code>
 */
public class TaskAttributeEntity implements Identifiable {

    @Column
    private int id;
    @Column
    private int taskId;
    @Column
    private int attributeId;
    @Column
    private String value;
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

    public TaskAttributeEntity() {}

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
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
