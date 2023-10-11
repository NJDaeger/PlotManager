CREATE TABLE IF NOT EXISTS Plot
(
    id int NOT NULL AUTO_INCREMENT,
    worldId int NOT NULL,
    x int NOT NULL,
    y int NOT NULL,
    z int NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    parentId int,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT Plot_pk PRIMARY KEY (id),
    CONSTRAINT Plot_worldId_fk FOREIGN KEY (worldId) REFERENCES World (id),
    CONSTRAINT Plot_parentId_fk FOREIGN KEY (parentId) REFERENCES Plot (id),
    CONSTRAINT Plot_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT Plot_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)