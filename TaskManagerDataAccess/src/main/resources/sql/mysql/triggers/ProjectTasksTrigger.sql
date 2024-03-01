CREATE TRIGGER IF NOT EXISTS projectTasks_modified_trigger
    BEFORE UPDATE ON ProjectTasks
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;