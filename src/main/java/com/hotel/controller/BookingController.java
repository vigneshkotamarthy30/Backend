package com.hotel.controller;

import com.hotel.dto.BookingDto;
import com.hotel.dto.BookingRequest;
import com.hotel.entity.Booking;
import com.hotel.entity.User;
import com.hotel.service.BookingService;
import com.hotel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingRequest request) {
        User currentUser = userService.getCurrentUser();
        Booking booking = bookingService.createBooking(currentUser.getId(), request);
        return ResponseEntity.ok(bookingService.toDto(booking));
    }

    @GetMapping("/bookings/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingDto>> getMyBookings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Page<Booking> bookings = bookingService.getBookingsForUser(currentUser.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(bookings.getContent().stream().map(bookingService::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/bookings/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDto> getBooking(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        Booking booking = bookingService.getBooking(id);
        if (!booking.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != com.hotel.entity.Role.ADMIN) {
            throw new SecurityException("Not allowed to access this booking.");
        }
        return ResponseEntity.ok(bookingService.toDto(booking));
    }

    @PostMapping("/bookings/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(bookingService.toDto(bookingService.cancelBooking(id, currentUser)));
    }
}
