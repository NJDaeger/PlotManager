CREATE TRIGGER IF NOT EXISTS plotGroups_modified_trigger
    BEFORE UPDATE ON PlotGroups
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;