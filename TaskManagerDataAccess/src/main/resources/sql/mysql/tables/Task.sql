CREATE TABLE IF NOT EXISTS TaskManager.Task
(
    id int NOT NULL AUTO_INCREMENT,
    typeId int NOT NULL,
    parentId int,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT Task_pk PRIMARY KEY (id),
    CONSTRAINT Task_parentId_fk FOREIGN KEY (parentId) REFERENCES Task (id),
    CONSTRAINT Task_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT Task_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)