DELIMITER //

CREATE TABLE IF NOT EXISTS TaskManager.SchemaVersion
(
    version int NOT NULL
)//

-- users
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
    CONSTRAINT User_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES TaskManager.User (id)
)//

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
    CONSTRAINT Project_createdBy_fk FOREIGN KEY (createdBy) REFERENCES TaskManager.User (id),
    CONSTRAINT Project_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES TaskManager.User (id),
    CONSTRAINT Project_prefix_uindex UNIQUE (prefix),
    CONSTRAINT Project_name_uindex UNIQUE (name)
)//

-- a table of different types of tasks
CREATE TABLE IF NOT EXISTS TaskManager.TaskType
(
    id INT NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    prefix varchar(4) NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    taskTypeVersion int NOT NULL DEFAULT 1,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT TaskType_pk PRIMARY KEY (id),
    CONSTRAINT TaskType_name_uindex UNIQUE (name),
    CONSTRAINT TaskType_prefix_uindex UNIQUE (prefix),
    CONSTRAINT TaskType_createdBy_fk FOREIGN KEY (createdBy) REFERENCES TaskManager.User (id),
    CONSTRAINT TaskType_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES TaskManager.User (id)
)//

-- types of attributes
CREATE TABLE IF NOT EXISTS TaskManager.Attribute
(
    id int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    taskTypeId int NOT NULL,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT Attribute_pk PRIMARY KEY (id),
    CONSTRAINT Attribute_name_uq UNIQUE (name, taskTypeId),
    CONSTRAINT Attribute_taskTypeId_fk FOREIGN KEY (taskTypeId) REFERENCES TaskManager.TaskType (id),
    CONSTRAINT Attribute_createdBy_fk FOREIGN KEY (createdBy) REFERENCES TaskManager.User (id),
    CONSTRAINT Attribute_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES TaskManager.User (id)
)//

-- tasks
CREATE TABLE IF NOT EXISTS TaskManager.Task
(
    id int NOT NULL AUTO_INCREMENT,
    typeId int NOT NULL,
    parentId int,
    projectId int NOT NULL,
    taskKey int NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT Task_pk PRIMARY KEY (id),
    CONSTRAINT Task_parentId_fk FOREIGN KEY (parentId) REFERENCES TaskManager.Task (id),
    CONSTRAINT Task_createdBy_fk FOREIGN KEY (createdBy) REFERENCES TaskManager.User (id),
    CONSTRAINT Task_modifiedBy_fk FOREIGN KEY (modifiedBy) REFERENCES TaskManager.User (id),
    CONSTRAINT Task_typeId_fk FOREIGN KEY (typeId) REFERENCES TaskManager.TaskType (id),
    CONSTRAINT Task_project_fk FOREIGN KEY (projectId) REFERENCES TaskManager.Project (id),
    CONSTRAINT TASK_taskKey_uq UNIQUE (projectId, typeId, taskKey)
)//

-- a table that lists the values of the attributes for each task
CREATE TABLE IF NOT EXISTS TaskManager.TaskAttributes
(
    id INT NOT NULL AUTO_INCREMENT,
    taskId INT NOT NULL,
    attributeId INT NOT NULL,
    value longtext NOT NULL,
    deleted bit NOT NULL DEFAULT 0,
    created bigint NOT NULL DEFAULT unix_timestamp(),
    createdBy int NOT NULL,
    modified bigint NULL,
    modifiedBy int NULL,
    CONSTRAINT TaskAttributes_pk PRIMARY KEY (id),
    CONSTRAINT TaskAttributes_Tasks_fk FOREIGN KEY (taskId) REFERENCES TaskManager.Task (id),
    CONSTRAINT TaskAttributes_Attributes_fk FOREIGN KEY (attributeId) REFERENCES TaskManager.Attribute (id),
    CONSTRAINT TaskAttributes_Users_fk FOREIGN KEY (createdBy) REFERENCES TaskManager.User (id),
    CONSTRAINT TaskAttributes_Users_fk_2 FOREIGN KEY (modifiedBy) REFERENCES TaskManager.User (id)
)//

-- trigger setup
-- create a trigger to update the modified date when a row is updated
CREATE OR REPLACE TRIGGER TaskManager_User_Update
    BEFORE UPDATE ON TaskManager.User
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END//

CREATE OR REPLACE TRIGGER TaskManager_Project_Update
    BEFORE UPDATE ON TaskManager.Project
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END//

