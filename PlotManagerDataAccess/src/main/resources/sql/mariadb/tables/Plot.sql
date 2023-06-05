CREATE TABLE IF NOT EXISTS Plot
(
    id int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    worldId int NOT NULL,
    x int NOT NULL,
    y int NOT NULL,
    z int NOT NULL,
    statusId int NOT NULL,
    parentId int,
    plotTypeId int,
    description varchar(4096),
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT Plot_pk PRIMARY KEY (id),
    CONSTRAINT Plot_worldId_fk FOREIGN KEY (worldId) REFERENCES PlotWorld (id),
    CONSTRAINT Plot_statusId_fk FOREIGN KEY (statusId) REFERENCES PlotStatus (id),
    CONSTRAINT Plot_parentId_fk FOREIGN KEY (parentId) REFERENCES Plot (id),
    CONSTRAINT Plot_plotTypeId_fk FOREIGN KEY (plotTypeId) REFERENCES PlotType (id),
    CONSTRAINT Plot_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)