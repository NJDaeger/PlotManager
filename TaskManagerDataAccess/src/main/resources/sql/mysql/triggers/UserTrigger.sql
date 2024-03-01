CREATE TRIGGER IF NOT EXISTS user_modified_trigger
    BEFORE UPDATE ON User
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;