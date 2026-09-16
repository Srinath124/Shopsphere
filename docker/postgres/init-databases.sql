-- Database initialization script for ShopSphere microservices
CREATE DATABASE auth_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE inventory_db;
CREATE DATABASE notification_db;

GRANT ALL PRIVILEGES ON DATABASE auth_db TO shopsphere;
GRANT ALL PRIVILEGES ON DATABASE product_db TO shopsphere;
GRANT ALL PRIVILEGES ON DATABASE order_db TO shopsphere;
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO shopsphere;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO shopsphere;
