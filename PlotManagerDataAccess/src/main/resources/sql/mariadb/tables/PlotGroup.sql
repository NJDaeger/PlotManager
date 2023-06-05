CREATE TABLE IF NOT EXISTS PlotGroup
(
    id int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotGroup_pk PRIMARY KEY (id),
    CONSTRAINT PlotGroup_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)