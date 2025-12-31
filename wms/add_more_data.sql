-- 增强版数据脚本
-- 目标：扩充仓库结构，并填入大量模拟数据演示容量管理

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 扩充现有库区 (电子产品区 ZoneId=1) 的货架和库位
-- 原有 RackId=1, 现在增加 RackId=11, 12, 13
INSERT INTO base_rack (rack_id, zone_id, warehouse_id, rack_code, rack_type, del_flag, create_time) VALUES
(11, 1, 1, 'A02', 'DOUBLE', '0', NOW()),
(12, 1, 1, 'A03', 'DOUBLE', '0', NOW());

-- 批量插入库位 (每个货架生成 10个库位: 2层 x 5列)
-- Rack 11
INSERT INTO base_location (rack_id, zone_id, warehouse_id, location_code, row_no, layer_no, max_weight, max_volume, status, del_flag, create_time)
SELECT 11, 1, 1, CONCAT('A02-', r, '-', l), r, l, 50.00, 0.10, 0, '0', NOW()
FROM (SELECT 1 AS r UNION SELECT 2) rows, (SELECT 1 AS l UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) layers;

-- Rack 12
INSERT INTO base_location (rack_id, zone_id, warehouse_id, location_code, row_no, layer_no, max_weight, max_volume, status, del_flag, create_time)
SELECT 12, 1, 1, CONCAT('A03-', r, '-', l), r, l, 50.00, 0.10, 0, '0', NOW()
FROM (SELECT 1 AS r UNION SELECT 2) rows, (SELECT 1 AS l UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) layers;

-- 2. 新增第二个仓库 "冷链中心"
INSERT INTO base_warehouse (warehouse_id, warehouse_code, warehouse_name, address, status, del_flag, create_time) VALUES
(2, 'WH002', '冷链中心', '上海市闵行区', 1, '0', NOW());

-- 3. 新增冷链库区
-- zone_id=3 生鲜区
-- zone_id=4 冷冻区
INSERT INTO base_zone (zone_id, warehouse_id, zone_code, zone_name, zone_type, status, del_flag, create_time) VALUES
(3, 2, 'Z003', '生鲜冷藏区', '冷藏', 1, '0', NOW()),
(4, 2, 'Z004', '深冷冻结区', '冷冻', 1, '0', NOW());

-- 4. 为生鲜区(Z3)创建货架和库位
INSERT INTO base_rack (rack_id, zone_id, warehouse_id, rack_code, rack_type, del_flag, create_time) VALUES
(21, 3, 2, 'C01', 'SINGLE', '0', NOW());

-- 增加10个库位给生鲜区
INSERT INTO base_location (rack_id, zone_id, warehouse_id, location_code, row_no, layer_no, max_weight, max_volume, status, del_flag, create_time)
SELECT 21, 3, 2, CONCAT('C01-', r, '-', l), r, l, 100.00, 0.50, 0, '0', NOW()
FROM (SELECT 1 AS r UNION SELECT 2) rows, (SELECT 1 AS l UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) layers;


-- 5. 添加库存 (Inventory) 
-- 需要确保 product_id=1 和 2 存在
INSERT INTO base_product (product_id, sku_code, product_name, category_id, unit_id, weight, length, width, height, del_flag) VALUES
(1, 'SKU1001', 'iPhone 15', 1, 1, 0.2, 15, 8, 1, '0'),
(2, 'SKU2001', '澳洲牛排', 1, 1, 1.0, 20, 15, 5, '0')
ON DUPLICATE KEY UPDATE product_name=product_name;

-- 5.1 填充电子区 (Zone 1) - 让它半满
-- 随机选取 12 个库位填入库存
INSERT INTO wms_inventory (warehouse_id, zone_id, location_id, product_id, batch_no, total_qty, available_qty, frozen_qty, del_flag, create_time)
SELECT 1, 1, location_id, 1, 'BATCH_ELEC', 50, 50, 0, '0', NOW()
FROM base_location WHERE zone_id=1 LIMIT 12;

-- 5.2 填充生鲜区 (Zone 3) - 让它快满 (80%)
-- 选取 8 个库位填入库存
INSERT INTO wms_inventory (warehouse_id, zone_id, location_id, product_id, batch_no, total_qty, available_qty, frozen_qty, del_flag, create_time)
SELECT 2, 3, location_id, 2, 'BATCH_FOOD', 20, 20, 0, '0', NOW()
FROM base_location WHERE zone_id=3 LIMIT 8;

SET FOREIGN_KEY_CHECKS = 1;

-- 验证统计结果
SELECT z.zone_name, 
       COUNT(DISTINCT l.location_id) as total,
       COUNT(DISTINCT i.location_id) as used
FROM base_zone z
LEFT JOIN base_location l ON z.zone_id = l.zone_id
LEFT JOIN wms_inventory i ON l.location_id = i.location_id
GROUP BY z.zone_name;
