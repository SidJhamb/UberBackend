package com.uber.uberapi.services;

import com.uber.uberapi.exceptions.InvalidActionForBookingStateException;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import com.uber.uberapi.services.drivermatching.DriverMatchingService;
import com.uber.uberapi.services.messagequeue.MessageQueue;
import com.uber.uberapi.services.notification.NotificationService;
import com.uber.uberapi.services.otp.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    DriverMatchingService driverMatchingService;
    @Autowired
    OTPService otpservice;
    @Autowired
    SchedulingService schedulingService;
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    ConstantsService constants;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    NotificationService notificationService;


    public void createBooking(Booking booking) {
        if(booking.getStartTime().after(new Date())){
            booking.setBookingStatus(BookingStatus.SCHEDULED);
            //producer
            messageQueue.sendMessage(constants.getSchedulingTopicName(), new SchedulingService.Message(booking));
//            {
//                // use a task queue to push this task (producer)
//                // send the message to the scheduling service
//                schedulingService.scheduleBooking(booking);
//            }
        }
        else{
            booking.setBookingStatus(BookingStatus.ASSIGNING_DRIVER);
            otpservice.sendRideStartOTP(booking.getRideStartOTP()); // sent to the passenger
            messageQueue.sendMessage(constants.getDriverMatchingTopicName(), new DriverMatchingService.Message(booking));
//            {
//                // use a task queue to push this task (producer)
//                driverMatchingService.assignDriver(booking);
//            }
            bookingRepository.save(booking);
            passengerRepository.save(booking.getPassenger());
        }
    }

    public void acceptBooking(Driver driver, Booking booking) {
        // called when a booking has been requested by the passenger
        if(!booking.needsDriver()){
            // do something
            return;
        }

        if(!driver.canAcceptBooking()){
            notificationService.notify(driver.getPhoneNumber(), "Cannot accept booking");
            return;
        }
        booking.setDriver(driver);
        driver.setActiveBooking(booking);
        booking.getNotifiedDrivers().clear();
        driver.getAcceptableBookings().clear();

        notificationService.notify(booking.getPassenger().getPhoneNumber(), driver.getName() + "is arriving at pickup location");
        notificationService.notify(driver.getPhoneNumber(), "Booking accepted");
        bookingRepository.save(booking);
        driverRepository.save(driver);

    }

    public void cancelByDriver(Driver driver, Booking booking) {
        booking.setDriver(null);
        driver.setActiveBooking(null);
        driver.getAcceptableBookings().remove(booking);
        notificationService.notify(booking.getPassenger().getPhoneNumber(),
                "Reassigning driver");
        notificationService.notify(driver.getPhoneNumber(),
                "Booking has been cancelled");
        retryBooking(booking);
    }

    public void cancelByPassenger(Passenger passenger, Booking booking) {
        try {
            booking.cancel();
            bookingRepository.save(booking);
        }
        catch(InvalidActionForBookingStateException inner){
            notificationService.notify(booking.getPassenger().getPhoneNumber(),
                    "Cannot cancel the booking now. If the ride is in progress, please contact the driver");
            throw inner;
        }
    }

    public void updateRoute(Booking booking, List<ExactLocation> route) {
        if(!booking.canChangeRoute()) {
            throw new InvalidActionForBookingStateException("Ride has already been completed or cancelled");
        }
        booking.setRoute(route);
        bookingRepository.save(booking);
        notificationService.notify(booking.getDriver().getPhoneNumber(), "Route has been updated!");
    }

    public void retryBooking(Booking booking) {
        createBooking(booking);
    }
}
