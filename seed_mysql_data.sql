-- ==============================================================
-- ASPAS Comprehensive Seed Data Script
-- Run this script to populate the MySQL Database with robust test data.
-- ==============================================================

USE aspas_db;

-- Disable foreign key checks for a clean teardown
SET FOREIGN_KEY_CHECKS = 0;

-- Clean existing data to make script repeatable
TRUNCATE TABLE order_items;
TRUNCATE TABLE order_lists;
TRUNCATE TABLE part_vendor;
TRUNCATE TABLE spare_parts;
TRUNCATE TABLE vendors;
TRUNCATE TABLE storage_racks;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- Insert Storage Racks
-- ==========================================
INSERT INTO storage_racks (rack_id, rack_number, wall_location, max_capacity, created_at, updated_at) VALUES
(1, 101, 'North Aisle, Sec A', 200, NOW(), NOW()),
(2, 102, 'North Aisle, Sec B', 200, NOW(), NOW()),
(3, 103, 'South Aisle, Sec A', 150, NOW(), NOW()),
(4, 104, 'South Aisle, Sec B', 100, NOW(), NOW()),
(5, 105, 'Bulk Storage A', 500, NOW(), NOW());

-- ==========================================
-- Insert Vendors
-- ==========================================
INSERT INTO vendors (vendor_id, vendor_name, vendor_address, contact_number, email, created_at, updated_at) VALUES
(1, 'Bosch Auto Parts', '120 Tech Park, Bangalore', '+91 9876543210', 'sales@bosch.in', NOW(), NOW()),
(2, 'Denso Corporation', 'Sakura Doori, Tokyo', '+81 312345678', 'contact@denso.co.jp', NOW(), NOW()),
(3, 'Valeo', '15 Rue de La, Paris', '+33 123456789', 'global@valeo.com', NOW(), NOW()),
(4, 'Minda Industries', 'Gurgaon, Haryana, India', '+91 9988776655', 'sales@uno-minda.com', NOW(), NOW()),
(5, 'Brembo', '15 Via Brembo, Italy', '+39 035 605111', 'italy@brembo.it', NOW(), NOW());

-- ==========================================
-- Insert Spare Parts
-- ==========================================
INSERT INTO spare_parts (part_id, part_number, part_name, current_quantity, threshold_value, unit_price, size_category, rack_id, created_at, updated_at) VALUES
(1, 'SP-BRK-001', 'Ceramic Brake Pads', 120, 30, 1500.00, 'SMALL', 1, NOW(), NOW()),
(2, 'SP-BRK-002', 'Brake Disc Rotor', 45, 15, 3500.00, 'MEDIUM', 1, NOW(), NOW()),
(3, 'SP-FIL-001', 'Standard Oil Filter', 300, 50, 450.00, 'SMALL', 2, NOW(), NOW()),
(4, 'SP-FIL-002', 'Cabin Air Filter', 150, 40, 600.00, 'SMALL', 2, NOW(), NOW()),
(5, 'SP-LGT-001', 'Halogen Headlamp H4', 80, 20, 850.00, 'SMALL', 3, NOW(), NOW()),
(6, 'SP-LGT-002', 'LED Headlamp Conversion Kit', 25, 10, 4500.00, 'MEDIUM', 3, NOW(), NOW()),
(7, 'SP-ALT-001', 'Alternator 12V 90A', 10, 5, 5200.00, 'LARGE', 4, NOW(), NOW()),
(8, 'SP-BLT-001', 'Timing Belt Kit', 35, 15, 2100.00, 'MEDIUM', 3, NOW(), NOW()),
(9, 'SP-SUS-001', 'Front Shock Absorber', 20, 8, 3200.00, 'LARGE', 5, NOW(), NOW()),
(10, 'SP-CLT-001', 'Clutch Plate Assembly', 15, 5, 4800.00, 'LARGE', 5, NOW(), NOW()),
(11, 'SP-ENG-001', 'Spark Plug Iridium', 400, 100, 350.00, 'SMALL', 2, NOW(), NOW()),
(12, 'SP-BAT-001', 'Car Battery 12V 65Ah', 40, 15, 6500.00, 'LARGE', 5, NOW(), NOW());

-- ==========================================
-- Map Parts to Vendors (part_vendor join table)
-- ==========================================
INSERT INTO part_vendor (part_id, vendor_id, is_primary) VALUES
(1, 1, TRUE), (1, 5, FALSE),      -- SP-BRK-001 by Bosch & Brembo
(2, 5, TRUE),                     -- SP-BRK-002 by Brembo
(3, 1, TRUE), (3, 2, FALSE),      -- SP-FIL-001 by Bosch & Denso
(4, 1, TRUE),                     -- SP-FIL-002 by Bosch
(5, 2, TRUE), (5, 3, FALSE),      -- SP-LGT-001 by Denso & Valeo
(6, 3, TRUE),                     -- SP-LGT-002 by Valeo
(7, 1, TRUE),                     -- SP-ALT-001 by Bosch
(8, 2, TRUE),                     -- SP-BLT-001 by Denso
(9, 4, TRUE),                     -- SP-SUS-001 by Minda
(10, 3, TRUE),                    -- SP-CLT-001 by Valeo
(11, 2, TRUE),                    -- SP-ENG-001 by Denso
(12, 1, TRUE);                    -- SP-BAT-001 by Bosch
