package com.uber.uberapi.exceptions;

public class InvalidActionForBookingStateException extends UberException {
    public InvalidActionForBookingStateException(String s) {
        super(s);
    }
}
