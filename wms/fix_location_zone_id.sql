-- 修复库位表中的zone_id字段
-- 将zone_id从关联的货架中同步过来

SET FOREIGN_KEY_CHECKS = 0;

-- 更新所有库位的zone_id，从其所属货架获取
UPDATE base_location bl
INNER JOIN base_rack br ON bl.rack_id = br.rack_id
SET bl.zone_id = br.zone_id
WHERE bl.del_flag = '0' AND br.del_flag = '0';

-- 验证修复结果：查看每个库区的库位数量
SELECT 
    z.zone_id,
    z.zone_name,
    COUNT(l.location_id) as location_count
FROM base_zone z
LEFT JOIN base_location l ON z.zone_id = l.zone_id AND l.del_flag = '0'
WHERE z.del_flag = '0'
GROUP BY z.zone_id, z.zone_name
ORDER BY z.zone_id;

SET FOREIGN_KEY_CHECKS = 1;
