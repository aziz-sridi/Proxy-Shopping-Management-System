-- ============================
-- Table: clients
-- ============================
CREATE TABLE clients (
    client_id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    source VARCHAR(20) CHECK (source IN ('facebook','instagram','whatsapp')),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- Table: currency_rates
-- ============================
CREATE TABLE currency_rates (
    rate_id SERIAL PRIMARY KEY,
    base_currency VARCHAR(10) NOT NULL,
    target_currency VARCHAR(10) NOT NULL,
    original_rate DECIMAL(10,4) NOT NULL,     -- actual market rate
    custom_rate DECIMAL(10,4) NOT NULL,       -- rate you use to calculate profit
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- Table: shipments
-- ============================
CREATE TABLE shipments (
    shipment_id SERIAL PRIMARY KEY,
    batch_name VARCHAR(100) NOT NULL,
    departure_country VARCHAR(100) DEFAULT 'France',
    arrival_country VARCHAR(100) DEFAULT 'Tunisia',
    shipment_cost DECIMAL(10,2) NOT NULL,      -- total cost for this batch
    departure_date DATE,
    arrival_date DATE,
    status VARCHAR(20) CHECK (status IN ('pending','in_transit','arrived','distributed')) DEFAULT 'pending'
);

-- ============================
-- Table: delivery_options
-- ============================
CREATE TABLE delivery_options (
    delivery_option_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,               -- e.g. "RapidEx", "Colis Tunisie"
    description TEXT,
    contact_info VARCHAR(150)
);

-- ============================
-- Table: orders
-- ============================
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    client_id INT NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    shipment_id INT REFERENCES shipments(shipment_id) ON DELETE SET NULL,
    delivery_option_id INT REFERENCES delivery_options(delivery_option_id) ON DELETE SET NULL,
    
    product_link TEXT NOT NULL,
    product_size VARCHAR(20),
    quantity INT DEFAULT 1,
    
    original_price DECIMAL(10,2) NOT NULL,    -- product price in base currency
    selling_price DECIMAL(10,2) NOT NULL,     -- calculated using custom rate
    
    platform VARCHAR(20) CHECK (platform IN ('Shein','Temu','AliExpress','Alibaba','Other')) DEFAULT 'Other',
    
    payment_type VARCHAR(20) CHECK (payment_type IN ('Deposit','Full','On Delivery')) DEFAULT 'On Delivery',
    payment_status VARCHAR(20) CHECK (payment_status IN ('Unpaid','Partial','Paid')) DEFAULT 'Unpaid',
    
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- ============================
-- Table: payments
-- ============================
CREATE TABLE payments (
    payment_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    username VARCHAR(100),
    payment_method VARCHAR(50),
    comment TEXT
);

-- ============================
-- Table: profits
-- ============================
CREATE TABLE profits (
    profit_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    original_rate DECIMAL(10,4) NOT NULL,
    custom_rate DECIMAL(10,4) NOT NULL,
    shipment_cost DECIMAL(10,2) DEFAULT 0,
    calculated_profit DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
