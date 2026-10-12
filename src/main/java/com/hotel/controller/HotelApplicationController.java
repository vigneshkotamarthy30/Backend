package com.hotel.controller;

import com.hotel.dto.HotelApplicationRequest;
import com.hotel.dto.HotelApplicationPhotoDto;
import com.hotel.dto.HotelListingDto;
import com.hotel.dto.PartnerHotelDto;
import com.hotel.entity.HotelApplication;
import com.hotel.entity.HotelApplicationStatus;
import com.hotel.entity.User;
import com.hotel.entity.Role;
import com.hotel.security.JwtService;
import com.hotel.service.HotelApplicationService;
import com.hotel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class HotelApplicationController {
    private final HotelApplicationService service;
    private final UserService userService;
    private final JwtService jwtService;

    public HotelApplicationController(HotelApplicationService service, UserService userService, JwtService jwtService) {
        this.service = service;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/hotel-applications")
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody HotelApplicationRequest request) {
        HotelApplication application = service.submit(request);
        return submissionResponse(application);
    }

    @PostMapping(value = "/hotel-applications/with-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> submitWithPhotos(
        @Valid @RequestPart("application") HotelApplicationRequest request,
        @RequestPart("photos") List<MultipartFile> photos) {
        HotelApplicationService.PartnerSubmission submission = service.submitAsPartner(request, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", submission.application().getId(),
            "status", submission.application().getStatus(),
            "message", "Your hotel has been registered and is now visible in the hotel list.",
            "token", jwtService.generateToken(submission.owner().getEmail()),
            "user", userService.toDto(submission.owner())
        ));
    }

    private ResponseEntity<Map<String, Object>> submissionResponse(HotelApplication application) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", application.getId(),
            "status", application.getStatus(),
            "message", "Your property has been submitted for review."
        ));
    }

    @GetMapping("/hotels")
    public List<HotelListingDto> findPublishedHotels() {
        return service.findPublishedHotels();
    }

    @GetMapping("/hotels/{id}/photos/{photoId}")
    public ResponseEntity<byte[]> getPublishedPhoto(@PathVariable Long id, @PathVariable Long photoId) {
        var photo = service.getPublishedPhoto(id, photoId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(Objects.requireNonNull(photo.getContentType())))
            .body(photo.getImageData());
    }

    @GetMapping("/partner/hotel-applications/mine")
    @PreAuthorize("hasRole('HOTEL_PARTNER')")
    public List<PartnerHotelDto> findMyHotels() {
        User owner = userService.getCurrentUser();
        if (owner.getRole() != Role.HOTEL_PARTNER) {
            throw new IllegalArgumentException("Only hotel partner accounts can view partner registrations.");
        }
        return service.findForOwner(owner);
    }

    @PostMapping(value = "/partner/hotels", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('HOTEL_PARTNER')")
    public ResponseEntity<Map<String, Object>> addHotelForCurrentPartner(
        @Valid @RequestPart("application") HotelApplicationRequest request,
        @RequestPart("photos") List<MultipartFile> photos) {
        HotelApplication application = service.submitForExistingPartner(request, photos, userService.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", application.getId(),
            "status", application.getStatus(),
            "message", "Your hotel has been added and is now visible in the hotel list."
        ));
    }

    @DeleteMapping("/partner/hotels/{id}")
    @PreAuthorize("hasRole('HOTEL_PARTNER')")
    public ResponseEntity<Void> removePartnerHotel(@PathVariable Long id) {
        service.deleteForOwner(id, userService.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/hotel-applications")
    @PreAuthorize("hasRole('ADMIN')")
    public List<HotelApplication> findAll() {
        return service.findAll();
    }

    @DeleteMapping("/admin/hotel-applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/hotel-applications/{id}/photos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<HotelApplicationPhotoDto> getPhotoDetails(@PathVariable Long id) {
        return service.findPhotoDetails(id);
    }

    @GetMapping("/admin/hotel-applications/{id}/photos/{photoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id, @PathVariable Long photoId) {
        var photo = service.getPhoto(id, photoId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(Objects.requireNonNull(photo.getContentType())))
            .body(photo.getImageData());
    }

    @PatchMapping("/admin/hotel-applications/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public HotelApplication updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        HotelApplicationStatus status;
        try {
            status = HotelApplicationStatus.valueOf(body.getOrDefault("status", "").toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Status must be PENDING, APPROVED, or REJECTED.");
        }
        return service.updateStatus(id, status);
    }
}
