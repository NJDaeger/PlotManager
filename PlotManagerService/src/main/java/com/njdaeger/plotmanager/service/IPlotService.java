package com.njdaeger.plotmanager.service;

import com.njdaeger.plotmanager.service.models.Plot;
import org.bukkit.Location;

public interface IPlotService extends ITransactionalService {

    /**
     * Creates a draft plot at the given location. no attributes are assigned to this plot, no people can be assigned to this plot
     *
     * @param location The location to create this plot
     * @return The plot that was created
     */
    PlotBuilder createPlotBuilder(Location location);

    /**
     *
     *
     * @param plot
     * @return
     */
    int createPlot(Plot plot);

}