CREATE OR REPLACE TRIGGER TaskManager_TaskType_Update
    BEFORE UPDATE ON TaskManager.TaskType
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END//

CREATE OR REPLACE TRIGGER TaskManager_Attribute_Update
    BEFORE UPDATE ON TaskManager.Attribute
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END//

CREATE OR REPLACE TRIGGER TaskManager_Task_Update
    BEFORE UPDATE ON TaskManager.Task
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END//

CREATE OR REPLACE TRIGGER TaskManager_TaskAttributes_Update
    BEFORE UPDATE ON TaskManager.TaskAttributes
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END//

CREATE OR REPLACE TRIGGER TaskManager_Generate_TaskKey
    BEFORE INSERT ON TaskManager.Task
    FOR EACH ROW
BEGIN
    DECLARE taskKey INT;

    SELECT COALESCE(MAX(taskKey), 0) + 1
    INTO taskKey
    FROM TaskManager.Task
    WHERE projectId = NEW.projectId AND typeId = NEW.typeId;

    SET NEW.taskKey = taskKey;
END//

-- --------------------------------------------------------------------------------
-- PROCEDURES
-- --------------------------------------------------------------------------------


-- ATTRIBUTE PROCEDURES

-- select all attributes

CREATE OR REPLACE PROCEDURE TaskManager.Select_Attributes()
BEGIN
    SELECT *
    FROM TaskManager.Attribute;
END//

-- select attributes by task type
CREATE OR REPLACE PROCEDURE TaskManager.Select_AttributesByTaskType(IN _taskTypeId INT)
BEGIN
    SELECT *
    FROM TaskManager.Attribute
    WHERE TaskManager.Attribute.taskTypeId = _taskTypeId;
END//

-- select attribute by name
CREATE OR REPLACE PROCEDURE TaskManager.Select_AttributeByName(IN _name VARCHAR(255), IN _taskTypeId INT)
BEGIN
    SELECT *
    FROM TaskManager.Attribute
    WHERE TaskManager.Attribute.name = _name AND TaskManager.Attribute.taskTypeId = _taskTypeId;
END//

-- select attribute by id
CREATE OR REPLACE PROCEDURE TaskManager.Select_AttributeById(IN _id INT)
BEGIN
    SELECT *
    FROM TaskManager.Attribute
    WHERE TaskManager.Attribute.id = _id;
END//

-- insert attribute
CREATE OR REPLACE PROCEDURE TaskManager.Insert_Attribute(IN _createdBy INT, IN _taskTypeId INT, IN _name VARCHAR(255))
BEGIN
    INSERT INTO TaskManager.Attribute (createdBy, name, taskTypeId)
    VALUES (_createdBy, _name, _taskTypeId);
END//

-- update attribute
CREATE OR REPLACE PROCEDURE TaskManager.Update_Attribute(IN _modifiedBy INT, IN _attributeId INT, IN _taskTypeId INT, IN _name VARCHAR(255))
BEGIN
    UPDATE TaskManager.Attribute
    SET
        modifiedBy = _modifiedBy,
        name = COALESCE(_name, name),
        taskTypeId = COALESCE(_taskTypeId, taskTypeId)
    WHERE TaskManager.Attribute.id = _attributeId;
END//

-- PROJECT PROCEDURES

-- select all projects
CREATE OR REPLACE PROCEDURE TaskManager.Select_Projects()
BEGIN
    SELECT *
    FROM TaskManager.Project;
END//

-- select project by id
CREATE OR REPLACE PROCEDURE TaskManager.Select_ProjectById(IN _id INT)
BEGIN
    SELECT *
    FROM TaskManager.Project
    WHERE TaskManager.Project.id = _id;
END//

-- select project by name
CREATE OR REPLACE PROCEDURE TaskManager.Select_ProjectByName(IN _name VARCHAR(255))
BEGIN
    SELECT *
    FROM TaskManager.Project
    WHERE TaskManager.Project.name = _name;
END//

-- select project by prefix
CREATE OR REPLACE PROCEDURE TaskManager.Select_ProjectByPrefix(IN _prefix VARCHAR(4))
BEGIN
    SELECT *
    FROM TaskManager.Project
    WHERE TaskManager.Project.prefix = _prefix;
END//

