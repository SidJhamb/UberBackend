package com.uber.uberapi.services;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.services.locationtracking.LocationTrackingService;
import com.uber.uberapi.services.messagequeue.MQMessage;
import com.uber.uberapi.services.messagequeue.MessageQueue;
import com.uber.uberapi.services.notification.NotificationService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class SchedulingService {
    @Autowired
    BookingService bookingService;
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    ConstantsService constants;
    @Autowired
    LocationTrackingService locationTrackingService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    BookingRepository bookingRepository;

    // we can have a sort of a priority queue over here
    Set<Booking> scheduledBookings = new HashSet<>();


    @Scheduled(fixedRate = 1000)
    public void consumer() {
        MQMessage m = messageQueue.consumeMessage(constants.getSchedulingTopicName());
        if(m == null){
            return;
        }

        Message message = (Message) m;
        scheduleBooking(message.getBooking());
    }

    public void scheduleBooking(Booking booking) {
        // if it is time to activate this booking
        scheduledBookings.add(booking);
        bookingService.acceptBooking(booking.getDriver(), booking);
    }

    @Scheduled(fixedRate = 60000)
    public void process() {
        Set<Booking> newScheduledBookings = new HashSet<>();
        for(Booking booking : scheduledBookings){
            // check if it is time to finally process this booking
            // we have scheduledTime attribute in the booking
            // if that check passes, then call bookingService.acceptBooking
            bookingService.acceptBooking(booking.getDriver(), booking);
        }
    }

    // Consumer
//    public static void main(String[] args) {
//        // queue consumer
//        // for each request
//        // call the appropriate method
//
//        // This is to simulate that this consumer is living in a separate process
//    }

    @Getter @Setter @AllArgsConstructor
    public static class Message implements MQMessage {
        private Booking booking;
    }
}

// There are two kinds of design pattern
// PubSub design pattern
// Observer design pattern -> events
// In Observer, producers and consumers know each other
// In PubSub, they are decoupled, live in separate processes and both talk to a queue
