package com.hotel.dto;

import com.hotel.entity.HotelBooking;
import com.hotel.entity.HotelBookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HotelBookingDto(
    Long id,
    String bookingReference,
    Long hotelId,
    String hotelName,
    String city,
    String guestName,
    String guestEmail,
    String guestPhone,
    LocalDate checkIn,
    LocalDate checkOut,
    int guestCount,
    HotelBookingStatus status,
    LocalDateTime createdAt
) {
    public static HotelBookingDto from(HotelBooking booking) {
        return new HotelBookingDto(
            booking.getId(),
            booking.getBookingReference(),
            booking.getHotel().getId(),
            booking.getHotel().getHotelName(),
            booking.getHotel().getCity(),
            booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName(),
            booking.getGuest().getEmail(),
            booking.getGuest().getPhone(),
            booking.getCheckIn(),
            booking.getCheckOut(),
            booking.getGuestCount(),
            booking.getStatus(),
            booking.getCreatedAt()
        );
    }
}
