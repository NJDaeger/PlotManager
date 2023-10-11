CREATE TRIGGER IF NOT EXISTS plot_modified_trigger
    BEFORE UPDATE ON Plot
    FOR EACH ROW
    BEGIN
        SET NEW.modified = unix_timestamp();
    END;