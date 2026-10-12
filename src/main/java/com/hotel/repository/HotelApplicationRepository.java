package com.hotel.repository;

import com.hotel.entity.HotelApplication;
import com.hotel.entity.HotelApplicationStatus;
import com.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelApplicationRepository extends JpaRepository<HotelApplication, Long> {
    List<HotelApplication> findAllByOrderByHotelNameAsc();
    List<HotelApplication> findByStatusNotOrderByHotelNameAsc(HotelApplicationStatus status);
    List<HotelApplication> findByOwnerOrderByCreatedAtDesc(User owner);
    java.util.Optional<HotelApplication> findByIdAndOwner(Long id, User owner);
}
