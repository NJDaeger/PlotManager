CREATE TABLE IF NOT EXISTS PlotAttribute
(
    id int NOT NULL AUTO_INCREMENT,
    plotId int NOT NULL,
    attributeId int NOT NULL,
    value varchar(4096) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT PlotAttribute_pk PRIMARY KEY (id),
    CONSTRAINT PlotAttribute_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT PlotAttribute_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id),
    CONSTRAINT PlotAttribute_plotId_fk FOREIGN KEY (plotId) REFERENCES Plot (id),
    CONSTRAINT PlotAttribute_plotId_attributeId_uc UNIQUE (plotId, attributeId)
)