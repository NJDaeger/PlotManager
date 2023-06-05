CREATE TABLE IF NOT EXISTS PlotTags
(
    plotId int NOT NULL,
    tagId int NOT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy int NOT NULL,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PlotTags_plotId_fk FOREIGN KEY (plotId) REFERENCES Plot (id),
    CONSTRAINT PlotTags_tagId_fk FOREIGN KEY (tagId) REFERENCES PlotTag (id),
    CONSTRAINT PlotTags_createdBy_fk FOREIGN KEY (createdBy) REFERENCES User (id),
    CONSTRAINT PlotTags_plotId_tagId_uc UNIQUE (plotId, tagId)
)