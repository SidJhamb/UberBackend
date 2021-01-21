package com.uber.uberapi.services;

import com.sun.tools.jconsole.JConsoleContext;
import com.uber.uberapi.models.ExactLocation;
import org.springframework.beans.factory.annotation.Autowired;

public class ETAService {
    @Autowired
    private ConstantsService constants;

    // strategy pattern, ETA based on traffic, based on festivals/events, based on weather
    // chain of responsibility pattern, applying multiple ETA strategies in sequence
    public Integer getETAMinutes(ExactLocation lastKnownLocation, ExactLocation pickup) {
        return (int) (60.0 * lastKnownLocation.distanceKm(pickup) / constants.getDefaultETASpeedKmph());

    }
}
