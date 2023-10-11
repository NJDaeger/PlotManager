CREATE TRIGGER IF NOT EXISTS plotUsers_modified_trigger
    BEFORE UPDATE ON PlotUsers
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;