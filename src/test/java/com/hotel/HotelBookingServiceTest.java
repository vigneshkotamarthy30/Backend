package com.hotel;

import com.hotel.dto.HotelApplicationRequest;
import com.hotel.dto.HotelBookingRequest;
import com.hotel.entity.HotelApplication;
import com.hotel.entity.HotelBooking;
import com.hotel.entity.HotelBookingStatus;
import com.hotel.entity.User;
import com.hotel.exception.NotFoundException;
import com.hotel.exception.ValidationException;
import com.hotel.service.HotelApplicationService;
import com.hotel.service.HotelBookingService;
import com.hotel.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HotelBookingServiceTest {
    @Autowired
    private HotelBookingService bookingService;

    @Autowired
    private HotelApplicationService hotelService;

    @Autowired
    private UserService userService;

    @Test
    void guestCanRequestBookingAndOnlyOwningPartnerCanAcceptIt() {
        User owner = userService.registerHotelPartner(
            "Hotel", "Owner", "booking-owner@example.com", "owner-password", "1234567890"
        );
        User otherOwner = userService.registerHotelPartner(
            "Other", "Owner", "booking-other@example.com", "other-password", "1234567891"
        );
        User guest = userService.registerUser(
            "Guest", "User", "booking-guest@example.com", "guest-password", "1234567892"
        );
        HotelApplication hotel = hotelService.submitForExistingPartner(
            createHotelRequest(), List.of(new MockMultipartFile("photos", "hotel.jpg", "image/jpeg", new byte[] { 1 })),
            owner
        );
        HotelBookingRequest request = new HotelBookingRequest();
        request.setCheckIn(LocalDate.now().plusDays(10));
        request.setCheckOut(LocalDate.now().plusDays(12));
        request.setGuestCount(2);

        HotelBooking booking = bookingService.create(hotel.getId(), guest.getId(), request);

        assertEquals(HotelBookingStatus.PENDING, booking.getStatus());
        assertEquals(1, bookingService.findForGuest(guest.getId()).size());
        assertEquals(1, bookingService.findForOwner(owner.getId()).size());
        assertThrows(NotFoundException.class, () -> bookingService.decide(booking.getId(), otherOwner.getId(), true));
        assertEquals(HotelBookingStatus.PENDING, booking.getStatus());
        assertEquals(HotelBookingStatus.ACCEPTED, bookingService.decide(booking.getId(), owner.getId(), true).getStatus());
    }

    @Test
    void guestCannotRequestInvalidDates() {
        User owner = userService.registerHotelPartner(
            "Hotel", "Owner", "invalid-date-owner@example.com", "owner-password", "1234567890"
        );
        User guest = userService.registerUser(
            "Guest", "User", "invalid-date-guest@example.com", "guest-password", "1234567892"
        );
        HotelApplication hotel = hotelService.submitForExistingPartner(
            createHotelRequest(), List.of(new MockMultipartFile("photos", "hotel.jpg", "image/jpeg", new byte[] { 1 })),
            owner
        );
        HotelBookingRequest request = new HotelBookingRequest();
        request.setCheckIn(LocalDate.now().plusDays(2));
        request.setCheckOut(LocalDate.now().plusDays(2));
        request.setGuestCount(1);

        assertThrows(ValidationException.class, () -> bookingService.create(hotel.getId(), guest.getId(), request));
    }

    private HotelApplicationRequest createHotelRequest() {
        HotelApplicationRequest request = new HotelApplicationRequest();
        request.setHotelName("Request Hotel");
        request.setCity("Hyderabad");
        request.setState("Telangana");
        request.setAddress("Test address");
        request.setContactName("Hotel Partner");
        request.setEmail("request-hotel@example.com");
        request.setPhone("1234567890");
        request.setRoomCount(5);
        return request;
    }
}
