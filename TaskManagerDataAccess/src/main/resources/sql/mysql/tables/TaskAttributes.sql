-- a table that lists the values of task type attributes for a task
CREATE TABLE IF NOT EXISTS TaskManager.TaskAttributes
(
    id INT NOT NULL AUTO_INCREMENT,
    taskId int not null,
    taskTypeAttributeId int not null,
    valueJson longtext not null,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT TaskAttributes_PK PRIMARY KEY (id),
    CONSTRAINT TaskAttributes_FK FOREIGN KEY (taskId) REFERENCES Task(id),
    CONSTRAINT TaskAttributes_FK2 FOREIGN KEY (taskTypeAttributeId) REFERENCES TaskTypeAttribute(id),
    CONSTRAINT TaskAttributes_FK3 FOREIGN KEY (createdBy) REFERENCES User(id),
    CONSTRAINT TaskAttributes_FK4 FOREIGN KEY (modifiedBy) REFERENCES User(id)
)