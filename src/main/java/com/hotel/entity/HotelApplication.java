package com.hotel.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotel_applications")
public class HotelApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_name", nullable = false, length = 150)
    private String hotelName;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "contact_name", nullable = false, length = 150)
    private String contactName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "room_count", nullable = false)
    private int roomCount;

    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HotelApplicationStatus status = HotelApplicationStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @JsonIgnore
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelApplicationPhoto> photos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public HotelApplicationStatus getStatus() { return status; }
    public void setStatus(HotelApplicationStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public List<HotelApplicationPhoto> getPhotos() { return photos; }
}
