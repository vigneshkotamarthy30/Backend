package com.hotel.controller;

import com.hotel.dto.RoomTypeDto;
import com.hotel.entity.*;
import com.hotel.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final UserService userService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public AdminController(RoomService roomService, BookingService bookingService, UserService userService,
                           InvoiceService invoiceService, PaymentService paymentService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalRooms", roomService.getActiveRoomList().size());
        response.put("availableRooms", roomService.getActiveRoomList().stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count());
        response.put("occupiedRooms", roomService.getActiveRoomList().stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count());
        response.put("activeBookings", bookingService.getAllBookings(PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements());
        response.put("recordedRevenue", BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<com.hotel.dto.RoomDto>> rooms() {
        return ResponseEntity.ok(roomService.getRooms(PageRequest.of(0, 50)).getContent().stream().map(roomService::toDto).toList());
    }

    @PostMapping("/rooms")
    public ResponseEntity<com.hotel.dto.RoomDto> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomService.toDto(roomService.createRoom(room)));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<com.hotel.dto.RoomDto> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        return ResponseEntity.ok(roomService.toDto(roomService.updateRoom(id, room)));
    }

    @PatchMapping("/rooms/{id}/status")
    public ResponseEntity<com.hotel.dto.RoomDto> updateRoomStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(roomService.toDto(roomService.updateStatus(id, RoomStatus.valueOf(body.get("status")))));
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<Booking>> bookings(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingService.getAllBookings(PageRequest.of(page, size)));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<User>> getCustomers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/bookings/{id}/confirm")
    public ResponseEntity<Booking> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @PatchMapping("/bookings/{id}/check-in")
    public ResponseEntity<Booking> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.checkInBooking(id));
    }

    @PatchMapping("/bookings/{id}/check-out")
    public ResponseEntity<Booking> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.checkOutBooking(id));
    }

    @PatchMapping("/bookings/{id}/cancel")
    public ResponseEntity<Booking> cancel(@PathVariable Long id) {
        User admin = userService.getCurrentUser();
        return ResponseEntity.ok(bookingService.cancelBooking(id, admin));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> invoices() {
        return ResponseEntity.ok(invoiceService.getInvoicesForUser(userService.getCurrentUser()));
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<Map<String, Object>> revenueReport() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", BigDecimal.ZERO);
        response.put("currency", "INR");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/occupancy")
    public ResponseEntity<Map<String, Object>> occupancyReport() {
        Map<String, Object> response = new HashMap<>();
        response.put("occupancyRate", 0.0);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> recordPayment(@RequestBody Map<String, Object> body) {
        Long invoiceId = Long.parseLong(body.get("invoiceId").toString());
        Long userId = Long.parseLong(body.get("userId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String method = body.get("method").toString();
        String notes = body.getOrDefault("notes", "").toString();
        return ResponseEntity.ok(paymentService.recordPayment(invoiceId, userId, amount, method, notes));
    }
}
