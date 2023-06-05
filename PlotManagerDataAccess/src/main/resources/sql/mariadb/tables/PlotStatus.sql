CREATE TABLE IF NOT EXISTS PlotStatus
(
    id int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    color char(8),
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotStatus_pk PRIMARY KEY (id),
    CONSTRAINT PlotStatus_name_uq UNIQUE (name),
    CONSTRAINT PlotStatus_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)