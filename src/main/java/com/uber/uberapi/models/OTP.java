package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="otp")
public class OTP extends Auditable{
    private String code;
    private String sentToNumber;

    public static OTP make(String phoneNumber) {
        return OTP.builder()
                .code("0000") // plugin a random number generator here
                .sentToNumber(phoneNumber)
                .build();
    }

    public boolean validateEnteredOTP(OTP otp, int rideStartOTPExpiryMinutes) {
        // TODO
        return true;
    }
}

// Global Constant : OTP_Expiry_Time