-- a table of different types of tasks
CREATE TABLE IF NOT EXISTS TaskManager.TaskType
(
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    prefix varchar(4) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    taskTypeVersion int NOT NULL DEFAULT 1,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT TaskType_pk PRIMARY KEY (id),
    CONSTRAINT TaskType_name_uindex UNIQUE (name),
    CONSTRAINT TaskType_prefix_uindex UNIQUE (prefix),
    CONSTRAINT TaskType_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT TaskType_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
);