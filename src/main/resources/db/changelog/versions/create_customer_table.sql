-- liquibase formatted sql

-- changeset lakshayghai:20241028-1
CREATE TABLE IF NOT EXISTS customer (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- changeset lakshayghai:20241028-2
CREATE INDEX idx_customer_last_name ON customer (last_name);