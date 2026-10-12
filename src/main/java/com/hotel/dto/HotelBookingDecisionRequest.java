package com.hotel.dto;

import jakarta.validation.constraints.NotNull;

public record HotelBookingDecisionRequest(@NotNull Decision decision) {
    public enum Decision {
        ACCEPT,
        REJECT
    }
}
