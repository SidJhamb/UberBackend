package com.uber.uberapi.services.drivermatching.filters;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.ExactLocation;
import com.uber.uberapi.services.ConstantsService;
import com.uber.uberapi.services.ETAService;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ETABasedFilter extends DriverFilter{
    private final ETAService etaService;
    private final ConstantsService constants;

    public ETABasedFilter(ETAService etaService, ConstantsService constants) {
        super(constants);
        this.etaService = etaService;
        this.constants = constants;
    }

    public List<Driver> apply(List<Driver> drivers, Booking booking) {
        if (!getConstants().getIsETABasedFilterEnabled()) return drivers;
        ExactLocation pickup = booking.getPickupLocation();
        return drivers.stream().filter(driver -> {
            return etaService.getETAMinutes(driver.getLastKnownLocation(), pickup) <= getConstants().getMaxDriverETAMinutes();
        }).collect(Collectors.toList());
    }
}


