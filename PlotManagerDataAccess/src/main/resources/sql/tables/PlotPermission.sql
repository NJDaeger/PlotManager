CREATE TABLE IF NOT EXISTS PlotPermission
(
    id int NOT NULL AUTO_INCREMENT,
    permission varchar(255) NOT NULL,
    niceName varchar(64) NOT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotPermission_pk PRIMARY KEY (id),
    CONSTRAINT PlotPermission_permission_uq UNIQUE (permission),
    CONSTRAINT PlotPermission_niceName_uq UNIQUE (niceName)
)