-- Insert roles
INSERT INTO roles (id, name) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'ROLE_ADMIN'),
('550e8400-e29b-41d4-a716-446655440002', 'ROLE_MANUFACTURER'),
('550e8400-e29b-41d4-a716-446655440003', 'ROLE_RETAILER'),
('550e8400-e29b-41d4-a716-446655440004', 'ROLE_CUSTOMER');

-- Insert users
INSERT INTO users (id, username, email, phone_number, password, enabled, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440011', 'admin', 'admin@example.com', '+1234567890', '$2a$10$7QJ1y7x9f6Jz7p8h9k0l1e2d3c4b5a6f7g8h9i0j1k2l3m4n5o6p7', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440012', 'manufacturer_user', 'manufacturer@example.com', '+1234567891', '$2a$10$7QJ1y7x9f6Jz7p8h9k0l1e2d3c4b5a6f7g8h9i0j1k2l3m4n5o6p7', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440013', 'retailer_user', 'retailer@example.com', '+1234567892', '$2a$10$7QJ1y7x9f6Jz7p8h9k0l1e2d3c4b5a6f7g8h9i0j1k2l3m4n5o6p7', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440014', 'customer_user', 'customer@example.com', '+1234567893', '$2a$10$7QJ1y7x9f6Jz7p8h9k0l1e2d3c4b5a6f7g8h9i0j1k2l3m4n5o6p7', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES 
('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001'), -- admin -> ROLE_ADMIN
('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440002'), -- manufacturer_user -> ROLE_MANUFACTURER
('550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440003'), -- retailer_user -> ROLE_RETAILER
('550e8400-e29b-41d4-a716-446655440014', '550e8400-e29b-41d4-a716-446655440004'); -- customer_user -> ROLE_CUSTOMER

-- Insert manufacturer profiles
INSERT INTO manufacturer_profiles (id, user_id, company_name, gst_number, business_type, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440012', 'ACME Manufacturing Co.', 'GST123456789', 'Electronics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert retailer profiles
INSERT INTO retailer_profiles (id, user_id, business_name, gst_number, store_type, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440013', 'Best Retail Store', 'GST987654321', 'Electronics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample addresses
INSERT INTO addresses (id, user_id, address_line, city, state, country, pincode, address_type, is_primary, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440041', '550e8400-e29b-41d4-a716-446655440012', '456 Industrial Ave', 'Detroit', 'MI', 'USA', '48201', 'BUSINESS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Manufacturer address
('550e8400-e29b-41d4-a716-446655440042', '550e8400-e29b-41d4-a716-446655440013', '789 Shopping Blvd', 'Los Angeles', 'CA', 'USA', '90001', 'BUSINESS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- Retailer address
('550e8400-e29b-41d4-a716-446655440043', '550e8400-e29b-41d4-a716-446655440014', '123 Main St', 'New York', 'NY', 'USA', '10001', 'SHIPPING', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- Customer address
