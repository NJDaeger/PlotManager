CREATE TABLE IF NOT EXISTS PermissionGroup
(
    id int NOT NULL AUTO_INCREMENT,
    permission varchar(255) NOT NULL,
    niceName varchar(64) NOT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PermissionGroup_pk PRIMARY KEY (id),
    CONSTRAINT PermissionGroup_permission_uq UNIQUE (permission),
    CONSTRAINT PermissionGroup_niceName_uq UNIQUE (niceName),
    CONSTRAINT PermissionGroup_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)