-- list of projects
CREATE TABLE IF NOT EXISTS TaskManager.Project
(
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    prefix varchar(4) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT Project_pk PRIMARY KEY (id),
    CONSTRAINT Project_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT Project_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id),
    CONSTRAINT Project_prefix_uindex UNIQUE (prefix),
    CONSTRAINT Project_name_uindex UNIQUE (name)
)