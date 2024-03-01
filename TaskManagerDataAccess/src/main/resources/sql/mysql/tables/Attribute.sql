-- types of attributes
CREATE TABLE IF NOT EXISTS TaskManager.Attribute
(
    id int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    validationConfigJson longtext NOT NULL,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT Attribute_pk PRIMARY KEY (id),
    CONSTRAINT Attribute_name_uq UNIQUE (name),
    CONSTRAINT Attribute_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT Attribute_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES User (id)
)