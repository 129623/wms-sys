SET FOREIGN_KEY_CHECKS = 0;

-- 1. 清空所有业务数据
TRUNCATE TABLE wms_inventory_history;
TRUNCATE TABLE wms_inventory;
TRUNCATE TABLE wms_receiving_task;
TRUNCATE TABLE wms_picking_task;
TRUNCATE TABLE wms_inbound_detail;
TRUNCATE TABLE wms_inbound_order;
TRUNCATE TABLE wms_outbound_detail;
TRUNCATE TABLE wms_outbound_order;

-- 2. 清空基础数据
TRUNCATE TABLE base_location;
TRUNCATE TABLE base_rack;
TRUNCATE TABLE base_zone;
TRUNCATE TABLE base_warehouse;
TRUNCATE TABLE base_product;
TRUNCATE TABLE base_category;
TRUNCATE TABLE base_unit;
TRUNCATE TABLE base_customer;
TRUNCATE TABLE base_storage_type;
TRUNCATE TABLE base_product_label;

-- 3. 基础字典/分类数据
-- 单位
INSERT INTO base_unit (unit_id, unit_code, unit_name, del_flag, create_time) VALUES 
(1, 'PCS', '个', '0', NOW()),
(2, 'BOX', '箱', '0', NOW()),
(3, 'KG', '千克', '0', NOW());

-- 存放类型
INSERT INTO base_storage_type (type_id, type_code, type_name, del_flag, create_time) VALUES 
(1, 'NORMAL', '常温', '0', NOW()),
(2, 'COLD', '冷藏', '0', NOW()),
(3, 'FROZEN', '冷冻', '0', NOW());

-- 标签
INSERT INTO base_product_label (label_id, label_code, label_name, del_flag, create_time) VALUES 
(1, 'FRAGILE', '易碎品', '0', NOW()),
(2, 'HIGH-VALUE', '高价值', '0', NOW());

-- 分类
INSERT INTO base_category (category_id, parent_id, category_name, level, del_flag, create_time) VALUES 
(1, 0, '电子产品', 1, '0', NOW()),
(2, 0, '生活用品', 1, '0', NOW()),
(3, 0, '食品饮料', 1, '0', NOW());

-- 4. 合作伙伴 (客户/供应商)
INSERT INTO base_customer (customer_id, customer_code, customer_name, customer_type, contact_person, phone, email, address, city, status, del_flag, create_time) VALUES 
(1, 'JD', '京东商城', 1, '刘强东', '13811112222', 'jd@example.com', '亦庄经济开发区', '北京', 1, '0', NOW()),
(2, 'APPLE', '苹果贸易', 2, '库克', '021-66668888', 'apple@apple.com', '南京东路', '上海', 1, '0', NOW()),
(3, 'SF', '顺丰速运', 3, '王卫', '95338', 'sf@sf-express.com', '南山区', '深圳', 1, '0', NOW());

-- 5. 产品数据 (SKU)
INSERT INTO base_product (product_id, category_id, sku_code, product_name, spec, unit_id, storage_type_id, label_id, weight, del_flag, create_time) VALUES 
(1, 1, 'P001', 'iPhone 15 Pro Max', '256G 钛金属', 1, 1, 2, 0.22, '0', NOW()),
(2, 1, 'P002', 'MacBook Air M3', '13寸 16G+512G', 1, 1, 2, 1.24, '0', NOW()),
(3, 3, 'P003', '可口可乐', '330ml 罐装', 2, 2, NULL, 0.35, '0', NOW());

-- 6. 仓库结构 (WH -> Zone -> Rack -> Location)
INSERT INTO base_warehouse (warehouse_id, warehouse_code, warehouse_name, address, manager, contact, status, del_flag, create_time) VALUES 
(1, 'WH001', '主营中心仓', '北京市朝阳区', '张伟', '13500001111', 1, '0', NOW()),
(2, 'WH002', '生鲜冷链仓', '上海市松江区', '王芳', '13600002222', 1, '0', NOW());

INSERT INTO base_zone (zone_id, warehouse_id, zone_code, zone_name, zone_type, status, del_flag, create_time) VALUES 
(1, 1, 'Z01', '手机存储区', '存储区', 1, '0', NOW()),
(2, 1, 'Z02', '配件拣货区', '拣货区', 1, '0', NOW()),
(3, 2, 'Z03', '冷饮存放区', '冷藏', 1, '0', NOW());

INSERT INTO base_rack (rack_id, zone_id, warehouse_id, rack_code, rack_type, del_flag, create_time) VALUES 
(1, 1, 1, 'R01', 'SINGLE', '0', NOW()),
(2, 1, 1, 'R02', 'SINGLE', '0', NOW()),
(3, 3, 2, 'R03', 'DOUBLE', '0', NOW());

INSERT INTO base_location (location_id, rack_id, warehouse_id, zone_id, location_code, row_no, layer_no, status, del_flag, create_time) VALUES 
(1, 1, 1, 1, 'R01-01-01', 1, 1, 0, '0', NOW()),
(2, 1, 1, 1, 'R01-01-02', 1, 2, 0, '0', NOW()),
(3, 2, 1, 1, 'R02-01-01', 1, 1, 0, '0', NOW()),
(4, 3, 2, 3, 'R03-01-01', 1, 1, 0, '0', NOW());

SET FOREIGN_KEY_CHECKS = 1;
