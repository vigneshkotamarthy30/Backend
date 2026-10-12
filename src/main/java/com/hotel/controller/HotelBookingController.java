package com.hotel.controller;

import com.hotel.dto.HotelBookingDecisionRequest;
import com.hotel.dto.HotelBookingDto;
import com.hotel.dto.HotelBookingRequest;
import com.hotel.entity.HotelBooking;
import com.hotel.service.HotelBookingService;
import com.hotel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HotelBookingController {
    private final HotelBookingService bookingService;
    private final UserService userService;

    public HotelBookingController(HotelBookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping("/hotels/{hotelId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public HotelBookingDto requestBooking(@PathVariable Long hotelId, @Valid @RequestBody HotelBookingRequest request) {
        return HotelBookingDto.from(bookingService.create(hotelId, userService.getCurrentUser().getId(), request));
    }

    @GetMapping("/bookings/my/hotels")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<HotelBookingDto> findMyHotelBookings() {
        return bookingService.findForGuest(userService.getCurrentUser().getId())
            .stream().map(HotelBookingDto::from).toList();
    }

    @GetMapping("/partner/hotels/bookings")
    @PreAuthorize("hasRole('HOTEL_PARTNER')")
    public List<HotelBookingDto> findPartnerHotelBookings() {
        return bookingService.findForOwner(userService.getCurrentUser().getId())
            .stream().map(HotelBookingDto::from).toList();
    }

    @PatchMapping("/partner/hotels/bookings/{bookingId}")
    @PreAuthorize("hasRole('HOTEL_PARTNER')")
    public HotelBookingDto decideBooking(
        @PathVariable Long bookingId,
        @Valid @RequestBody HotelBookingDecisionRequest request) {
        boolean accept = request.decision() == HotelBookingDecisionRequest.Decision.ACCEPT;
        HotelBooking booking = bookingService.decide(bookingId, userService.getCurrentUser().getId(), accept);
        return HotelBookingDto.from(booking);
    }
}
