package com.uber.uberapi.controllers;

import com.uber.uberapi.exceptions.InvalidBookingException;
import com.uber.uberapi.exceptions.InvalidDriverException;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.ReviewRepository;
import com.uber.uberapi.services.ConstantsService;
import com.uber.uberapi.services.BookingService;
import com.uber.uberapi.services.drivermatching.DriverMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// Controller can have access to DAL (repo layer)
// Not everything needs to go via the service
// Something that is more complicated is usually abstracted out into a separate service
// Controllers talk to models and they talk to services
// Services talk between each other
// Services might talk to some external controllers, and they might talk to models

// -------------- (Important)
// Controllers ->(talk to) models/services/repo
// Services -> other services / other controllers / models
// models(DAO) -> DB
// repositories(DAL) -> manage the models

@RequestMapping("/driver")
@RestController
public class DriverController {
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    DriverMatchingService driverMatchingService;
    @Autowired
    BookingService bookingService;
    @Autowired
    ConstantsService constants;

    // all the endpoints that the driver can use
    public Driver getDriverFromId(long driverId) {
        Optional<Driver> driver = driverRepository.findById(driverId);
        if(driver.isEmpty()){
            throw new InvalidDriverException("No driver with id " + driverId);
        }
        return driver.get();
    }

    public Booking getDriverBookingFromId(long bookingId, long driverId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        Driver driver = getDriverFromId(driverId);
        Booking booking = optionalBooking.get();
        if(optionalBooking.isEmpty()){
            throw new InvalidBookingException("No booking with id " + bookingId);
        }

        if (booking.getDriver().equals(driver)) {
            throw new InvalidBookingException("Driver " + driverId + " has no such booking " + bookingId);
        }

        return booking;
    }

    private Booking getBookingFromId(Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if(booking.isEmpty()){
            throw new InvalidBookingException("No booking with id " + bookingId);
        }
        return booking.get();
    }

    // session/jwt based authentication
    // We can fetch the currently logged in userId/driverId from the
    // session or jwt tokens based on the auth mechanism
    // But its a good practice to keep the driverId as a request param
    // and internally verify whether the driverId is authenticated
    @GetMapping("/{driverId}")
    public Driver getDriver(@PathVariable(name = "driverId") Long driverId) {
        // driver 10 has authenticated from thr auth layer
        // but it is trying to hit /driver/20/bookings, we should disallow this, hence a check
        // another option could have been to only expose /drivers/bookings

        // make sure the driver is authenticated and has the same driverid as requested
        return getDriverFromId(driverId);
    }

    // Exception Handling :
    // Currently if an exception is thrown by this API call,
    // we return a blanket 500 error code
    // we can also map each exception to a different response code if there is a need
    @GetMapping("/{driverId}/bookings")
    public List<Booking> getAllBookings(@PathVariable(name = "driverId") Long driverId) {
        Driver driver = getDriverFromId(driverId);
        return driver.getBookings();
    }

    @GetMapping("/{driverId}/bookings/{bookingId}")
    public Booking getBooking(@PathVariable(name = "driverId") Long driverId,
                              @PathVariable(name = "driverId") Long bookingId) {
        Driver driver = getDriverFromId(driverId);
        return getDriverBookingFromId(bookingId, driverId);
    }

    @PostMapping("/{driverId}/bookings/{bookingId}")
    public void acceptBooking(@PathVariable(name = "driverId") Long driverId,
                              @PathVariable(name = "bookingId") Long bookingId) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getBookingFromId(bookingId);
        // delegating the ton of business logic to Booking service
        bookingService.acceptBooking(driver, booking);
        //driverMatchingService.acceptBooking(driver, booking);
    }



    @DeleteMapping("/{driverId}/bookings/{bookingId}")
    public void cancelBooking(@PathVariable(name = "driverId") Long driverId,
                              @PathVariable(name = "bookingId") Long bookingId) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driverId);
        // delegating the ton of business logic to Booking service
        bookingService.cancelByDriver(driver, booking);
        //driverMatchingService.cancelByDriver(driver, booking);
    }

    // Start the ride
    @PatchMapping("/{driverId}/bookings/{bookingId}/startRide")
    public void startRide(@PathVariable(name = "driverId") Long driverId,
                          @PathVariable(name = "bookingId") Long bookingId,
                          @RequestBody OTP otp) {
        Booking booking = getDriverBookingFromId(bookingId, driverId);

        // {Business Logic}
        // Confirm the OTP
        // the ride is currently in the correct state

        // This business logic shouldn't be handled directly by the controller
        // So we are delegating this to the booking model (startRide)
        // Just like what we did in changeAvailability method

        // booking.setBookingStatus(BookingStatus.IN_RIDE);

        booking.startRide(otp, constants.getRideStartOTPExpiryMinutes());
        bookingRepository.save(booking);
    }

    // End the ride
    @PatchMapping("/{driverId}/bookings/{bookingId}/endRide")
    public void startRide(@RequestParam(name = "driverId") Long driverId,
                          @RequestParam(name = "bookingId") Long bookingId) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driverId);
        booking.endRide();
        driverRepository.save(driver);
        bookingRepository.save(booking);
    }

    // Rate the ride
    @PatchMapping("/{driverId}/bookings/{bookingId}/rateRide")
    public void rateRide(@RequestParam(name = "driverId") Long driverId,
                         @RequestParam(name = "bookingId") Long bookingId,
                         @RequestBody Review data) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driverId);

        // Best practice :
        // We could have just had booking.rateRide(data);
        // But we recreate the review object after safety checks
        // Never let a user directly create objects
        // Let the controller do it
        Review review = Review.builder()
                .note(data.getNote())
                .ratingOutOfFive(data.getRatingOutOfFive())
                .build();

        booking.rateRideByDriver(review);
        reviewRepository.save(review);
        bookingRepository.save(booking);
    }

    // REST API :
    // URI, request -> mark the driver as unavailable
    @PatchMapping("/{driverId}")
    public void changeAvailability(@RequestParam(name = "driverId") Long driverId,
                                   @RequestBody Boolean available){
        // the controller should not be handling this business logic
        // so we are delegating this to the driver model class (setIsAvailable)
//        if(available && !this.getApprovalStatus().equals(DriverApprovalStatus.APPROVED)){
//            throw new UnapprovedDriverException("Driver approval pending or denied " + this.getId());
//        }
        Driver driver = getDriverFromId(driverId);
        driver.setIsAvailable(available);
        driverRepository.save(driver);
    }

}