-- insert project
CREATE OR REPLACE PROCEDURE TaskManager.Insert_Project(IN _createdBy INT, IN _name VARCHAR(255), IN _description VARCHAR(255), IN _prefix VARCHAR(4))
BEGIN
    INSERT INTO TaskManager.Project (createdBy, name, description, prefix)
    VALUES (_createdBy, _name, _description, _prefix);
END//

-- update project. projectName, projectDescription, projectPrefix are all optional. if they are not provided, the value will not be updated
CREATE OR REPLACE PROCEDURE TaskManager.Update_Project(IN _modifiedBy INT, IN _projectId INT, IN _projectName VARCHAR(255), IN _projectDescription VARCHAR(255), IN _projectPrefix VARCHAR(4))
BEGIN
    UPDATE TaskManager.Project
    SET
        modifiedBy = _modifiedBy,
        name = COALESCE(_projectName, name),
        description = COALESCE(_projectDescription, description),
        prefix = COALESCE(_projectPrefix, prefix)
    WHERE TaskManager.Project.id = _projectId;
END//

-- delete project
CREATE OR REPLACE PROCEDURE TaskManager.Delete_Project(IN _deletedBy INT, IN _projectId INT)
BEGIN
    UPDATE TaskManager.Project
    SET
        deleted = 1,
        modifiedBy = _deletedBy
    WHERE TaskManager.Project.id = _projectId;
END//

-- restore project
CREATE OR REPLACE PROCEDURE TaskManager.Restore_Project(IN _restoredBy INT, IN _projectId INT)
BEGIN
    UPDATE TaskManager.Project
    SET
        deleted = 0,
        modifiedBy = _restoredBy
    WHERE TaskManager.Project.id = _projectId;
END//


-- TASKTYPE PROCEDURES

-- select all task types
CREATE OR REPLACE PROCEDURE TaskManager.Select_TaskTypes()
BEGIN
    SELECT *
    FROM TaskManager.TaskType;
END//

-- select task type by id
CREATE OR REPLACE PROCEDURE TaskManager.Select_TaskTypeById(IN _id INT)
BEGIN
    SELECT *
    FROM TaskManager.TaskType
    WHERE TaskManager.TaskType.id = _id;
END//

-- select task type by name
CREATE OR REPLACE PROCEDURE TaskManager.Select_TaskTypeByName(IN _name VARCHAR(255))
BEGIN
    SELECT *
    FROM TaskManager.TaskType
    WHERE TaskManager.TaskType.name = _name;
END//

-- select task type by prefix
CREATE OR REPLACE PROCEDURE TaskManager.Select_TaskTypeByPrefix(IN _prefix VARCHAR(4))
BEGIN
    SELECT *
    FROM TaskManager.TaskType
    WHERE TaskManager.TaskType.prefix = _prefix;
END//

-- insert task type
CREATE OR REPLACE PROCEDURE TaskManager.Insert_TaskType(IN _createdBy INT, IN _name VARCHAR(255), IN _prefix VARCHAR(4))
BEGIN
    INSERT INTO TaskManager.TaskType (createdBy, name, prefix)
    VALUES (_createdBy, _name, _prefix);
END//

-- update task type. taskTypeName, taskTypeVersion, taskTypePrefix are all optional. if they are not provided, the value will not be updated
CREATE OR REPLACE PROCEDURE TaskManager.Update_TaskType(IN _modifiedBy INT, IN _taskTypeId INT, IN _taskTypeName VARCHAR(255), IN _taskTypeVersion INT, IN _taskTypePrefix VARCHAR(4))
BEGIN
    UPDATE TaskManager.TaskType
    SET
        modifiedBy = _modifiedBy,
        name = COALESCE(_taskTypeName, name),
        taskTypeVersion = COALESCE(_taskTypeVersion, taskTypeVersion),
        prefix = COALESCE(_taskTypePrefix, prefix)
    WHERE TaskManager.TaskType.id = _taskTypeId;
END//

-- delete task type
CREATE OR REPLACE PROCEDURE TaskManager.Delete_TaskType(IN _deletedBy INT, IN _taskTypeId INT)
BEGIN
    UPDATE TaskManager.TaskType
    SET
        deleted = 1,
        modifiedBy = _deletedBy
    WHERE TaskManager.TaskType.id = _taskTypeId;
END//

