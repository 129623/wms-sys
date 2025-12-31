-- 添加测试数据：货架、库位和库存
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 为"手机存储区"(zone_id=1)添加货架
INSERT INTO base_rack (rack_id, zone_id, warehouse_id, rack_code, rack_type, del_flag, create_time) VALUES
(100, 1, 1, 'R100', 'SINGLE', '0', NOW()),
(101, 1, 1, 'R101', 'DOUBLE', '0', NOW())
ON DUPLICATE KEY UPDATE rack_code=rack_code;

-- 为货架100添加库位
INSERT INTO base_location (rack_id, warehouse_id, zone_id, location_code, row_no, layer_no, status, del_flag, create_time) VALUES
(NULL, 100, 1, 1, 'R100-01-01', 1, 1, 0, '0', NOW()),
(NULL, 100, 1, 1, 'R100-01-02', 1, 2, 0, '0', NOW()),
(NULL, 100, 1, 1, 'R100-01-03', 1, 3, 0, '0', NOW()),
(NULL, 100, 1, 1, 'R100-02-01', 2, 1, 0, '0', NOW()),
(NULL, 100, 1, 1, 'R100-02-02', 2, 2, 0, '0', NOW()),
(NULL, 100, 1, 1, 'R100-02-03', 2, 3, 0, '0', NOW()),
(NULL, 101, 1, 1, 'R101-01-01', 1, 1, 0, '0', NOW()),
(NULL, 101, 1, 1, 'R101-01-02', 1, 2, 0, '0', NOW()),
(NULL, 101, 1, 1, 'R101-02-01', 2, 1, 0, '0', NOW()),
(NULL, 101, 1, 1, 'R101-02-02', 2, 2, 0, '0', NOW())
ON DUPLICATE KEY UPDATE location_code=location_code;

-- 2. 为"配件拣货区"(zone_id=2)添加货架
INSERT INTO base_rack (rack_id, zone_id, warehouse_id, rack_code, rack_type, del_flag, create_time) VALUES
(102, 2, 1, 'R102', 'SINGLE', '0', NOW())
ON DUPLICATE KEY UPDATE rack_code=rack_code;

-- 添加库位
INSERT INTO base_location (rack_id, warehouse_id, zone_id, location_code, row_no, layer_no, status, del_flag, create_time) VALUES
(NULL, 102, 1, 2, 'R102-01-01', 1, 1, 0, '0', NOW()),
(NULL, 102, 1, 2, 'R102-01-02', 1, 2, 0, '0', NOW()),
(NULL, 102, 1, 2, 'R102-02-01', 2, 1, 0, '0', NOW()),
(NULL, 102, 1, 2, 'R102-02-02', 2, 2, 0, '0', NOW())
ON DUPLICATE KEY UPDATE location_code=location_code;

-- 3. 添加库存（模拟部分库位被占用）
INSERT INTO wms_inventory (location_id, product_id, warehouse_id, available_qty, allocated_qty, total_qty, del_flag, create_time)
SELECT l.location_id, 1, 1, 100, 0, 100, '0', NOW()
FROM base_location l
WHERE l.zone_id = 1 AND l.location_code LIKE 'R100%' AND l.del_flag = '0'
LIMIT 4
ON DUPLICATE KEY UPDATE total_qty=100;

INSERT INTO wms_inventory (location_id, product_id, warehouse_id, available_qty, allocated_qty, total_qty, del_flag, create_time)
SELECT l.location_id, 2, 1, 50, 0, 50, '0', NOW()
FROM base_location l
WHERE l.zone_id = 2 AND l.del_flag = '0'
LIMIT 2
ON DUPLICATE KEY UPDATE total_qty=50;

SET FOREIGN_KEY_CHECKS = 1;

-- 查看结果
SELECT 
    z.zone_id,
    z.zone_name,
    COUNT(DISTINCT l.location_id) as total_locations,
    COUNT(DISTINCT CASE WHEN i.inventory_id IS NOT NULL THEN l.location_id END) as used_locations
FROM base_zone z
LEFT JOIN base_location l ON z.zone_id = l.zone_id AND l.del_flag = '0'
LEFT JOIN wms_inventory i ON l.location_id = i.location_id AND i.del_flag = '0' AND i.total_qty > 0
WHERE z.del_flag = '0'
GROUP BY z.zone_id, z.zone_name
HAVING total_locations > 0
ORDER BY z.zone_id;
