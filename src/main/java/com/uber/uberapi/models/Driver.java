package com.uber.uberapi.models;

import com.uber.uberapi.exceptions.UnapprovedDriverException;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="driver")
public class Driver extends Auditable{
    private Gender gender;
    private String name;

    @OneToOne(mappedBy = "driver")
    private Car car;

    private String picUrl; // image location - Amazon S3

    @OneToOne
    private Account account;

    private String phoneNumber;

    private String licenseDetails;

    @Temporal(value = TemporalType.DATE)
    private Date dob;

    @Enumerated(value = EnumType.STRING)
    private DriverApprovalStatus approvalStatus;

    @OneToMany(mappedBy = "driver")
    private List<Booking> bookings; // bookings that the driver actually drove

    // put a unique constraint

    @ManyToMany(mappedBy = "notifiedDrivers", cascade = CascadeType.PERSIST)
    private Set<Booking> acceptableBookings = new HashSet<>();// bookings that the driver can currently accept

    @OneToOne
    private Booking activeBooking = null;
    // driver.active_booking_id can be either null or valid

    private Boolean isAvailable;

    private String activeCity;

    @OneToOne
    private ExactLocation lastKnownLocation;

    @OneToOne
    private Review avgRating; // will be updated by a nightly cron job

    @OneToOne
    private ExactLocation home;

    public void setAvailable(Boolean available) {
        if(available && !this.getApprovalStatus().equals(DriverApprovalStatus.APPROVED)){
            throw new UnapprovedDriverException("Driver approval pending or denied " + this.getId());
        }
        isAvailable = available;
    }

    public boolean canAcceptBooking() {
        if(isAvailable && activeBooking == null){
            return true;
        }
        // check if the current ride ends within 10 mins, then I can accept
        //return activeBooking.getExpectedCompletiontTime().before(new Date() + ten_minutes);
        return true;

    }
}
