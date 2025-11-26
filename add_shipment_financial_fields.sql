-- Add financial fields to shipments table
-- Run this SQL in your PostgreSQL database

ALTER TABLE shipments 
ADD COLUMN transportation_cost DECIMAL(10,2) DEFAULT 0.0,
ADD COLUMN other_costs DECIMAL(10,2) DEFAULT 0.0;

-- Update the comment for the table
COMMENT ON COLUMN shipments.transportation_cost IS 'Transportation costs for this shipment';
COMMENT ON COLUMN shipments.other_costs IS 'Other additional costs for this shipment';

-- Verify the changes
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_name = 'shipments' 
ORDER BY ordinal_position;
