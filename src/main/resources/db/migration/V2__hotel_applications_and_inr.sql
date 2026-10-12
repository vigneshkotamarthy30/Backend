CREATE TABLE hotel_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    contact_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    room_count INT NOT NULL,
    website VARCHAR(255),
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Existing project prices were seeded in USD; convert persisted monetary values at the
-- original configuration's reference rate before switching the application currency to INR.
UPDATE room_types SET base_rate = ROUND(base_rate * 83, 2);
UPDATE rooms SET nightly_rate = ROUND(nightly_rate * 83, 2);
UPDATE bookings
SET nightly_rate = ROUND(nightly_rate * 83, 2),
    total_amount = ROUND(total_amount * 83, 2);
UPDATE invoices
SET subtotal = ROUND(subtotal * 83, 2),
    tax_amount = ROUND(tax_amount * 83, 2),
    additional_charges = ROUND(additional_charges * 83, 2),
    total_amount = ROUND(total_amount * 83, 2);
UPDATE payments SET amount = ROUND(amount * 83, 2);
