-- 清理脏数据
-- 删除名称为 '1·' 和 'a' 的库区及其关联的所有数据

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 找到要删除的 zone_id
-- (为了安全，先找出ID，但这里直接用子查询删除更方便，不过MySQL不允许在DELETE中直接子查询同表，所以用JOIN)

-- 1.1 删除关联的库存 (wms_inventory)
DELETE i FROM wms_inventory i
INNER JOIN base_location l ON i.location_id = l.location_id
INNER JOIN base_zone z ON l.zone_id = z.zone_id
WHERE z.zone_name IN ('1·', 'a', '1', '2额2', '21额2e', '22', '222');

-- 1.2 删除关联的库位 (base_location)
DELETE l FROM base_location l
INNER JOIN base_zone z ON l.zone_id = z.zone_id
WHERE z.zone_name IN ('1·', 'a', '1', '2额2', '21额2e', '22', '222');

-- 1.3 删除关联的货架 (base_rack)
DELETE r FROM base_rack r
INNER JOIN base_zone z ON r.zone_id = z.zone_id
WHERE z.zone_name IN ('1·', 'a', '1', '2额2', '21额2e', '22', '222');

-- 1.4 最后删除库区本身
DELETE FROM base_zone 
WHERE zone_name IN ('1·', 'a', '1', '2额2', '21额2e', '22', '222');

SET FOREIGN_KEY_CHECKS = 1;

-- 查看剩余库区
SELECT zone_id, zone_name FROM base_zone WHERE del_flag='0';
