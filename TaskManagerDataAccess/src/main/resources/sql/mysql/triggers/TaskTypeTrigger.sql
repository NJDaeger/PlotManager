CREATE TRIGGER IF NOT EXISTS taskType_modified_trigger
    BEFORE UPDATE ON TaskType
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;