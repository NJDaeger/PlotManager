CREATE TRIGGER IF NOT EXISTS task_modified_trigger
    BEFORE UPDATE ON Task
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;