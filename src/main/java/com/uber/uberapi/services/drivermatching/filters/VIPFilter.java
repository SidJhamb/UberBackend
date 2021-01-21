package com.uber.uberapi.services.drivermatching.filters;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Gender;
import com.uber.uberapi.services.ConstantsService;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class VIPFilter extends DriverFilter{
    public VIPFilter(ConstantsService constantsService) {
        super(constantsService);
    }

    public List<Driver> apply(List<Driver> drivers, Booking booking) {
        // male drivers can only drive male passengers
        // for a female or other passenger, only get a female or other passenger
        if (!getConstantsService().getIsGenderFilterEnabled()) return drivers;

        // if booking is for a prime or sedan, then only match drivers > 4 rating
        // todo
        // for each driver, find the avg rating

        return drivers;
    }
}
