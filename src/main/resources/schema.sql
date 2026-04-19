-- ==============================================================
-- ASPAS - MySQL Schema (managed via phpMyAdmin)
-- ==============================================================
-- HOW TO USE:
--   Option A: Run this script manually in phpMyAdmin
--   Option B: Set spring.sql.init.mode=always in application.properties
--             (Spring Boot runs it on startup automatically)
-- ==============================================================

CREATE DATABASE IF NOT EXISTS aspas_db;
USE aspas_db;

-- =========================
-- TABLE: storage_racks
-- Maps to: Class Diagram → StorageRack
-- =========================
CREATE TABLE IF NOT EXISTS storage_racks (
    rack_id        INT AUTO_INCREMENT PRIMARY KEY,
    rack_number    INT NOT NULL UNIQUE,
    wall_location  VARCHAR(50) NOT NULL,
    max_capacity   INT NOT NULL DEFAULT 100,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- =========================
-- TABLE: vendors
-- Maps to: Class Diagram → Vendor
-- Maps to: DFD → D3 Vendor Directory
-- =========================
CREATE TABLE IF NOT EXISTS vendors (
    vendor_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name     VARCHAR(100) NOT NULL,
    vendor_address  VARCHAR(255) NOT NULL,
    contact_number  VARCHAR(20),
    email           VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- =========================
-- TABLE: spare_parts
-- Maps to: Class Diagram → SparePart
-- Maps to: DFD → D1 Inventory File
-- =========================
CREATE TABLE IF NOT EXISTS spare_parts (
    part_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_number     VARCHAR(50) NOT NULL UNIQUE,
    part_name       VARCHAR(100) NOT NULL,
    current_qty     INT NOT NULL DEFAULT 0,
    threshold_val   INT NOT NULL DEFAULT 0,
    unit_price      DECIMAL(10,2) NOT NULL,
    size_category   VARCHAR(20) DEFAULT 'MEDIUM',
    rack_id         INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_part_rack
        FOREIGN KEY (rack_id) REFERENCES storage_racks(rack_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;


-- =========================
-- JOIN TABLE: part_vendor
-- Maps to: Class Diagram → SparePart *──1..* Vendor
-- A part can be supplied by multiple vendors
-- A vendor can supply multiple parts
-- =========================
CREATE TABLE IF NOT EXISTS part_vendor (
    part_id     BIGINT NOT NULL,
    vendor_id   BIGINT NOT NULL,
    is_primary  BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (part_id, vendor_id),

    CONSTRAINT fk_pv_part
        FOREIGN KEY (part_id) REFERENCES spare_parts(part_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pv_vendor
        FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;


-- =========================
-- TABLE: order_lists
-- Maps to: Class Diagram → OrderList
-- Composition parent (items cascade-delete with list)
-- =========================
CREATE TABLE IF NOT EXISTS order_lists (
    order_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_date  DATE NOT NULL,
    vendor_id   BIGINT NULL,
    total_items INT DEFAULT 0,
    is_printed  BOOLEAN DEFAULT FALSE,
    print_text  TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ol_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id)
) ENGINE=InnoDB;


-- =========================
-- TABLE: order_items
-- Maps to: Class Diagram → OrderItem
-- Composition child → ON DELETE CASCADE
-- Each row = one line on the printed order
-- =========================
CREATE TABLE IF NOT EXISTS order_items (
    item_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    part_id         BIGINT NOT NULL,
    vendor_id       BIGINT NOT NULL,
    part_number     VARCHAR(50) NOT NULL,
    part_name       VARCHAR(100),
    required_qty    INT NOT NULL,
    fulfilled       BOOLEAN NOT NULL DEFAULT FALSE,
    vendor_name     VARCHAR(100),
    vendor_address  VARCHAR(255) NOT NULL,

    CONSTRAINT fk_oi_order
        FOREIGN KEY (order_id) REFERENCES order_lists(order_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_oi_part
        FOREIGN KEY (part_id) REFERENCES spare_parts(part_id),

    CONSTRAINT fk_oi_vendor
        FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id)
) ENGINE=InnoDB;


-- =========================
-- SEED DATA (Optional - for testing)
-- =========================

-- Sample Racks
INSERT IGNORE INTO storage_racks (rack_number, wall_location, max_capacity)
VALUES
    (1, 'North Wall - Section A', 50),
    (2, 'North Wall - Section B', 75),
    (3, 'East Wall - Section A', 60),
    (4, 'East Wall - Section B', 40),
    (5, 'South Wall - Section A', 100);

-- Sample Vendors
INSERT IGNORE INTO vendors (vendor_name, vendor_address, contact_number, email)
VALUES
    ('Bosch Auto Parts', '123 Industrial Area, Berlin, Germany', '+49-30-12345', 'orders@bosch-auto.com'),
    ('Denso Corporation', '1-1 Showa-cho, Kariya, Aichi, Japan', '+81-566-25-5511', 'supply@denso.com'),
    ('Valeo India Pvt Ltd', 'Plot 56, SIPCOT IT Park, Chennai, India', '+91-44-66789000', 'vendor@valeo.in'),
    ('Mann+Hummel', '45 Filter Street, Ludwigsburg, Germany', '+49-7141-980', 'parts@mann-hummel.com'),
    ('Brembo SpA', 'Via Brembo 25, Bergamo, Italy', '+39-035-605111', 'orders@brembo.it');

-- Sample Spare Parts
INSERT IGNORE INTO spare_parts (part_number, part_name, current_qty, threshold_val, unit_price, size_category, rack_id)
VALUES
    ('SP-BRK-001', 'Brake Pad - Front (Ceramic)', 25, 10, 450.00, 'SMALL', 1),
    ('SP-BRK-002', 'Brake Disc - Front', 12, 6, 1200.00, 'MEDIUM', 1),
    ('SP-FLT-001', 'Oil Filter - Standard', 40, 20, 180.00, 'SMALL', 2),
    ('SP-FLT-002', 'Air Filter - Panel Type', 30, 15, 250.00, 'MEDIUM', 2),
    ('SP-ENG-001', 'Spark Plug - Iridium', 60, 30, 320.00, 'SMALL', 3),
    ('SP-ENG-002', 'Timing Belt Kit', 8, 4, 2800.00, 'LARGE', 3),
    ('SP-SUS-001', 'Shock Absorber - Front', 15, 7, 1800.00, 'LARGE', 4),
    ('SP-ELC-001', 'Alternator 12V', 6, 3, 4500.00, 'LARGE', 5),
    ('SP-CLT-001', 'Clutch Plate Kit', 10, 5, 3200.00, 'MEDIUM', 4),
    ('SP-COL-001', 'Coolant Hose Set', 20, 10, 650.00, 'SMALL', 5);

-- Link Parts to Vendors (Many-to-Many)
INSERT IGNORE INTO part_vendor (part_id, vendor_id, is_primary)
VALUES
    (1, 5, TRUE),    -- Brake Pad → Brembo (primary)
    (1, 1, FALSE),   -- Brake Pad → Bosch (secondary)
    (2, 5, TRUE),    -- Brake Disc → Brembo
    (3, 4, TRUE),    -- Oil Filter → Mann+Hummel
    (4, 4, TRUE),    -- Air Filter → Mann+Hummel
    (5, 2, TRUE),    -- Spark Plug → Denso
    (6, 1, TRUE),    -- Timing Belt → Bosch
    (7, 3, TRUE),    -- Shock Absorber → Valeo
    (8, 1, TRUE),    -- Alternator → Bosch
    (8, 2, FALSE),   -- Alternator → Denso (secondary)
    (9, 3, TRUE),    -- Clutch Plate → Valeo
    (10, 3, TRUE);   -- Coolant Hose → Valeo
    