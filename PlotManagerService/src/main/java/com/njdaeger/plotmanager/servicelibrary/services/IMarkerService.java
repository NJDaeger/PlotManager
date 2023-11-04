package com.njdaeger.plotmanager.servicelibrary.services;

import com.njdaeger.plotmanager.servicelibrary.models.Plot;

public interface IMarkerService {

    void createPlotMarker(Plot plot);

    void deletePlotMarker(Plot plot);

}
