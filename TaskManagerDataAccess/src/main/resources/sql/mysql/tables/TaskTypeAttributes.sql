-- a table that defines the types of attributes that can be associated with a task type
CREATE TABLE IF NOT EXISTS TaskManager.TaskTypeAttributes
(
    id INT NOT NULl AUTO_INCREMENT,
    taskTypeId INT NOT NULL,
    attributeId INT NOT NULL,
    attributeConfigJson longtext NOT NULL,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT TaskTypeAttribute_PK PRIMARY KEY (id),
    CONSTRAINT TaskTypeAttribute_UQ UNIQUE (taskTypeId, attributeId),
    CONSTRAINT TaskTypeAttribute_FK_TaskType FOREIGN KEY (taskTypeId) REFERENCES TaskType(id),
    CONSTRAINT TaskTypeAttribute_FK_Attribute FOREIGN KEY (attributeId) REFERENCES Attribute(id),
    CONSTRAINT TaskTypeAttribute_FK_CreatedBy FOREIGN KEY (createdBy) REFERENCES User(id),
    CONSTRAINT TaskTypeAttribute_FK_ModifiedBy FOREIGN KEY (modifiedBy) REFERENCES User(id)
)