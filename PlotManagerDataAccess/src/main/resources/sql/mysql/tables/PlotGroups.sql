CREATE TABLE IF NOT EXISTS PlotGroups
(
    plotId int NOT NULL,
    groupId int NOT NULL,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT PlotGroups_plotId_uc UNIQUE (plotId),
    CONSTRAINT PlotGroups_plotGroupId_fk FOREIGN KEY (groupId) REFERENCES `Group` (id),
    CONSTRAINT PlotGroups_plotId_fk FOREIGN KEY (plotId) REFERENCES Plot (id),
    CONSTRAINT PlotGroups_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT PlotGroups_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)