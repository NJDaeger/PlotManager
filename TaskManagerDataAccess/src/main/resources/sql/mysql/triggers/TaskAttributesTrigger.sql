CREATE TRIGGER IF NOT EXISTS taskAttributes_modified_trigger
    BEFORE UPDATE ON TaskAttributes
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;