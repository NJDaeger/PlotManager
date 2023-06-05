CREATE TABLE IF NOT EXISTS PlotWorld
(
    id int NOT NULL AUTO_INCREMENT,
    uuid char(36) NOT NULL,
    name varchar(255) NOT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotWorld_pk PRIMARY KEY (id),
    CONSTRAINT PlotWorld_uuid_uq UNIQUE (uuid),
    CONSTRAINT PlotWorld_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)