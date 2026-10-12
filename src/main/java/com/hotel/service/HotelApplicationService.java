package com.hotel.service;

import com.hotel.dto.HotelApplicationRequest;
import com.hotel.dto.HotelApplicationPhotoDto;
import com.hotel.dto.HotelListingDto;
import com.hotel.dto.PartnerHotelDto;
import com.hotel.entity.HotelApplication;
import com.hotel.entity.HotelApplicationPhoto;
import com.hotel.entity.HotelApplicationStatus;
import com.hotel.entity.User;
import com.hotel.exception.NotFoundException;
import com.hotel.repository.HotelApplicationRepository;
import com.hotel.repository.HotelApplicationPhotoRepository;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class HotelApplicationService {
    private final HotelApplicationRepository repository;
    private final HotelApplicationPhotoRepository photoRepository;
    private final UserService userService;

    public HotelApplicationService(HotelApplicationRepository repository, HotelApplicationPhotoRepository photoRepository,
                                   UserService userService) {
        this.repository = repository;
        this.photoRepository = photoRepository;
        this.userService = userService;
    }

    @Transactional
    public HotelApplication submit(HotelApplicationRequest request) {
        return submit(request, List.of());
    }

    @Transactional
    public HotelApplication submit(HotelApplicationRequest request, List<MultipartFile> photos) {
        return submit(request, photos, null);
    }

    @Transactional
    public PartnerSubmission submitAsPartner(HotelApplicationRequest request, List<MultipartFile> photos) {
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
        String[] contactName = request.getContactName().trim().split("\\s+", 2);
        String firstName = contactName[0];
        String lastName = contactName.length > 1 ? contactName[1] : "Partner";
        User owner = userService.registerHotelPartner(
            firstName, lastName, request.getEmail(), request.getPassword(), request.getPhone()
        );
        HotelApplication application = submit(request, photos, owner);
        return new PartnerSubmission(application, owner);
    }

    @Transactional
    public HotelApplication submitForExistingPartner(HotelApplicationRequest request, List<MultipartFile> photos, User owner) {
        return submit(request, photos, owner);
    }

    private HotelApplication submit(HotelApplicationRequest request, List<MultipartFile> photos, User owner) {
        HotelApplication application = new HotelApplication();
        application.setHotelName(request.getHotelName().trim());
        application.setCity(request.getCity().trim());
        application.setState(request.getState().trim());
        application.setAddress(request.getAddress().trim());
        application.setContactName(request.getContactName().trim());
        application.setEmail(request.getEmail().trim().toLowerCase());
        application.setPhone(request.getPhone().trim());
        application.setRoomCount(request.getRoomCount());
        application.setWebsite(request.getWebsite() == null ? null : request.getWebsite().trim());
        application.setMessage(request.getMessage() == null ? null : request.getMessage().trim());
        application.setOwner(owner);
        HotelApplication saved = repository.save(application);
        for (MultipartFile photo : photos) {
            if (photo.isEmpty()) {
                throw new IllegalArgumentException("Selected hotel photos cannot be empty.");
            }
            if (photo.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Each hotel photo must be 10 MB or smaller.");
            }
            String contentType = photo.getContentType();
            if (contentType == null || !List.of(
                MediaType.IMAGE_JPEG_VALUE,
                MediaType.IMAGE_PNG_VALUE,
                "image/webp",
                "image/gif"
            ).contains(contentType)) {
                throw new IllegalArgumentException("Hotel photos must be JPEG, PNG, WebP, or GIF images.");
            }
            try {
                HotelApplicationPhoto savedPhoto = new HotelApplicationPhoto();
                savedPhoto.setApplication(saved);
                String originalName = photo.getOriginalFilename();
                savedPhoto.setOriginalName(originalName == null ? "hotel-photo" : originalName);
                savedPhoto.setContentType(contentType);
                savedPhoto.setImageData(photo.getBytes());
                photoRepository.save(savedPhoto);
                saved.getPhotos().add(savedPhoto);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read one of the selected hotel photos.", ex);
            }
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<HotelApplication> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<HotelApplicationPhotoDto> findPhotoDetails(@NonNull Long applicationId) {
        if (!repository.existsById(applicationId)) {
            throw new NotFoundException("Hotel application not found.");
        }
        return photoRepository.findPhotoDetailsByApplicationId(applicationId);
    }

    @Transactional(readOnly = true)
    public List<HotelListingDto> findPublishedHotels() {
        return repository.findByStatusNotOrderByHotelNameAsc(HotelApplicationStatus.REJECTED).stream()
            .map(hotel -> new HotelListingDto(
                hotel.getId(),
                hotel.getHotelName(),
                hotel.getCity(),
                hotel.getState(),
                hotel.getAddress(),
                hotel.getRoomCount(),
                hotel.getWebsite(),
                photoRepository.findPhotoDetailsByApplicationId(hotel.getId())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PartnerHotelDto> findForOwner(User owner) {
        return repository.findByOwnerOrderByCreatedAtDesc(owner).stream()
            .map(hotel -> new PartnerHotelDto(
                hotel.getId(),
                hotel.getHotelName(),
                hotel.getCity(),
                hotel.getState(),
                hotel.getRoomCount(),
                hotel.getStatus()
            ))
            .toList();
    }

    @Transactional
    public void delete(@NonNull Long id) {
        HotelApplication application = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Hotel application not found."));
        repository.delete(application);
    }

    @Transactional
    public void deleteForOwner(@NonNull Long id, User owner) {
        HotelApplication application = repository.findByIdAndOwner(id, owner)
            .orElseThrow(() -> new NotFoundException("Hotel not found in your registrations."));
        repository.delete(application);
    }

    @Transactional(readOnly = true)
    public HotelApplicationPhoto getPhoto(@NonNull Long applicationId, @NonNull Long photoId) {
        return photoRepository.findByIdAndApplicationId(photoId, applicationId)
            .orElseThrow(() -> new NotFoundException("Hotel application photo not found."));
    }

    @Transactional(readOnly = true)
    public HotelApplicationPhoto getPublishedPhoto(@NonNull Long applicationId, @NonNull Long photoId) {
        HotelApplication hotel = repository.findById(applicationId)
            .orElseThrow(() -> new NotFoundException("Hotel not found."));
        return photoRepository.findByIdAndApplicationId(photoId, hotel.getId())
            .orElseThrow(() -> new NotFoundException("Hotel photo not found."));
    }

    @Transactional
    public HotelApplication updateStatus(@NonNull Long id, HotelApplicationStatus status) {
        HotelApplication application = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Hotel application not found."));
        application.setStatus(status);
        return application;
    }

    public record PartnerSubmission(HotelApplication application, User owner) {}
}
