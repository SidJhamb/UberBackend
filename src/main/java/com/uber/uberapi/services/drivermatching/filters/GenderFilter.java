package com.uber.uberapi.services.drivermatching.filters;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Gender;
import com.uber.uberapi.services.ConstantsService;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class GenderFilter extends DriverFilter{
    public GenderFilter(ConstantsService constantsService) {
        super(constantsService);
    }

    public List<Driver> apply(List<Driver> drivers, Booking booking) {
        // male drivers can only drive male passengers
        // for a female or other passenger, only get a female or other passenger
        if (!getConstantsService().getIsGenderFilterEnabled()) return drivers;

        Gender passengerGender = booking.getPassenger().getGender();
        return drivers.stream().filter(driver -> {
            Gender driverGender = driver.getGender();
            return !driverGender.equals(Gender.MALE) || passengerGender.equals(Gender.FEMALE);
        }).collect(Collectors.toList());
    }
}
