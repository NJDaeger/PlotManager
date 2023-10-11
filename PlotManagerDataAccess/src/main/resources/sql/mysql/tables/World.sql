CREATE TABLE IF NOT EXISTS World
(
    id int NOT NULL AUTO_INCREMENT,
    uuid char(36) NOT NULL,
    name varchar(255) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT PlotWorld_pk PRIMARY KEY (id),
    CONSTRAINT PlotWorld_uuid_uq UNIQUE (uuid),
    CONSTRAINT PlotWorld_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT PlotWorld_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)