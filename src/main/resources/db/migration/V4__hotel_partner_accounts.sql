ALTER TABLE hotel_applications
    ADD COLUMN owner_id BIGINT NULL,
    ADD CONSTRAINT fk_hotel_application_owner
        FOREIGN KEY (owner_id) REFERENCES users(id);
