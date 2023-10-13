CREATE TABLE IF NOT EXISTS `Group`
(
    id int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT Group_pk PRIMARY KEY (id),
    CONSTRAINT Group_name_uc UNIQUE (name),
    CONSTRAINT Group_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT Group_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)