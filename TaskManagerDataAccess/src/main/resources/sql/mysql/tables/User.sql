CREATE TABLE IF NOT EXISTS TaskManager.User
(
    id int NOT NULL AUTO_INCREMENT,
    uuid char(36) NOT NULL,
    username varchar(16) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT User_pk PRIMARY KEY (id),
    CONSTRAINT User_uuid_uq UNIQUE (uuid),
    CONSTRAINT User_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
);
