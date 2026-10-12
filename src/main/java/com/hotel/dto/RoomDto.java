package com.hotel.dto;

import com.hotel.entity.RoomStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RoomDto {
    private Long id;
    private String roomNumber;
    private RoomTypeDto roomType;
    private RoomStatus status;
    private String title;
    private String description;
    private BigDecimal nightlyRate;
    private int maxGuests;
    private int beds;
    private Integer sizeSqft;
    private String imageUrl;
    private boolean active;
    private List<AmenityDto> amenities = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public RoomTypeDto getRoomType() { return roomType; }
    public void setRoomType(RoomTypeDto roomType) { this.roomType = roomType; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(BigDecimal nightlyRate) { this.nightlyRate = nightlyRate; }
    public int getMaxGuests() { return maxGuests; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }
    public int getBeds() { return beds; }
    public void setBeds(int beds) { this.beds = beds; }
    public Integer getSizeSqft() { return sizeSqft; }
    public void setSizeSqft(Integer sizeSqft) { this.sizeSqft = sizeSqft; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<AmenityDto> getAmenities() { return amenities; }
    public void setAmenities(List<AmenityDto> amenities) { this.amenities = amenities; }
}
