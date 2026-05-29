-- SQL script to initialize the database for the lab5-prog project
-- Creates tables: users, coordinates, locations, persons
-- Uses sequences for auto-generating IDs

-- Sequence for users
CREATE SEQUENCE IF NOT EXISTS user_id_seq START 1;

-- Sequence for coordinates
CREATE SEQUENCE IF NOT EXISTS coordinates_id_seq START 1;

-- Sequence for locations
CREATE SEQUENCE IF NOT EXISTS location_id_seq START 1;

-- Sequence for persons
CREATE SEQUENCE IF NOT EXISTS person_id_seq START 1;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY DEFAULT nextval('user_id_seq'),
    login VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(512) NOT NULL
);

-- Coordinates table
CREATE TABLE IF NOT EXISTS coordinates (
    id BIGINT PRIMARY KEY DEFAULT nextval('coordinates_id_seq'),
    x DOUBLE PRECISION NOT NULL,
    y BIGINT NOT NULL
);

-- Locations table
CREATE TABLE IF NOT EXISTS locations (
    id BIGINT PRIMARY KEY DEFAULT nextval('location_id_seq'),
    name VARCHAR(255),
    x DOUBLE PRECISION NOT NULL,
    y BIGINT NOT NULL,
    z DOUBLE PRECISION NOT NULL
);

-- Persons table
CREATE TABLE IF NOT EXISTS persons (
    id BIGINT PRIMARY KEY DEFAULT nextval('person_id_seq'),
    name VARCHAR(255) NOT NULL,
    coordinates_id BIGINT REFERENCES coordinates(id) ON DELETE CASCADE,
    creation_date TIMESTAMP NOT NULL,
    height REAL NOT NULL CHECK (height > 0),
    birthday DATE,
    passport_id VARCHAR(255) UNIQUE,
    eye_color VARCHAR(50),
    location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
    owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_persons_owner_id ON persons(owner_id);
CREATE INDEX IF NOT EXISTS idx_persons_passport_id ON persons(passport_id);