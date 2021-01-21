package com.uber.uberapi.services.otp;

import com.uber.uberapi.models.OTP;
import com.uber.uberapi.services.otp.OTPService;
import org.springframework.stereotype.Service;

@Service
public class ConsoleOTPService implements OTPService {
    @Override
    public void sendPhoneNumberConfirmationOTP(OTP otp) {
        System.out.printf("Confirm phone number %s: OTP - %s", otp.getSentToNumber(), otp.getCode());
    }

    @Override
    public void sendRideStartOTP(OTP otp) {
        System.out.printf("Share this otp with the driver to start the ride %s: OTP - %s", otp.getSentToNumber(), otp.getCode());
    }
}
