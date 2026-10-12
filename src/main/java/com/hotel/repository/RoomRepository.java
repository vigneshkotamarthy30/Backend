package com.hotel.repository;

import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.roomType LEFT JOIN FETCH r.amenities WHERE r.active = true")
    List<Room> findAllActiveWithDetails();

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.roomType LEFT JOIN FETCH r.amenities WHERE r.id = :id")
    Room findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.roomType LEFT JOIN FETCH r.amenities WHERE r.active = true AND r.status = :status")
    Page<Room> findByStatus(@Param("status") RoomStatus status, Pageable pageable);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.roomType LEFT JOIN FETCH r.amenities WHERE r.active = true")
    Page<Room> findAllActive(Pageable pageable);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.roomType LEFT JOIN FETCH r.amenities WHERE r.active = true AND r.maxGuests >= :guestCount")
    List<Room> findAvailableForGuests(@Param("guestCount") int guestCount);
}
