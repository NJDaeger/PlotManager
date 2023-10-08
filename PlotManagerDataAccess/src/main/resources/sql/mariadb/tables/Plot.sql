CREATE TABLE IF NOT EXISTS Plot
(
    id int NOT NULL AUTO_INCREMENT,
    worldId int NOT NULL,
    x int NOT NULL,
    y int NOT NULL,
    z int NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    parentId int,
    created bigint NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified bigint NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    modifiedBy int NOT NULL,
    CONSTRAINT Plot_pk PRIMARY KEY (id),
    CONSTRAINT Plot_worldId_fk FOREIGN KEY (worldId) REFERENCES PlotWorld (id),
    CONSTRAINT Plot_parentId_fk FOREIGN KEY (parentId) REFERENCES Plot (id),
    CONSTRAINT Plot_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT Plot_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)