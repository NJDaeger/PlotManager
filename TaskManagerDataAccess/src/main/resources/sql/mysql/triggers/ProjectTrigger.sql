CREATE TRIGGER IF NOT EXISTS project_modified_trigger
    BEFORE UPDATE ON Project
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;