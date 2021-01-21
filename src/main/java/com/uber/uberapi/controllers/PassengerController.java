package com.uber.uberapi.controllers;

import com.uber.uberapi.exceptions.InvalidBookingException;
import com.uber.uberapi.exceptions.InvalidPassengerException;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import com.uber.uberapi.repositories.ReviewRepository;
import com.uber.uberapi.services.BookingService;
import com.uber.uberapi.services.drivermatching.DriverMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequestMapping("/passenger")
@RestController
public class PassengerController {
    // handle all operations for passenger
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    DriverMatchingService driverMatchingService;
    @Autowired
    BookingService bookingService;

    // all the endpoints that the passenger can use
    public Passenger getPassengerFromId(long passengerId) {
        Optional<Passenger> passenger = passengerRepository.findById(passengerId);
        if(passenger.isEmpty()){
            throw new InvalidPassengerException("No passenger with id " + passengerId);
        }
        return passenger.get();
    }

    public Booking getPassengerBookingFromId(long bookingId, long passengerId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = optionalBooking.get();
        if(optionalBooking.isEmpty()){
            throw new InvalidBookingException("No booking with id " + bookingId);
        }

        if (booking.getPassenger().equals(passenger)) {
            throw new InvalidPassengerException("Passenger " + passengerId + " has no such booking " + bookingId);
        }

        return booking;
    }

    // session/jwt based authentication
    // We can fetch the currently logged in userId/driverId from the
    // session or jwt tokens based on the auth mechanism
    // But its a good practice to keep the driverId as a request param
    // and internally verify whether the driverId is authenticated
    @GetMapping("/{driverId}")
    public Passenger getPassenger(@RequestParam(name = "passengerId") Long passengerId) {
        // driver 10 has authenticated from thr auth layer
        // but it is trying to hit /driver/20/bookings, we should disallow this, hence a check
        // another option could have been to only expose /drivers/bookings

        // make sure the driver is authenticated and has the same driverid as requested
        return getPassengerFromId(passengerId);
    }

    @GetMapping("/{passengerId}/bookings")
    public List<Booking> getAllBookings(@RequestParam(name = "passengerId") Long passengerId) {
        Passenger passenger = getPassengerFromId(passengerId);
        return passenger.getBookings();
    }

    @GetMapping("/{passengerId}/bookings/{bookingId}")
    public Booking getBooking(@RequestParam(name = "passengerId") Long passengerId,
                              @RequestParam(name = "driverId") Long bookingId) {
        Passenger passenger = getPassengerFromId(passengerId);
        return getPassengerBookingFromId(bookingId, passengerId);
    }

    @PostMapping("/{passengerId}/bookings")
    public void requestBooking(@RequestParam(name = "passengerId") Long passengerId,
                              @RequestBody Booking data) {
        Passenger passenger = getPassengerFromId(passengerId);
        List<ExactLocation> route = new ArrayList<>();
        data.getRoute().forEach(exactLocation ->
                route.add(ExactLocation.builder()
                        .latitude(exactLocation.getLatitude())
                        .longitude(exactLocation.getLongitude())
                        .build()
                ));
        Booking booking = Booking.builder()
                .rideStartOTP(OTP.make(passenger.getPhoneNumber()))
                .route(route)
                .passenger(passenger)
                .bookingType(data.getBookingType())
                .scheduledTime(data.getScheduledTime())
                .build();
        bookingService.createBooking(booking); // Delegating some business logic to Booking Service
    }

    // passenger requests booking
    // saved to db
    // message sent to drivermatching service
    // consume the message
    // find the drivers
    // if none are available
    // passenger will be notified
    // passenger might retry to find drivers
    // or the passenger might cancel the booking
    // put, patch, get -> idempotent

    // Controllers are very lightweight
    // They have no or very less business logic
    // Just do message passing like the waiters in a restaurant
    @PostMapping("/{passengerId}/bookings/{bookingId}")
    public void retryBooking(@RequestParam(name = "passengerId") Long passengerId,
                             @RequestParam(name = "driverId") Long bookingId) {
        Booking booking = getPassengerBookingFromId(bookingId, passengerId);
        bookingService.retryBooking(booking);
    }

    @DeleteMapping("/{passengerId}/bookings/{bookingId}")
    public void cancelBooking(@RequestParam(name = "passengerId") Long passengerId,
                              @RequestParam(name = "bookingId") Long bookingId) {
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId, passengerId);
        // delegating the ton of business logic to Booking service
        // it should be the responsibility of the booking service to act as producer
        bookingService.cancelByPassenger(passenger, booking);
        driverMatchingService.cancelByPassenger(passenger, booking);
    }

    // Rate the ride
    @PatchMapping("/{passengerId}/bookings/{bookingId}/rateRide")
    public void rateRide(@RequestParam(name = "passengerId") Long passengerId,
                         @RequestParam(name = "bookingId") Long bookingId,
                         @RequestBody Review data) {
        Booking booking = getPassengerBookingFromId(bookingId, passengerId);

        // Best practice :
        // We could have just had booking.rateRide(data);
        // But we recreate the review object after safety checks
        // Never let a user directly create objects
        // Let the controller do it
        Review review = Review.builder()
                .note(data.getNote())
                .ratingOutOfFive(data.getRatingOutOfFive())
                .build();

        booking.rateRideByPassenger(review);
        reviewRepository.save(review);
        bookingRepository.save(booking);
    }

    @PatchMapping("/{passengerId}/bookings/{bookingId}")
    public void updateRoute(@RequestParam(name = "passengerId") Long passengerId,
                            @RequestParam(name = "bookingId") Long bookingId,
                            @RequestBody Booking data) {
        Booking booking = getPassengerBookingFromId(bookingId, passengerId);
        List<ExactLocation> route = new ArrayList<>(booking.getCompletedRoute());
        data.getRoute().forEach(exactLocation ->
                route.add(ExactLocation.builder()
                        .latitude(exactLocation.getLatitude())
                        .longitude(exactLocation.getLongitude())
                        .build()
                ));

        bookingService.updateRoute(booking, route);

    }

}
