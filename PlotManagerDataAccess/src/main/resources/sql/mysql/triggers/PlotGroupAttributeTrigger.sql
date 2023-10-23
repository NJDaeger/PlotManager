CREATE TRIGGER IF NOT EXISTS plotGroupAttribute_modified_trigger
    BEFORE UPDATE ON PlotGroupAttribute
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;