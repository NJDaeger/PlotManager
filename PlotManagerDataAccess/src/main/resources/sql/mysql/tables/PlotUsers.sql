CREATE TABLE IF NOT EXISTS PlotUsers
(
    id int NOT NULL AUTO_INCREMENT,
    plotId int NOT NULL,
    userId int NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT PlotUsers_pk PRIMARY KEY (id),
    CONSTRAINT PlotUsers_plotId_userId_uc UNIQUE (plotId, userId),
    CONSTRAINT PlotUsers_plotId_fk FOREIGN KEY (plotId) REFERENCES Plot (id),
    CONSTRAINT PlotUsers_userId_fk FOREIGN KEY (userId) REFERENCES User (id),
    CONSTRAINT PlotUsers_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT PlotUsers_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)