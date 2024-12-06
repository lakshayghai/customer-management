-- liquibase formatted sql

-- changeset lakshayghai:20241028-1
CREATE TABLE IF NOT EXISTS phone_number (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    phone_type VARCHAR(15) DEFAULT 'MOBILE',
    is_verified BOOLEAN DEFAULT FALSE,
    country_code VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);

-- changeset lakshayghai:20241028-2
CREATE UNIQUE INDEX idx_phone_number_type ON phone_number (phone_number, phone_type);
CREATE INDEX idx_phone_number_customer_id ON phone_number (customer_id);
