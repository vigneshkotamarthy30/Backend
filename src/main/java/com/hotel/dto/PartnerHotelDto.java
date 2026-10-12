package com.hotel.dto;

import com.hotel.entity.HotelApplicationStatus;

public record PartnerHotelDto(
    Long id,
    String hotelName,
    String city,
    String state,
    int roomCount,
    HotelApplicationStatus status
) {
}
