CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_data (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    geometry geometry(Geometry, 4326),
    properties JSONB
);

CREATE TABLE raster_data (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(255),
    raster_type VARCHAR(50),
    resolution DOUBLE PRECISION,
    bands INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE IF NOT EXISTS raster_data_id_seq;
ALTER TABLE raster_data ALTER COLUMN id SET DEFAULT nextval('raster_data_id_seq');
ALTER SEQUENCE raster_data_id_seq OWNED BY raster_data.id;

-- 创建导出目录表
CREATE TABLE IF NOT EXISTS export_directories (
    id SERIAL PRIMARY KEY,
    directory_path VARCHAR(255) NOT NULL UNIQUE,
    last_used TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
); 