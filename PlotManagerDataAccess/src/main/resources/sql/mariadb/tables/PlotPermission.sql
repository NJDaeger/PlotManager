CREATE TABLE IF NOT EXISTS PlotPermission
(
    permissionGroupId int NOT NULL,
    plotId int NOT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotPermission_permissionGroupId_fk FOREIGN KEY (permissionGroupId) REFERENCES PermissionGroup (id),
    CONSTRAINT PlotPermission_plotId_fk FOREIGN KEY (plotId) REFERENCES Plot (id),
    CONSTRAINT PlotPermission_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)