-- restore task type
CREATE OR REPLACE PROCEDURE TaskManager.Restore_TaskType(IN _restoredBy INT, IN _taskTypeId INT)
BEGIN
    UPDATE TaskManager.TaskType
    SET
        deleted = 0,
        modifiedBy = _restoredBy
    WHERE TaskManager.TaskType.id = _taskTypeId;
END//

-- USER PROCEDURES

-- select all users
CREATE OR REPLACE PROCEDURE TaskManager.Select_Users()
BEGIN
    SELECT *
    FROM TaskManager.User;
END//

-- select user by id
CREATE OR REPLACE PROCEDURE TaskManager.Select_UserById(IN _id INT)
BEGIN
    SELECT *
    FROM TaskManager.User
    WHERE TaskManager.User.id = _id;
END//

-- select user by username
CREATE OR REPLACE PROCEDURE TaskManager.Select_UsersByUsername(IN _username VARCHAR(16))
BEGIN
    SELECT *
    FROM TaskManager.User
    WHERE TaskManager.User.username = _username;
END//

-- select user by uuid
CREATE OR REPLACE PROCEDURE TaskManager.Select_UserByUuid(IN _uuid CHAR(36))
BEGIN
    SELECT *
    FROM TaskManager.User
    WHERE TaskManager.User.uuid = _uuid;
END//

-- insert user and return the row id of the inserted user
CREATE OR REPLACE PROCEDURE TaskManager.Insert_User(IN _createdBy INT, IN _uuid CHAR(36), IN _username VARCHAR(16))
BEGIN
    INSERT INTO TaskManager.User (createdBy, uuid, username)
    VALUES (_createdBy, _uuid, _username);
END//

-- update user. username and uuid is optional. if it is not provided, the value will not be updated
CREATE OR REPLACE PROCEDURE TaskManager.Update_User(IN _modifiedBy INT, IN _userId INT, IN _uuid CHAR(36), IN _username VARCHAR(16))
BEGIN
    UPDATE TaskManager.User
    SET
        modifiedBy = _modifiedBy,
        uuid = COALESCE(_uuid, uuid),
        username = COALESCE(_username, username)
    WHERE TaskManager.User.id = _userId;
END//

-- delete user
CREATE OR REPLACE PROCEDURE TaskManager.Delete_User(IN _deletedBy INT, IN _userId INT)
BEGIN
    UPDATE TaskManager.User
    SET deleted = 1,
        modifiedBy = _deletedBy
    WHERE TaskManager.User.id = _userId;
END//

-- restore user
CREATE OR REPLACE PROCEDURE TaskManager.Restore_User(IN _restoredBy INT, IN _userId INT)
BEGIN
    UPDATE TaskManager.User
    SET deleted = 0,
        modifiedBy = _restoredBy
    WHERE TaskManager.User.id = _userId;
END//

-- TASK PROCEDURES

-- select all tasks
CREATE OR REPLACE PROCEDURE TaskManager.Select_Tasks()
BEGIN
    SELECT *
    FROM TaskManager.Task;
END//

-- select task by id
CREATE OR REPLACE PROCEDURE TaskManager.Select_TaskById(IN _id INT)
BEGIN
    SELECT *
    FROM TaskManager.Task
    WHERE TaskManager.Task.id = _id;
END//

-- select tasks by projectId
CREATE OR REPLACE PROCEDURE TaskManager.Select_TasksByProject(IN _projectId INT)
BEGIN
    SELECT *
    FROM TaskManager.Task
    WHERE TaskManager.Task.projectId = _projectId;
END//

-- select tasks by project and type
CREATE OR REPLACE PROCEDURE TaskManager.Select_TasksByProjectAndType(IN _projectId INT, IN _typeId INT)
BEGIN
    SELECT *
    FROM TaskManager.Task
    WHERE TaskManager.Task.projectId = _projectId AND TaskManager.Task.typeId = _typeId;
END//

-- insert task parentId is optional
CREATE OR REPLACE PROCEDURE TaskManager.Insert_Task(IN _createdBy INT, IN _typeId INT, IN _projectId INT, IN _parentId INT)
BEGIN
    INSERT INTO TaskManager.Task (createdBy, typeId, projectId, parentId)
    VALUES (_createdBy, _typeId, _projectId, _parentId);
END//

-- update task. parentId is optional, projectId cannot be updated
CREATE OR REPLACE PROCEDURE TaskManager.Update_Task(IN _modifiedBy INT, IN _taskId INT, IN _parentId INT)
BEGIN
    UPDATE TaskManager.Task
    SET
        modifiedBy = _modifiedBy,
        parentId = COALESCE(_parentId, parentId)
    WHERE TaskManager.Task.id = _taskId;
