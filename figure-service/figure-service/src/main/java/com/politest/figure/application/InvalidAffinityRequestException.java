package com.politest.figure.application;

/** Raised when an affinity request does not meet the contract (unknown axis, value out of range, bad language). */
public class InvalidAffinityRequestException extends RuntimeException {

    public InvalidAffinityRequestException(String message) {
        super(message);
    }
}
