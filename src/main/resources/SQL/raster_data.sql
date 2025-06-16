-- 创建序列（如果不存在）
CREATE SEQUENCE IF NOT EXISTS raster_data_id_seq;

-- 创建表（如果不存在）
CREATE TABLE IF NOT EXISTS raster_data (
    id           BIGINT PRIMARY KEY NOT NULL DEFAULT nextval('raster_data_id_seq'),
    bands        INTEGER,
    created_at   TIMESTAMP(6) WITHOUT TIME ZONE,
    description  VARCHAR(255),
    name         VARCHAR(255) NOT NULL,
    raster_data  OID,
    raster_type  VARCHAR(255),
    resolution   DOUBLE PRECISION,
    updated_at   TIMESTAMP(6) WITHOUT TIME ZONE,
    lo_oid       BIGINT,
    file_size    BIGINT,
    thumbnail    BYTEA
);

-- 重置序列
SELECT setval('raster_data_id_seq', COALESCE((SELECT MAX(id) FROM raster_data), 1), true);

-- ==========================================
-- 按当前 id 顺序重排 id 字段
-- ==========================================
DO $$
DECLARE
    rec    RECORD;
    new_id BIGINT := 1;
BEGIN
    FOR rec IN SELECT id FROM raster_data ORDER BY id LOOP
        UPDATE raster_data SET id = new_id WHERE id = rec.id;
        new_id := new_id + 1;
    END LOOP;
END $$;
