CREATE TABLE IF NOT EXISTS UserRole
(
    id int NOT NULL AUTO_INCREMENT,
    role varchar(255) NOT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT UserRole_pk PRIMARY KEY (id),
    CONSTRAINT UserRole_role_uc UNIQUE (role),
    CONSTRAINT UserRole_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id)
)