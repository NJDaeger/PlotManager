-- a project is a collection of tasks
CREATE TABLE IF NOT EXISTS TaskManager.ProjectTasks
(
    id INT NOT NULL AUTO_INCREMENT,
    projectId INT NOT NULL,
    taskId INT NOT NULL,
    taskKey int generated always as (ROW_NUMBER() over (PARTITION BY projectId ORDER BY taskId)) STORED,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT ProjectTasks_pk PRIMARY KEY (id),
    CONSTRAINT ProjectTasks_Projects_fk FOREIGN KEY (projectId) REFERENCES Project (id),
    CONSTRAINT ProjectTasks_Tasks_fk FOREIGN KEY (taskId) REFERENCES Task (id),
    CONSTRAINT ProjectTasks_Users_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT ProjectTasks_Users_fk_2 FOREIGN KEY (modifiedBy) REFERENCES User (id)
);