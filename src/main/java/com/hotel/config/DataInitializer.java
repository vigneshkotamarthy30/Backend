package com.hotel.config;

import com.hotel.entity.*;
import com.hotel.repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class DataInitializer {
    private final UserRepository userRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AmenityRepository amenityRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoomTypeRepository roomTypeRepository,
                           AmenityRepository amenityRepository, RoomRepository roomRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (userRepository.findByEmail("admin@hotel.com").isEmpty()) {
            User admin = new User();
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEmail("admin@hotel.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setPhone("+10000000000");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
        }

        if (roomTypeRepository.count() == 0) {
            RoomType standard = new RoomType();
            standard.setName("Standard");
            standard.setDescription("Comfortable room with essentials.");
            standard.setMaxGuests(2);
            standard.setBaseRate(new BigDecimal("9960.00"));
            roomTypeRepository.save(standard);

            RoomType deluxe = new RoomType();
            deluxe.setName("Deluxe");
            deluxe.setDescription("Larger room with city views.");
            deluxe.setMaxGuests(3);
            deluxe.setBaseRate(new BigDecimal("14940.00"));
            roomTypeRepository.save(deluxe);
        }

        if (amenityRepository.count() == 0) {
            Amenity wifi = new Amenity();
            wifi.setName("Wi-Fi");
            wifi.setDescription("High-speed internet access");
            amenityRepository.save(wifi);

            Amenity breakfast = new Amenity();
            breakfast.setName("Breakfast");
            breakfast.setDescription("Complimentary breakfast");
            amenityRepository.save(breakfast);

            Amenity pool = new Amenity();
            pool.setName("Pool Access");
            pool.setDescription("Access to rooftop pool");
            amenityRepository.save(pool);
        }

        if (roomRepository.count() == 0) {
            RoomType standard = roomTypeRepository.findAll().get(0);
            Amenity wifi = amenityRepository.findAll().get(0);
            Amenity breakfast = amenityRepository.findAll().get(1);

            Room room1 = new Room();
            room1.setRoomNumber("101");
            room1.setRoomType(standard);
            room1.setStatus(RoomStatus.AVAILABLE);
            room1.setTitle("City View Standard Room");
            room1.setDescription("Comfortable room ideal for short stays.");
            room1.setNightlyRate(new BigDecimal("9960.00"));
            room1.setMaxGuests(2);
            room1.setBeds(1);
            room1.setSizeSqft(280);
            room1.setImageUrl("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85");
            room1.setActive(true);
            room1.setAmenities(Set.of(wifi, breakfast));
            roomRepository.save(room1);

            Room room2 = new Room();
            room2.setRoomNumber("205");
            room2.setRoomType(roomTypeRepository.findAll().get(1));
            room2.setStatus(RoomStatus.AVAILABLE);
            room2.setTitle("Deluxe Family Suite");
            room2.setDescription("Spacious room for couples and small families.");
            room2.setNightlyRate(new BigDecimal("14940.00"));
            room2.setMaxGuests(3);
            room2.setBeds(2);
            room2.setSizeSqft(370);
            room2.setImageUrl("https://images.unsplash.com/photo-1566665797739-1674de7a421a");
            room2.setActive(true);
            room2.setAmenities(Set.of(wifi, breakfast));
            roomRepository.save(room2);
        }
    }
}
