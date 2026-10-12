package com.hotel.repository;

import com.hotel.dto.HotelApplicationPhotoDto;
import com.hotel.entity.HotelApplicationPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface HotelApplicationPhotoRepository extends JpaRepository<HotelApplicationPhoto, Long> {
    Optional<HotelApplicationPhoto> findByIdAndApplicationId(Long id, Long applicationId);

    @Query("SELECT new com.hotel.dto.HotelApplicationPhotoDto(p.id, p.originalName) " +
        "FROM HotelApplicationPhoto p WHERE p.application.id = :applicationId")
    List<HotelApplicationPhotoDto> findPhotoDetailsByApplicationId(@Param("applicationId") Long applicationId);
}
