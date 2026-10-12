CREATE TABLE hotel_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(30) NOT NULL UNIQUE,
    hotel_id BIGINT NOT NULL,
    guest_id BIGINT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guest_count INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hotel_booking_hotel FOREIGN KEY (hotel_id)
        REFERENCES hotel_applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_hotel_booking_guest FOREIGN KEY (guest_id)
        REFERENCES users(id),
    INDEX idx_hotel_bookings_guest (guest_id, created_at),
    INDEX idx_hotel_bookings_hotel_status (hotel_id, status, check_in, check_out)
);
