CREATE TABLE hotel_application_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    image_data LONGBLOB NOT NULL,
    CONSTRAINT fk_hotel_application_photo_application
        FOREIGN KEY (application_id) REFERENCES hotel_applications(id) ON DELETE CASCADE
);
