-- 全新初始化仓库基础数据
-- 顺序：清理 -> 仓库 -> 库区 -> 货架 -> 库位

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 清理现有数据 (为了保证ID重置和数据干净)
TRUNCATE TABLE wms_inventory; -- 先清空库存，避免外键约束
TRUNCATE TABLE base_location;
TRUNCATE TABLE base_rack;
TRUNCATE TABLE base_zone;
TRUNCATE TABLE base_warehouse;

-- 2. 创建仓库 (Warehouse)
-- status=1 表示启用
INSERT INTO base_warehouse (warehouse_id, warehouse_code, warehouse_name, address, manager, contact, status, del_flag, create_time) VALUES
(1, 'WH001', '主运营中心', '上海市浦东新区', '张经理', '13800000001', 1, '0', NOW());

-- 3. 创建库区 (Zone)
-- zone_id: 1 -> 电子产品区 (适合小体积)
-- zone_id: 2 -> 大宗物资区 (适合大体积/重货)
INSERT INTO base_zone (zone_id, warehouse_id, zone_code, zone_name, zone_type, status, del_flag, create_time) VALUES
(1, 1, 'Z001', '电子产品区', '存储区', 1, '0', NOW()),
(2, 1, 'Z002', '大宗物资区', '存储区', 1, '0', NOW());

-- 4. 创建货架 (Rack)
-- rack_id: 1 -> 电子区货架A
-- rack_id: 2 -> 大宗区货架B
INSERT INTO base_rack (rack_id, zone_id, warehouse_id, rack_code, rack_type, del_flag, create_time) VALUES
(1, 1, 1, 'A01', 'SINGLE', '0', NOW()),
(2, 2, 1, 'B01', 'HEAVY', '0', NOW());

-- 5. 创建库位 (Location)
-- 为 电子区货架A (A01) 创建 4个库位
-- 限制：限重 50kg, 限体积 0.1m³ (100000cm³)
INSERT INTO base_location (rack_id, zone_id, warehouse_id, location_code, row_no, layer_no, max_weight, max_volume, status, del_flag, create_time) VALUES
(1, 1, 1, 'A01-01-01', 1, 1, 50.00, 0.10, 0, '0', NOW()),
(1, 1, 1, 'A01-01-02', 1, 2, 50.00, 0.10, 0, '0', NOW()),
(1, 1, 1, 'A01-02-01', 2, 1, 50.00, 0.10, 0, '0', NOW()),
(1, 1, 1, 'A01-02-02', 2, 2, 50.00, 0.10, 0, '0', NOW());

-- 为 大宗区货架B (B01) 创建 2个大库位
-- 限制：限重 1000kg, 限体积 2.0m³
INSERT INTO base_location (rack_id, zone_id, warehouse_id, location_code, row_no, layer_no, max_weight, max_volume, status, del_flag, create_time) VALUES
(2, 2, 1, 'B01-01-01', 1, 1, 1000.00, 2.00, 0, '0', NOW()),
(2, 2, 1, 'B01-01-02', 1, 2, 1000.00, 2.00, 0, '0', NOW());

SET FOREIGN_KEY_CHECKS = 1;
