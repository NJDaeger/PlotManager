CREATE TABLE IF NOT EXISTS PlotGroupAttribute
(
    id int NOT NULL AUTO_INCREMENT,
    groupId int NOT NULL,
    attributeId int NOT NULL,
    value nvarchar(4000) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT PlotGroupAttribute_pk PRIMARY KEY (id),
    CONSTRAINT PlotGroupAttribute_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT PlotGroupAttribute_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id),
    CONSTRAINT PlotGroupAttribute_attributeId_fk FOREIGN KEY (attributeId) REFERENCES Attribute (id),
    CONSTRAINT PlotGroupAttribute_plotId_fk FOREIGN KEY (groupId) REFERENCES `Group` (id),
    CONSTRAINT PlotGroupAttribute_plotId_attributeId_uc UNIQUE (groupId, attributeId)
)