END//

-- delete task
CREATE OR REPLACE PROCEDURE TaskManager.Delete_Task(IN _deletedBy INT, IN _taskId INT)
BEGIN
    UPDATE TaskManager.Task
    SET
        deleted = 1,
        modifiedBy = _deletedBy
    WHERE TaskManager.Task.id = _taskId;
END//

-- restore task
CREATE OR REPLACE PROCEDURE TaskManager.Restore_Task(IN _restoredBy INT, IN _taskId INT)
BEGIN
    UPDATE TaskManager.Task
    SET
        deleted = 0,
        modifiedBy = _restoredBy
    WHERE TaskManager.Task.id = _taskId;
END//

-- TASK ATTRIBUTES PROCEDURES

-- select all task attributes of a task
CREATE OR REPLACE PROCEDURE TaskManager.Select_TaskAttributes(IN _taskId INT)
BEGIN
    SELECT *
    FROM TaskManager.TaskAttributes
    WHERE TaskManager.TaskAttributes.taskId = _taskId;
END//

-- select all task attributes of a task by attributeId
CREATE OR REPLACE PROCEDURE TaskManager.Select_TaskAttributesByAttribute(IN _taskId INT, IN _attributeId INT)
BEGIN
    SELECT *
    FROM TaskManager.TaskAttributes
    WHERE TaskManager.TaskAttributes.taskId = _taskId AND TaskManager.TaskAttributes.attributeId = _attributeId;
END//

-- select task attribute by id
CREATE OR REPLACE PROCEDURE TaskManager.Select_TaskAttributeById(IN _id INT)
BEGIN
    SELECT *
    FROM TaskManager.TaskAttributes
    WHERE TaskManager.TaskAttributes.id = _id;
END//

-- insert task attribute
CREATE OR REPLACE PROCEDURE TaskManager.Insert_TaskAttribute(IN _createdBy INT, IN _taskId INT, IN _attributeId INT, IN _value LONGTEXT)
BEGIN
    INSERT INTO TaskManager.TaskAttributes (createdBy, taskId, attributeId, value)
    VALUES (_createdBy, _taskId, _attributeId, _value);
END//

-- update task attribute
CREATE OR REPLACE PROCEDURE TaskManager.Update_TaskAttribute(IN _modifiedBy INT, IN _taskAttributeId INT, IN _value LONGTEXT)
BEGIN
    UPDATE TaskManager.TaskAttributes
    SET
        modifiedBy = _modifiedBy,
        value = COALESCE(_value, value)
    WHERE TaskManager.TaskAttributes.id = _taskAttributeId;
END//

-- delete task attribute
CREATE OR REPLACE PROCEDURE TaskManager.Delete_TaskAttribute(IN _deletedBy INT, IN _taskAttributeId INT)
BEGIN
    UPDATE TaskManager.TaskAttributes
    SET
        deleted = 1,
        modifiedBy = _deletedBy
    WHERE TaskManager.TaskAttributes.id = _taskAttributeId;
END//

-- restore task attribute
CREATE OR REPLACE PROCEDURE TaskManager.Restore_TaskAttribute(IN _restordBy INT, IN _taskAttributeId INT)
BEGIN
    UPDATE TaskManager.TaskAttributes
    SET
        deleted = 0,
        modifiedBy = _restordBy
    WHERE TaskManager.TaskAttributes.id = _taskAttributeId;
END//

-- SCHEMA PROCEDURES

-- insert the current schema version
CREATE OR REPLACE PROCEDURE TaskManager.Insert_SchemaVersion(IN _version INT)
BEGIN
    DELETE FROM TaskManager.SchemaVersion;
    INSERT INTO TaskManager.SchemaVersion (version)
    VALUES (_version);
END//

-- select the current schema version
CREATE OR REPLACE PROCEDURE TaskManager.Select_SchemaVersion()
BEGIN
    SELECT *
    FROM TaskManager.SchemaVersion;
END//

-- update the schema version
CREATE OR REPLACE PROCEDURE TaskManager.Update_SchemaVersion(IN _version INT)
BEGIN
    UPDATE TaskManager.SchemaVersion
    SET version = _version;
END//

DELIMITER ;