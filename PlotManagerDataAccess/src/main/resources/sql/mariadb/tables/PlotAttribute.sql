CREATE TABLE IF NOT EXISTS PlotAttribute
(
    attributeId int NOT NULL,
    value varchar(4096) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified bigint NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    modifiedBy int NOT NULL,
    CONSTRAINT PlotAttribute_attributeId_fk UNIQUE (attributeId),
    CONSTRAINT PlotAttribute_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT PlotAttribute_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)