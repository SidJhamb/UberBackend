package com.uber.uberapi.services.drivermatching.filters;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.services.ConstantsService;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class DriverFilter {
    private ConstantsService constantsService;

    public DriverFilter(ConstantsService constantsService){
        this.constantsService = constantsService;
    }

    public abstract List<Driver> apply(List<Driver> drivers, Booking booking);
}
