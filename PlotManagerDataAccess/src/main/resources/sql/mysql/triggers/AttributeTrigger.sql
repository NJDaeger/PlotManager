CREATE TRIGGER IF NOT EXISTS attribute_modified_trigger
    BEFORE UPDATE ON Attribute
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;