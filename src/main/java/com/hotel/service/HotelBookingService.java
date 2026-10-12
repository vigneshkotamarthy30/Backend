package com.hotel.service;

import com.hotel.dto.HotelBookingRequest;
import com.hotel.entity.*;
import com.hotel.exception.NotFoundException;
import com.hotel.exception.ValidationException;
import com.hotel.repository.HotelApplicationRepository;
import com.hotel.repository.HotelBookingRepository;
import com.hotel.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class HotelBookingService {
    private final HotelBookingRepository bookingRepository;
    private final HotelApplicationRepository hotelRepository;
    private final UserRepository userRepository;

    public HotelBookingService(HotelBookingRepository bookingRepository,
                               HotelApplicationRepository hotelRepository,
                               UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public HotelBooking create(@NonNull Long hotelId, @NonNull Long guestId, HotelBookingRequest request) {
        HotelApplication hotel = hotelRepository.findById(hotelId)
            .orElseThrow(() -> new NotFoundException("Hotel not found."));
        if (hotel.getStatus() == HotelApplicationStatus.REJECTED) {
            throw new ValidationException("This hotel is not accepting booking requests.");
        }
        LocalDate today = LocalDate.now();
        if (request.getCheckIn() == null || request.getCheckOut() == null
            || request.getCheckIn().isBefore(today) || !request.getCheckIn().isBefore(request.getCheckOut())) {
            throw new ValidationException("Choose a check-in date from today and a check-out date after check-in.");
        }
        if (request.getGuestCount() < 1) {
            throw new ValidationException("At least one guest is required.");
        }
        User guest = userRepository.findById(guestId)
            .orElseThrow(() -> new NotFoundException("Guest account not found."));
        HotelBooking booking = new HotelBooking();
        booking.setHotel(hotel);
        booking.setGuest(guest);
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setGuestCount(request.getGuestCount());
        booking.setBookingReference("HB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<HotelBooking> findForGuest(@NonNull Long guestId) {
        return bookingRepository.findByGuestIdWithDetails(guestId);
    }

    @Transactional(readOnly = true)
    public List<HotelBooking> findForOwner(@NonNull Long ownerId) {
        return bookingRepository.findByHotelOwnerIdWithDetails(ownerId);
    }

    @Transactional
    public HotelBooking decide(@NonNull Long bookingId, @NonNull Long ownerId, boolean accept) {
        HotelBooking booking = bookingRepository.findByIdAndHotelOwnerIdWithDetails(bookingId, ownerId)
            .orElseThrow(() -> new NotFoundException("Booking request not found in your hotels."));
        if (booking.getStatus() != HotelBookingStatus.PENDING) {
            throw new ValidationException("Only pending booking requests can be accepted or rejected.");
        }
        booking.setStatus(accept ? HotelBookingStatus.ACCEPTED : HotelBookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }
}
