CREATE TABLE IF NOT EXISTS PlotGroups
(
    plotGroupId int NOT NULL,
    plotId int NOT NULL,
    created bigint NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified bigint NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotGroups_plotGroupId_fk FOREIGN KEY (plotGroupId) REFERENCES PlotGroup (id),
    CONSTRAINT PlotGroups_plotId_fk FOREIGN KEY (plotId) REFERENCES Plot (id),
    CONSTRAINT PlotGroups_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)