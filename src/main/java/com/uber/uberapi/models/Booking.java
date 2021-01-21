package com.uber.uberapi.models;

import com.uber.uberapi.exceptions.InvalidActionForBookingStateException;
import com.uber.uberapi.exceptions.InvalidOTPException;
import lombok.*;
import org.hibernate.annotations.ManyToAny;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking", indexes = {
        @Index(columnList = "passenger_id"),
        @Index(columnList = "driver_id")
})
public class Booking extends Auditable{
    @ManyToOne
    private Passenger passenger;

    @ManyToOne
    private Driver driver;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Driver> notifiedDrivers = new HashSet<>();
    // storing which drivers can potentially accept this booking

    @Enumerated(value = EnumType.STRING)
    private BookingType bookingType;

    @OneToOne
    private Review reviewByPassenger;

    @OneToOne
    private Review reviewByDriver;

    @OneToOne
    private PaymentReceipt paymentReceipt; // todo: add payment services

    @Enumerated(value = EnumType.STRING)
    private BookingStatus bookingStatus;

    /*
     exact_location.booking_id is not the case,
     because we dont have a back reference to Booking in the exact location table (mappedby thing)
     booking_route -> booking_id, exact_location_id // Primary Key (booking_id, exact_location_id)
     10 million bookings, min 2 exact locations per booking, 20 million entries
     If query is, get the route for a booking
     A default index is created for a primary key
     We can also add an index of our own to optimize search queries
    */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="booking_route",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name="exact_location_id"),
            indexes = {@Index(columnList = "booking_id")}
    )
    @OrderColumn(name = "location_index")
    private List<ExactLocation> route = new ArrayList<>();
    // ordered list
    // start location, next location, next to next location, end location
    // How to make this list ordered?

    // booking_route > booking_route, location_index, exact_location_id

    @OneToMany
    @JoinTable(
            name="booking_completed_route",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name="exact_location_id"),
            indexes = {@Index(columnList = "booking_id")}
    )
    @OrderColumn(name = "location_index")
    private List<ExactLocation> completedRoute = new ArrayList<>();

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startTime; // actual start time

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date endTime; // actual end time

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date expectedCompletiontTime; // filled by the location tracking service

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date scheduledTime;

    private Long totalDistanceMeters; // also be tracked by the location tracking service

    // a cron job deleted otps of completed rides
    @OneToOne
    private OTP rideStartOTP;
    // created as soon as the booking is made
    // but not sent to the passenger immidietly
    // only sent when the driver is assigned for a scheduled ride


    public void startRide(OTP otp, int rideStartOTPExpiryMinutes)  {
        startTime = new Date();
        if(!bookingStatus.equals(BookingStatus.CAB_ARRIVED)){
            throw new InvalidActionForBookingStateException("Cannot start the ride before the driver has reached the pickup point");
        }

        if(!rideStartOTP.getCode().equals(otp.getCode())){
            throw new InvalidOTPException();
        }

        if(!rideStartOTP.validateEnteredOTP(otp, rideStartOTPExpiryMinutes)){
            throw new InvalidOTPException();
        }

        bookingStatus = BookingStatus.IN_RIDE;
        passenger.setActiveBooking(this);
    }

    public void endRide()  {
        endTime = new Date();
        if(!bookingStatus.equals(BookingStatus.IN_RIDE)){
            throw new InvalidActionForBookingStateException("Cannot end the ride before the driver has reached the drop point");
        }

        driver.setActiveBooking(null);
        bookingStatus = BookingStatus.COMPLETED;
        passenger.setActiveBooking(null);
    }

    public void rateRideByDriver(Review review) {
    }

    public void rateRideByPassenger(Review review) {
    }

    public boolean canChangeRoute() {
        return bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER)
                || bookingStatus.equals(BookingStatus.CAB_ARRIVED)
                || bookingStatus.equals(BookingStatus.IN_RIDE)
                || bookingStatus.equals(BookingStatus.SCHEDULED)
                || bookingStatus.equals(BookingStatus.REACHING_PICKUP_LOCATION);
    }

    public boolean needsDriver() {
        return bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER);
    }

    public ExactLocation getPickupLocation() {
        return route.get(0);
    }

    public void cancel() {
        if(!bookingStatus.equals(BookingStatus.REACHING_PICKUP_LOCATION) ||
                bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER) ||
                bookingStatus.equals(BookingStatus.SCHEDULED) ||
                bookingStatus.equals(BookingStatus.CAB_ARRIVED) ){
            throw new InvalidActionForBookingStateException("Cannot cancel the ride now. If the ride is in progress, ask the driver to end the ride.");
        }
        bookingStatus = BookingStatus.CANCELLED;
        driver = null;
        notifiedDrivers.clear();
    }
}
