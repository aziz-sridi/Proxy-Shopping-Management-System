-- Migration script to add Platform support to existing database
-- Run this if you have an existing database with orders

-- Add platform column to orders table
ALTER TABLE orders ADD COLUMN platform VARCHAR(20) CHECK (platform IN ('Shein','Temu','AliExpress','Alibaba','Other')) DEFAULT 'Other';

-- Update any existing orders to have 'Other' as platform if NULL
UPDATE orders SET platform = 'Other' WHERE platform IS NULL;

-- Verify the migration
SELECT COUNT(*) as total_orders, platform, COUNT(*) as count_per_platform 
FROM orders 
GROUP BY platform
ORDER BY platform;
