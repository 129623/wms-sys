-- 增强版数据脚本 (兼容写法)
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 扩充现有库区 (电子产品区 ZoneId=1) 的货架
INSERT INTO base_rack (rack_id, zone_id, warehouse_id, rack_code, rack_type, del_flag, create_time) VALUES
(11, 1, 1, 'A02', 'DOUBLE', '0', NOW()),
(12, 1, 1, 'A03', 'DOUBLE', '0', NOW())
ON DUPLICATE KEY UPDATE rack_code=rack_code;

-- 批量插入库位 (手动展开一部分以规避解析错误)
INSERT INTO base_location (rack_id, zone_id, warehouse_id, location_code, row_no, layer_no, max_weight, max_volume, status, del_flag, create_time) VALUES
-- Rack 11
(11, 1, 1, 'A02-1-1', 1, 1, 50, 0.1, 0, '0', NOW()), (11, 1, 1, 'A02-1-2', 1, 2, 50, 0.1, 0, '0', NOW()),
(11, 1, 1, 'A02-1-3', 1, 3, 50, 0.1, 0, '0', NOW()), (11, 1, 1, 'A02-1-4', 1, 4, 50, 0.1, 0, '0', NOW()),
(11, 1, 1, 'A02-2-1', 2, 1, 50, 0.1, 0, '0', NOW()), (11, 1, 1, 'A02-2-2', 2, 2, 50, 0.1, 0, '0', NOW()),
-- Rack 12
(12, 1, 1, 'A03-1-1', 1, 1, 50, 0.1, 0, '0', NOW()), (12, 1, 1, 'A03-1-2', 1, 2, 50, 0.1, 0, '0', NOW()),
(12, 1, 1, 'A03-2-1', 2, 1, 50, 0.1, 0, '0', NOW()), (12, 1, 1, 'A03-2-2', 2, 2, 50, 0.1, 0, '0', NOW());

-- 2. 新增第二个仓库 "冷链中心"
INSERT INTO base_warehouse (warehouse_id, warehouse_code, warehouse_name, address, status, del_flag, create_time) VALUES
(2, 'WH002', '冷链中心', '上海市闵行区', 1, '0', NOW())
ON DUPLICATE KEY UPDATE warehouse_name=warehouse_name;

-- 3. 新增冷链库区
INSERT INTO base_zone (zone_id, warehouse_id, zone_code, zone_name, zone_type, status, del_flag, create_time) VALUES
(3, 2, 'Z003', '生鲜冷藏区', '冷藏', 1, '0', NOW()),
(4, 2, 'Z004', '深冷冻结区', '冷冻', 1, '0', NOW())
ON DUPLICATE KEY UPDATE zone_name=zone_name;

-- 4. 为生鲜区(Z3)创建货架和库位
INSERT INTO base_rack (rack_id, zone_id, warehouse_id, rack_code, rack_type, del_flag, create_time) VALUES
(21, 3, 2, 'C01', 'SINGLE', '0', NOW())
ON DUPLICATE KEY UPDATE rack_code=rack_code;

-- 增加库位给生鲜区
INSERT INTO base_location (rack_id, zone_id, warehouse_id, location_code, row_no, layer_no, max_weight, max_volume, status, del_flag, create_time) VALUES
(21, 3, 2, 'C01-1-1', 1, 1, 100, 0.5, 0, '0', NOW()), (21, 3, 2, 'C01-1-2', 1, 2, 100, 0.5, 0, '0', NOW()),
(21, 3, 2, 'C01-2-1', 2, 1, 100, 0.5, 0, '0', NOW()), (21, 3, 2, 'C01-2-2', 2, 2, 100, 0.5, 0, '0', NOW()),
(21, 3, 2, 'C01-3-1', 3, 1, 100, 0.5, 0, '0', NOW()), (21, 3, 2, 'C01-3-2', 3, 2, 100, 0.5, 0, '0', NOW());

-- 5. 添加库存
-- 确保产品存在 (假设之前脚本可能没成功)
INSERT INTO base_product (product_id, sku_code, product_name, category_id, unit_id, weight, length, width, height, del_flag) VALUES
(1, 'SKU1001', 'iPhone 15', 1, 1, 0.2, 15, 8, 1, '0'),
(2, 'SKU2001', '澳洲牛排', 1, 1, 1.0, 20, 15, 5, '0')
ON DUPLICATE KEY UPDATE product_name=product_name;

-- 填充电子区 (ID 1), 刚才新建的库位
INSERT INTO wms_inventory (warehouse_id, zone_id, location_id, product_id, batch_no, total_qty, available_qty, frozen_qty, del_flag, create_time)
SELECT 1, 1, location_id, 1, 'BATCH_NEW', 10, 10, 0, '0', NOW()
FROM base_location WHERE zone_id = 1 AND location_code LIKE 'A02%' LIMIT 5;

-- 填充生鲜区 (ID 3)
INSERT INTO wms_inventory (warehouse_id, zone_id, location_id, product_id, batch_no, total_qty, available_qty, frozen_qty, del_flag, create_time)
SELECT 2, 3, location_id, 2, 'BATCH_FOOD', 20, 20, 0, '0', NOW()
FROM base_location WHERE zone_id = 3 LIMIT 4;

SET FOREIGN_KEY_CHECKS = 1;
