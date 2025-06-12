-- 确保序列存在并正确关联到表
CREATE SEQUENCE IF NOT EXISTS vector_data_id_seq;
ALTER TABLE vector_data ALTER COLUMN id SET DEFAULT nextval('vector_data_id_seq');
ALTER SEQUENCE vector_data_id_seq OWNED BY vector_data.id;

-- 重置序列到当前最大ID + 1
SELECT setval('vector_data_id_seq', COALESCE((SELECT MAX(id) FROM vector_data), 0) + 1, false); 