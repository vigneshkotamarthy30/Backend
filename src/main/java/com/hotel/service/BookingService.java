package com.hotel.service;

import com.hotel.dto.BookingDto;
import com.hotel.dto.BookingRequest;
import com.hotel.entity.*;
import com.hotel.exception.ConflictException;
import com.hotel.exception.NotFoundException;
import com.hotel.exception.ValidationException;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          UserRepository userRepository, InvoiceService invoiceService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public Booking createBooking(Long userId, BookingRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found."));
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new NotFoundException("Room not found."));

        if (!room.isActive()) {
            throw new ValidationException("This room is not available for booking.");
        }
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new ValidationException("This room is under maintenance.");
        }
        LocalDate checkIn = request.getCheckIn();
        LocalDate checkOut = request.getCheckOut();
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            throw new ValidationException("Check-in date must be before check-out date.");
        }
        if (request.getGuestCount() > room.getMaxGuests()) {
            throw new ValidationException("Guest count exceeds room capacity.");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(room, checkIn, checkOut);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Selected dates are not available for this room.");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal nightlyRate = room.getNightlyRate() == null ? BigDecimal.ZERO : room.getNightlyRate();
        BigDecimal totalAmount = nightlyRate.multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setGuestCount(request.getGuestCount());
        booking.setNights((int) nights);
        booking.setNightlyRate(nightlyRate);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingReference(generateReference());
        Booking saved = bookingRepository.saveAndFlush(booking);
        invoiceService.createInvoiceForBooking(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Booking> getBookingsForUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found.");
        }
        return bookingRepository.findByUserIdWithDetails(userId, pageable);
    }

    public Booking getBooking(Long bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found."));
    }

    public Booking cancelBooking(Long bookingId, User currentUser) {
        Booking booking = getBooking(bookingId);
        if (!booking.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("You are not allowed to cancel this booking.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new ValidationException("This booking cannot be cancelled.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public Booking confirmBooking(Long bookingId) {
        Booking booking = getBooking(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    public Booking checkInBooking(Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ValidationException("Booking must be confirmed before check-in.");
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
        return bookingRepository.save(booking);
    }

    public Booking checkOutBooking(Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new ValidationException("Booking must be checked in before check-out.");
        }
        booking.setStatus(BookingStatus.CHECKED_OUT);
        return bookingRepository.save(booking);
    }

    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setBookingReference(booking.getBookingReference());
        dto.setUserId(booking.getUser().getId());
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoomTitle(booking.getRoom().getTitle());
        dto.setCheckIn(booking.getCheckIn());
        dto.setCheckOut(booking.getCheckOut());
        dto.setGuestCount(booking.getGuestCount());
        dto.setNights(booking.getNights());
        dto.setNightlyRate(booking.getNightlyRate());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }

    private String generateReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
