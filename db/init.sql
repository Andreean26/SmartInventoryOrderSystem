-- =====================================================
-- Smart Inventory Order System - Database Init Script
-- =====================================================
-- This script is automatically executed by MySQL Docker
-- container on first startup (when volume is empty).
-- =====================================================

-- Create database if not exists (safety net)
CREATE DATABASE IF NOT EXISTS smart_inventory_prod;
USE smart_inventory_prod;

-- =====================================================
-- 1. CUSTOMER TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    membership_level VARCHAR(255) NOT NULL DEFAULT 'REGULAR',
    total_spent DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    active BIT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. PRODUCT TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    active BIT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. ORDERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    discount_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'CREATED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY fk_orders_customer (customer_id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. ORDER_ITEMS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(19, 2) NOT NULL,
    PRIMARY KEY (id),
    KEY fk_order_items_order (order_id),
    KEY fk_order_items_product (product_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
