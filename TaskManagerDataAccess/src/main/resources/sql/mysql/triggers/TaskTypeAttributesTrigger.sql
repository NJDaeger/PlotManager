CREATE TRIGGER IF NOT EXISTS taskTypeAttribute_modified_trigger
    BEFORE UPDATE ON TaskTypeAttributes
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;