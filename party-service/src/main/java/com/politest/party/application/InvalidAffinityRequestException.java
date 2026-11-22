package com.politest.party.application;

/** Raised when an affinity request does not meet the contract (unknown axis, value out of range, missing scores). */
public class InvalidAffinityRequestException extends RuntimeException {

    public InvalidAffinityRequestException(String message) {
        super(message);
    }
}
