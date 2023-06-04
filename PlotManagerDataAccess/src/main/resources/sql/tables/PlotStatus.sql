CREATE TABLE IF NOT EXISTS PlotStatus
(
    id int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    color char(8),
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotStatus_pk PRIMARY KEY (id),
    CONSTRAINT PlotStatus_name_uq UNIQUE (name)
)