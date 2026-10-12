package com.hotel.repository;

import com.hotel.entity.HotelBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelBookingRepository extends JpaRepository<HotelBooking, Long> {
    @Query("SELECT b FROM HotelBooking b JOIN FETCH b.hotel JOIN FETCH b.guest WHERE b.guest.id = :guestId ORDER BY b.createdAt DESC")
    List<HotelBooking> findByGuestIdWithDetails(@Param("guestId") Long guestId);

    @Query("SELECT b FROM HotelBooking b JOIN FETCH b.hotel h JOIN FETCH b.guest WHERE h.owner.id = :ownerId ORDER BY b.createdAt DESC")
    List<HotelBooking> findByHotelOwnerIdWithDetails(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM HotelBooking b JOIN FETCH b.hotel JOIN FETCH b.guest WHERE b.id = :id AND b.hotel.owner.id = :ownerId")
    Optional<HotelBooking> findByIdAndHotelOwnerIdWithDetails(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
