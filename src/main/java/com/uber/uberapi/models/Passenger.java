package com.uber.uberapi.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="passenger")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passenger extends Auditable{
    // Passenger is the owner of User Account
    @OneToOne(cascade = CascadeType.ALL)
    private Account account;

    private String name;

    @Enumerated(value = EnumType.STRING)
    private Gender Gender;

    @OneToMany(mappedBy = "passenger")
    private List<Booking> bookings = new ArrayList<>();

    @OneToOne
    private Booking activeBooking = null;

    @Temporal(value = TemporalType.DATE)
    private Date dob;

    private String phoneNumber;

    @OneToOne
    private ExactLocation home;
    @OneToOne
    private ExactLocation work;
    @OneToOne
    private ExactLocation lastKnownLocation;

    @OneToOne
    private Review avgRating;
    // this is updated by a cron job that runs nightly






}

// VARCHAR (size)
// TEXT
// JSON
// BLOB
// INT
