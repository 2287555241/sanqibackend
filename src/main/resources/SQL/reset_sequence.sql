-- 重置vector_data表的ID序列
-- 此脚本用于手动重置序列，可在PostgreSQL客户端中执行

-- 清空表并重置序列
TRUNCATE TABLE vector_data RESTART IDENTITY CASCADE;

-- 确保序列正确设置
DO $$
DECLARE
    seq_name TEXT;
BEGIN
    -- 获取序列名称
    SELECT pg_get_serial_sequence('vector_data', 'id') INTO seq_name;
    RAISE NOTICE '找到序列名: %', seq_name;
    
    IF seq_name IS NULL THEN
        RAISE EXCEPTION '未找到vector_data表的ID序列';
    END IF;
    
    -- 重置序列
    EXECUTE 'ALTER SEQUENCE ' || seq_name || ' RESTART WITH 1';
    RAISE NOTICE '已重置序列到1';
    
    -- 验证序列值
    EXECUTE 'SELECT setval(''' || seq_name || ''', 0, false)';
    RAISE NOTICE '已设置序列的下一个值为1';
END $$;

-- 验证序列状态
SELECT currval('vector_data_id_seq') AS current_value;
SELECT nextval('vector_data_id_seq') AS next_value; 