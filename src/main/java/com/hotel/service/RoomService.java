package com.hotel.service;

import com.hotel.dto.AmenityDto;
import com.hotel.dto.RoomDto;
import com.hotel.entity.Amenity;
import com.hotel.entity.Room;
import com.hotel.entity.RoomStatus;
import com.hotel.entity.RoomType;
import com.hotel.exception.NotFoundException;
import com.hotel.repository.AmenityRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AmenityRepository amenityRepository;

    public RoomService(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository, AmenityRepository amenityRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
    }

    public Page<Room> getRooms(Pageable pageable) {
        return roomRepository.findAllActive(pageable);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Room not found."));
    }

    public Room getRoomDetails(Long id) {
        return roomRepository.findByIdWithDetails(id);
    }

    public List<Room> getActiveRoomList() {
        return roomRepository.findAllActiveWithDetails();
    }

    @Transactional
    public Room createRoom(Room room) {
        if (room.getRoomType() == null || room.getRoomType().getId() == null) {
            throw new IllegalArgumentException("Room type is required.");
        }
        RoomType roomType = roomTypeRepository.findById(room.getRoomType().getId())
            .orElseThrow(() -> new NotFoundException("Room type not found."));
        room.setRoomType(roomType);
        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Long id, Room updated) {
        Room room = getRoomById(id);
        room.setRoomNumber(updated.getRoomNumber());
        room.setTitle(updated.getTitle());
        room.setDescription(updated.getDescription());
        room.setNightlyRate(updated.getNightlyRate());
        room.setMaxGuests(updated.getMaxGuests());
        room.setBeds(updated.getBeds());
        room.setImageUrl(updated.getImageUrl());
        room.setStatus(updated.getStatus());
        room.setActive(updated.isActive());
        if (updated.getRoomType() != null && updated.getRoomType().getId() != null) {
            RoomType roomType = roomTypeRepository.findById(updated.getRoomType().getId())
                .orElseThrow(() -> new NotFoundException("Room type not found."));
            room.setRoomType(roomType);
        }
        return roomRepository.save(room);
    }

    @Transactional
    public Room updateStatus(Long roomId, RoomStatus status) {
        Room room = getRoomById(roomId);
        room.setStatus(status);
        return roomRepository.save(room);
    }

    public RoomDto toDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setStatus(room.getStatus());
        dto.setTitle(room.getTitle());
        dto.setDescription(room.getDescription());
        dto.setNightlyRate(room.getNightlyRate());
        dto.setMaxGuests(room.getMaxGuests());
        dto.setBeds(room.getBeds());
        dto.setSizeSqft(room.getSizeSqft());
        dto.setImageUrl(room.getImageUrl());
        dto.setActive(room.isActive());
        if (room.getRoomType() != null) {
            dto.setRoomType(new com.hotel.dto.RoomTypeDto());
            dto.getRoomType().setId(room.getRoomType().getId());
            dto.getRoomType().setName(room.getRoomType().getName());
            dto.getRoomType().setDescription(room.getRoomType().getDescription());
            dto.getRoomType().setMaxGuests(room.getRoomType().getMaxGuests());
            dto.getRoomType().setBaseRate(room.getRoomType().getBaseRate());
        }
        if (room.getAmenities() != null) {
            dto.setAmenities(room.getAmenities().stream().map(this::toAmenityDto).collect(Collectors.toList()));
        }
        return dto;
    }

    public AmenityDto toAmenityDto(Amenity amenity) {
        AmenityDto dto = new AmenityDto();
        dto.setId(amenity.getId());
        dto.setName(amenity.getName());
        dto.setDescription(amenity.getDescription());
        return dto;
    }

    public void setAmenities(Long roomId, Set<Long> amenityIds) {
        Room room = getRoomById(roomId);
        Set<Amenity> amenities = amenityRepository.findAllById(amenityIds).stream().collect(Collectors.toSet());
        room.setAmenities(amenities);
        roomRepository.save(room);
    }
}
