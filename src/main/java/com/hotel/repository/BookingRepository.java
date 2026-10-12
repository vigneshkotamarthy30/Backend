package com.hotel.repository;

import com.hotel.entity.Booking;
import com.hotel.entity.BookingStatus;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);
    List<Booking> findByUser(User user);
    Page<Booking> findByUser(User user, Pageable pageable);
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Query(
        value = "SELECT b FROM Booking b JOIN FETCH b.room JOIN FETCH b.user WHERE b.user.id = :userId",
        countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId"
    )
    Page<Booking> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN FETCH b.room r JOIN FETCH b.user u WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM Booking b WHERE b.room = :room AND b.status <> com.hotel.entity.BookingStatus.CANCELLED AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    List<Booking> findOverlappingBookings(@Param("room") Room room,
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut);
}
