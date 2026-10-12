package com.hotel;

import com.hotel.dto.BookingRequest;
import com.hotel.entity.*;
import com.hotel.exception.ConflictException;
import com.hotel.exception.ValidationException;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.repository.UserRepository;
import com.hotel.service.BookingService;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BookingBusinessRulesTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldRejectInvalidDateRange() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("invaliddates@example.com");
        user.setPassword("password");
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        RoomType roomType = new RoomType();
        roomType.setName("Suite-InvalidDates");
        roomType.setBaseRate(new BigDecimal("100"));
        roomType.setMaxGuests(2);
        roomTypeRepository.save(roomType);

        Room room = new Room();
        room.setRoomNumber("200");
        room.setRoomType(roomType);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setTitle("Suite");
        room.setNightlyRate(new BigDecimal("100"));
        room.setMaxGuests(2);
        room.setBeds(1);
        room.setActive(true);
        roomRepository.save(room);

        BookingRequest request = new BookingRequest();
        request.setRoomId(room.getId());
        request.setCheckIn(LocalDate.now().plusDays(3));
        request.setCheckOut(LocalDate.now().plusDays(2));
        request.setGuestCount(1);

        assertThrows(ValidationException.class, () -> bookingService.createBooking(user.getId(), request));
    }

    @Test
    void shouldRejectOverlappingBooking() {
        User user = new User();
        user.setFirstName("A");
        user.setLastName("B");
        user.setEmail("overlap@example.com");
        user.setPassword("password");
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        RoomType roomType = new RoomType();
        roomType.setName("Suite-Overlap");
        roomType.setBaseRate(new BigDecimal("120"));
        roomType.setMaxGuests(2);
        roomTypeRepository.save(roomType);

        Room room = new Room();
        room.setRoomNumber("201");
        room.setRoomType(roomType);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setTitle("Overlap Suite");
        room.setNightlyRate(new BigDecimal("120"));
        room.setMaxGuests(2);
        room.setBeds(1);
        room.setActive(true);
        roomRepository.save(room);

        BookingRequest request = new BookingRequest();
        request.setRoomId(room.getId());
        request.setCheckIn(LocalDate.now().plusDays(5));
        request.setCheckOut(LocalDate.now().plusDays(8));
        request.setGuestCount(2);

        Booking created = bookingService.createBooking(user.getId(), request);
        var userBookings = bookingService.getBookingsForUser(user.getId(), PageRequest.of(0, 10));

        assertEquals(1, userBookings.getTotalElements());
        assertEquals("Overlap Suite", bookingService.toDto(userBookings.getContent().get(0)).getRoomTitle());
        assertEquals(created.getBookingReference(),
            bookingService.toDto(userBookings.getContent().get(0)).getBookingReference());

        BookingRequest second = new BookingRequest();
        second.setRoomId(room.getId());
        second.setCheckIn(LocalDate.now().plusDays(6));
        second.setCheckOut(LocalDate.now().plusDays(9));
        second.setGuestCount(1);

        assertThrows(ConflictException.class, () -> bookingService.createBooking(user.getId(), second));
    }
}
