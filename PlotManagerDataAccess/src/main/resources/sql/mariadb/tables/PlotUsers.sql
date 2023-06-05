CREATE TABLE IF NOT EXISTS PlotUsers
(
    plotId int NOT NULL,
    userId int NOT NULL,
    userRole int NOT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotUsers_plotId_userId_uc UNIQUE (plotId, userId),
    CONSTRAINT PlotUsers_plotId_fk FOREIGN KEY (plotId) REFERENCES Plot (id),
    CONSTRAINT PlotUsers_userId_fk FOREIGN KEY (userId) REFERENCES User (id),
    CONSTRAINT PlotUsers_userRole_fk FOREIGN KEY (userRole) REFERENCES UserRole (id),
    CONSTRAINT PlotUsers_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)