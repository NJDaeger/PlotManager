CREATE TRIGGER IF NOT EXISTS plotAttribute_modified_trigger
    BEFORE UPDATE ON PlotAttribute
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;