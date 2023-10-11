CREATE TRIGGER IF NOT EXISTS world_modified_trigger
    BEFORE UPDATE ON World
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;