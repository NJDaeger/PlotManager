CREATE TRIGGER IF NOT EXISTS group_modified_trigger
    BEFORE UPDATE ON `Group`
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;