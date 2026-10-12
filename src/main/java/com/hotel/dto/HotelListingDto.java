package com.hotel.dto;

import java.util.List;

public record HotelListingDto(
    Long id,
    String hotelName,
    String city,
    String state,
    String address,
    int roomCount,
    String website,
    List<HotelApplicationPhotoDto> photos
) {
}
