package com.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.dto.HotelApplicationRequest;
import com.hotel.entity.HotelApplication;
import com.hotel.entity.HotelApplicationStatus;
import com.hotel.entity.User;
import com.hotel.exception.NotFoundException;
import com.hotel.security.JwtService;
import com.hotel.repository.HotelApplicationRepository;
import com.hotel.repository.HotelApplicationPhotoRepository;
import com.hotel.service.HotelApplicationService;
import com.hotel.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HotelApplicationServiceTest {

    @Autowired
    private HotelApplicationService service;

    @Autowired
    private HotelApplicationRepository repository;

    @Autowired
    private HotelApplicationPhotoRepository photoRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldAcceptPublicMultipartHotelRegistration() throws Exception {
        HotelApplicationRequest request = createRequest();
        MockMultipartFile application = new MockMultipartFile(
            "application", "application.json", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile photo = new MockMultipartFile(
            "photos", "hotel.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] { 1, 2, 3 }
        );

        var response = mockMvc.perform(multipart("/api/hotel-applications/with-photos").file(application).file(photo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.user.role").value("HOTEL_PARTNER"))
            .andReturn();
        String responseBody = response.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        HotelApplication saved = repository.findAll().stream()
            .filter(item -> item.getHotelName().equals("Test Hotel"))
            .findFirst()
            .orElseThrow();
        assertEquals(1, service.findPhotoDetails(saved.getId()).size());
        assertEquals("Test Hotel", service.findPublishedHotels().get(0).hotelName());
        mockMvc.perform(get("/api/hotels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].hotelName").value("Test Hotel"));
        mockMvc.perform(get("/api/partner/hotel-applications/mine")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].hotelName").value("Test Hotel"));

        Long photoId = service.findPhotoDetails(saved.getId()).get(0).id();
        service.updateStatus(saved.getId(), HotelApplicationStatus.APPROVED);
        assertEquals("Test Hotel", service.findPublishedHotels().get(0).hotelName());
        mockMvc.perform(get("/api/hotels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].hotelName").value("Test Hotel"));
        mockMvc.perform(get("/api/hotels/{id}/photos/{photoId}", saved.getId(), photoId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes(new byte[] { 1, 2, 3 }));
        service.updateStatus(saved.getId(), HotelApplicationStatus.REJECTED);
        assertTrue(service.findPublishedHotels().isEmpty());
    }

    @Test
    void shouldSaveHotelApplicationAndPhotosAndRemoveThemFromDatabase() {
        HotelApplicationRequest request = createRequest();
        MockMultipartFile photo = new MockMultipartFile(
            "photos", "hotel.jpg", "image/jpeg", new byte[] { 1, 2, 3 }
        );
        HotelApplication saved = service.submit(request, java.util.List.of(photo));

        assertTrue(repository.findById(saved.getId()).isPresent());
        assertEquals("partner@example.com", saved.getEmail());
        assertNotNull(saved.getCreatedAt());
        Long photoId = saved.getPhotos().get(0).getId();
        assertEquals("hotel.jpg", service.findPhotoDetails(saved.getId()).get(0).originalName());
        assertArrayEquals(new byte[] { 1, 2, 3 }, service.getPhoto(saved.getId(), photoId).getImageData());

        service.delete(saved.getId());

        assertFalse(repository.findById(saved.getId()).isPresent());
        assertFalse(photoRepository.findById(photoId).isPresent());
        assertThrows(NotFoundException.class, () -> service.delete(saved.getId()));
    }

    @Test
    void partnerCanRemoveOwnHotelButNotAnotherPartnersHotel() throws Exception {
        User owner = userService.registerHotelPartner(
            "Hotel", "Owner", "owner@example.com", "owner-password", "1234567890"
        );
        User otherOwner = userService.registerHotelPartner(
            "Other", "Owner", "other@example.com", "other-password", "1234567891"
        );
        MockMultipartFile photo = new MockMultipartFile(
            "photos", "hotel.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] { 1, 2, 3 }
        );
        HotelApplication saved = service.submitForExistingPartner(createRequest(), java.util.List.of(photo), owner);
        Long photoId = service.findPhotoDetails(saved.getId()).get(0).id();

        mockMvc.perform(delete("/api/partner/hotels/{id}", saved.getId())
                .header("Authorization", "Bearer " + jwtService.generateToken(otherOwner.getEmail())))
            .andExpect(status().isNotFound());
        assertTrue(repository.findById(saved.getId()).isPresent());

        mockMvc.perform(delete("/api/partner/hotels/{id}", saved.getId())
                .header("Authorization", "Bearer " + jwtService.generateToken(owner.getEmail())))
            .andExpect(status().isNoContent());
        assertFalse(repository.findById(saved.getId()).isPresent());
        assertFalse(photoRepository.findById(photoId).isPresent());
        assertTrue(service.findPublishedHotels().isEmpty());
    }

    private HotelApplicationRequest createRequest() {
        HotelApplicationRequest request = new HotelApplicationRequest();
        request.setHotelName("Test Hotel");
        request.setCity("Hyderabad");
        request.setState("Telangana");
        request.setAddress("Test address");
        request.setContactName("Test Partner");
        request.setEmail("PARTNER@EXAMPLE.COM");
        request.setPhone("1234567890");
        request.setRoomCount(6);
        request.setPassword("partner-password");
        return request;
    }
}
