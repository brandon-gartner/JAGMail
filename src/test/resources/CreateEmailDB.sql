DROP DATABASE IF EXISTS EMAILDB;
CREATE DATABASE EMAILDB;

USE EMAILDB;

DROP USER IF EXISTS brandon@localhost;
CREATE USER brandon@'localhost' IDENTIFIED WITH mysql_native_password BY 'dawson!123' REQUIRE NONE;
GRANT ALL ON EMAILDB.* TO brandon@'localhost';

-- This creates a user with access from any IP number except localhost
-- Use only if your MyQL database is on a different host from localhost
-- DROP USER IF EXISTS fish;
-- CREATE USER fish IDENTIFIED WITH mysql_native_password BY 'kfstandard' REQUIRE NONE;
-- GRANT ALL ON AQUARIUM TO fish;

-- This creates a user with access from a specific IP number
-- Preferable to '%'
-- DROP USER IF EXISTS fish@'192.168.0.194';
-- CREATE USER fish@'192.168.0.194' IDENTIFIED WITH mysql_native_password BY 'kfstandard' REQUIRE NONE;
-- GRANT ALL ON AQUARIUM TO fish@'192.168.0.194';

FLUSH PRIVILEGES;