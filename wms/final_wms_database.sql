/*
 * WMS 仓储管理系统 - 最终整合数据库脚本
 * Date: 2025-01-05
 * Description: 整合了Schema结构、RBAC权限、基础业务数据及数据修复脚本。
 * content:
 * 1. wms_schema.sql (表结构)
 * 2. init_rbac.sql (用户/角色/菜单)
 * 3. init_data.sql (基础数据: 仓库/产品/客户等)
 * 4. fix_location_zone_id.sql (数据修复)
 */

-- ==========================================
-- Part 1: Database Schema (wms_schema.sql)
-- ==========================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 系统管理模块 (System Management)
-- 1.1 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态 (1:正常, 0:停用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 1.2 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) NOT NULL COMMENT '角色权限字符',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态 (1:正常, 0:停用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 1.3 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`),
  CONSTRAINT `fk_ur_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ur_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`role_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 1.4 菜单权限表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识 (如 inventory:adjust)',
  `menu_type` char(1) DEFAULT '' COMMENT '类型 (M:目录, C:菜单, F:按钮)',
  `order_num` int(11) DEFAULT 0 COMMENT '显示顺序',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 1.5 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`),
  CONSTRAINT `fk_rm_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`role_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rm_menu_id` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`menu_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 1.6 操作日志表
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `module` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `operation` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `method` varchar(100) DEFAULT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- 2. 基础数据模块 (Master Data)
-- 2.1 仓库表
DROP TABLE IF EXISTS `base_warehouse`;
CREATE TABLE `base_warehouse` (
  `warehouse_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `warehouse_code` varchar(50) NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) NOT NULL COMMENT '仓库名称',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `manager` varchar(50) DEFAULT NULL COMMENT '负责人',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系方式',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态 (1:可用, 0:不可用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`warehouse_id`),
  UNIQUE KEY `uk_wh_code` (`warehouse_code`),
  UNIQUE KEY `uk_wh_name` (`warehouse_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 2.2 库区表
DROP TABLE IF EXISTS `base_zone`;
CREATE TABLE `base_zone` (
  `zone_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '库区ID',
  `warehouse_id` bigint(20) NOT NULL COMMENT '所属仓库ID',
  `zone_code` varchar(50) NOT NULL COMMENT '库区编码',
  `zone_name` varchar(100) DEFAULT NULL COMMENT '库区名称',
  `zone_type` varchar(20) DEFAULT NULL COMMENT '库区类型 (如: 冷冻, 常温)',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态 (1:可用, 0:不可用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`zone_id`),
  UNIQUE KEY `uk_zone_code` (`warehouse_id`, `zone_code`),
  CONSTRAINT `fk_zone_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `base_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库区表';

-- 2.3 货架表
DROP TABLE IF EXISTS `base_rack`;
CREATE TABLE `base_rack` (
  `rack_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '货架ID',
  `zone_id` bigint(20) NOT NULL COMMENT '所属库区ID',
  `warehouse_id` bigint(20) NOT NULL COMMENT '冗余所属仓库ID',
  `rack_code` varchar(50) NOT NULL COMMENT '货架编码',
  `rack_type` varchar(20) DEFAULT 'SINGLE' COMMENT '货架类型 (SINGLE:单排, DOUBLE:双排)',
  `related_rack_id` bigint(20) DEFAULT NULL COMMENT '关联货架ID (双排时使用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`rack_id`),
  UNIQUE KEY `uk_rack_code` (`zone_id`, `rack_code`),
  CONSTRAINT `fk_rack_zone` FOREIGN KEY (`zone_id`) REFERENCES `base_zone` (`zone_id`),
  CONSTRAINT `fk_rack_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `base_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货架表';

-- 2.4 库位表
DROP TABLE IF EXISTS `base_location`;
CREATE TABLE `base_location` (
  `location_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '库位ID',
  `rack_id` bigint(20) NOT NULL COMMENT '所属货架ID',
  `warehouse_id` bigint(20) NOT NULL COMMENT '冗余仓库ID',
  `zone_id` bigint(20) NOT NULL COMMENT '冗余库区ID',
  `location_code` varchar(50) NOT NULL COMMENT '库位编码 (格式: 货架-列-层)',
  `row_no` int(11) DEFAULT NULL COMMENT '列号',
  `layer_no` int(11) DEFAULT NULL COMMENT '层号',
  `status` tinyint(1) DEFAULT 0 COMMENT '状态 (0:空闲, 1:占用, 2:锁定)',
  `max_weight` decimal(10,2) DEFAULT 0 COMMENT '最大承重 (kg)',
  `max_volume` decimal(10,2) DEFAULT 0 COMMENT '最大体积 (m3)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`location_id`),
  UNIQUE KEY `uk_loc_code` (`warehouse_id`, `location_code`),
  CONSTRAINT `fk_loc_rack` FOREIGN KEY (`rack_id`) REFERENCES `base_rack` (`rack_id`),
  CONSTRAINT `fk_loc_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `base_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_loc_zone` FOREIGN KEY (`zone_id`) REFERENCES `base_zone` (`zone_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位表';

-- 2.5 产品分类表
DROP TABLE IF EXISTS `base_category`;
CREATE TABLE `base_category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `level` int(4) DEFAULT 1 COMMENT '层级 (1, 2, 3)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- 2.6 单位表
DROP TABLE IF EXISTS `base_unit`;
CREATE TABLE `base_unit` (
  `unit_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '单位ID',
  `unit_code` varchar(20) NOT NULL COMMENT '单位编码',
  `unit_name` varchar(20) NOT NULL COMMENT '单位名称',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`unit_id`),
  UNIQUE KEY `uk_unit_code` (`unit_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计量单位表';

-- 2.7 存放类型表
DROP TABLE IF EXISTS `base_storage_type`;
CREATE TABLE `base_storage_type` (
  `type_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `type_code` varchar(20) NOT NULL COMMENT '类型编码',
  `type_name` varchar(50) NOT NULL COMMENT '类型名称',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`type_id`),
  UNIQUE KEY `uk_st_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存放类型表';

-- 2.8 产品标签表
DROP TABLE IF EXISTS `base_product_label`;
CREATE TABLE `base_product_label` (
  `label_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `label_code` varchar(20) NOT NULL COMMENT '标签编码',
  `label_name` varchar(50) NOT NULL COMMENT '标签名称',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`label_id`),
  UNIQUE KEY `uk_pl_code` (`label_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品标签表';

-- 2.9 产品表 (SKU)
DROP TABLE IF EXISTS `base_product`;
CREATE TABLE `base_product` (
  `product_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `category_id` bigint(20) NOT NULL COMMENT '所属分类ID',
  `sku_code` varchar(50) NOT NULL COMMENT 'SKU编码',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `spec` varchar(100) DEFAULT NULL COMMENT '规格型号',
  `unit_id` bigint(20) DEFAULT NULL COMMENT '单位ID',
  `storage_type_id` bigint(20) DEFAULT NULL COMMENT '存放类型ID',
  `label_id` bigint(20) DEFAULT NULL COMMENT '主要标签ID',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '单品重量 (kg)',
  `length` decimal(10,2) DEFAULT NULL COMMENT '长 (cm)',
  `width` decimal(10,2) DEFAULT NULL COMMENT '宽 (cm)',
  `height` decimal(10,2) DEFAULT NULL COMMENT '高 (cm)',
  `pallet_capacity` int(11) DEFAULT NULL COMMENT '托盘满载数量 (个)',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图片URL',
  `origin` varchar(100) DEFAULT NULL COMMENT '产地',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_sku` (`sku_code`),
  CONSTRAINT `fk_prod_category` FOREIGN KEY (`category_id`) REFERENCES `base_category` (`category_id`),
  CONSTRAINT `fk_prod_unit` FOREIGN KEY (`unit_id`) REFERENCES `base_unit` (`unit_id`),
  CONSTRAINT `fk_prod_storage` FOREIGN KEY (`storage_type_id`) REFERENCES `base_storage_type` (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品(SKU)表';

-- 2.10 客户/供应商表
DROP TABLE IF EXISTS `base_customer`;
CREATE TABLE `base_customer` (
  `customer_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_code` varchar(50) NOT NULL COMMENT '客户编码',
  `customer_name` varchar(100) NOT NULL COMMENT '客户名称',
  `customer_type` tinyint(2) DEFAULT 1 COMMENT '类型 (1:客户/收货方, 2:供应商/发货方, 3:两者皆是)',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态 (1:可用, 0:不可用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `uk_cus_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户/供应商表';

-- 3. 业务数据模块 (Business Data)
-- 3.1 入库单主表
DROP TABLE IF EXISTS `wms_inbound_order`;
CREATE TABLE `wms_inbound_order` (
  `inbound_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  `order_no` varchar(50) NOT NULL COMMENT '入库单号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '目标仓库ID',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `order_type` tinyint(2) DEFAULT 1 COMMENT '入库类型 (1:采购入库, 2:调拨入库, 3:退货入库)',
  `status` tinyint(2) DEFAULT 0 COMMENT '状态 (0:已创建, 1:待收货, 2:收货中, 3:完成, -1:已取消)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`inbound_id`),
  UNIQUE KEY `uk_in_order_no` (`order_no`),
  CONSTRAINT `fk_in_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `base_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_in_customer` FOREIGN KEY (`customer_id`) REFERENCES `base_customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单主表';

-- 3.2 入库单明细表
DROP TABLE IF EXISTS `wms_inbound_detail`;
CREATE TABLE `wms_inbound_detail` (
  `detail_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `inbound_id` bigint(20) NOT NULL COMMENT '入库单ID',
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `plan_qty` int(11) NOT NULL COMMENT '计划数量',
  `received_qty` int(11) DEFAULT 0 COMMENT '已收数量',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  PRIMARY KEY (`detail_id`),
  CONSTRAINT `fk_ind_order` FOREIGN KEY (`inbound_id`) REFERENCES `wms_inbound_order` (`inbound_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ind_product` FOREIGN KEY (`product_id`) REFERENCES `base_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细表';

-- 3.3 收货任务表 (含上架策略)
DROP TABLE IF EXISTS `wms_receiving_task`;
CREATE TABLE `wms_receiving_task` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `inbound_id` bigint(20) NOT NULL COMMENT '关联入库单',
  `detail_id` bigint(20) NOT NULL COMMENT '关联明细',
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `qty` int(11) NOT NULL COMMENT '收货数量',
  `target_location_id` bigint(20) DEFAULT NULL COMMENT '推荐上架库位',
  `actual_location_id` bigint(20) DEFAULT NULL COMMENT '实际此批次存放库位',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '批次号 (系统生成或录入)',
  `status` tinyint(2) DEFAULT 0 COMMENT '状态 (0:待上架, 1:已上架)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`task_id`),
  CONSTRAINT `fk_rcv_inbound` FOREIGN KEY (`inbound_id`) REFERENCES `wms_inbound_order` (`inbound_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货/上架任务表';

-- 3.4 出库单主表
DROP TABLE IF EXISTS `wms_outbound_order`;
CREATE TABLE `wms_outbound_order` (
  `outbound_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  `order_no` varchar(50) NOT NULL COMMENT '出库单号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '源仓库ID',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户ID',
  `order_type` tinyint(2) DEFAULT 1 COMMENT '出库类型 (1:销售出库, 2:调拨出库, 3:领用出库)',
  `status` tinyint(2) DEFAULT 0 COMMENT '状态 (0:已创建, 1:待拣货, 2:拣货中, 3:已出库, -1:已取消)',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货电话',
  `address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`outbound_id`),
  UNIQUE KEY `uk_out_order_no` (`order_no`),
  CONSTRAINT `fk_out_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `base_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_out_customer` FOREIGN KEY (`customer_id`) REFERENCES `base_customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单主表';

-- 3.5 出库单明细表
DROP TABLE IF EXISTS `wms_outbound_detail`;
CREATE TABLE `wms_outbound_detail` (
  `detail_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `outbound_id` bigint(20) NOT NULL COMMENT '出库单ID',
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `plan_qty` int(11) NOT NULL COMMENT '计划出库数量',
  `picked_qty` int(11) DEFAULT 0 COMMENT '已拣货数量',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  PRIMARY KEY (`detail_id`),
  CONSTRAINT `fk_outd_order` FOREIGN KEY (`outbound_id`) REFERENCES `wms_outbound_order` (`outbound_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_outd_product` FOREIGN KEY (`product_id`) REFERENCES `base_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细表';

-- 3.6 拣货任务表
DROP TABLE IF EXISTS `wms_picking_task`;
CREATE TABLE `wms_picking_task` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `outbound_id` bigint(20) NOT NULL COMMENT '关联出库单',
  `detail_id` bigint(20) NOT NULL COMMENT '关联明细',
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `location_id` bigint(20) NOT NULL COMMENT '拣货库位',
  `batch_no` varchar(50) NOT NULL COMMENT '指定批次',
  `qty` int(11) NOT NULL COMMENT '拣货数量',
  `status` tinyint(2) DEFAULT 0 COMMENT '状态 (0:待拣货, 1:已拣货)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`task_id`),
  CONSTRAINT `fk_pick_outbound` FOREIGN KEY (`outbound_id`) REFERENCES `wms_outbound_order` (`outbound_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拣货任务表';

-- 3.7 库存表 (核心)
DROP TABLE IF EXISTS `wms_inventory`;
CREATE TABLE `wms_inventory` (
  `inventory_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库ID',
  `zone_id` bigint(20) NOT NULL COMMENT '库区ID',
  `location_id` bigint(20) NOT NULL COMMENT '库位ID',
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `batch_no` varchar(50) NOT NULL COMMENT '批次号',
  `total_qty` int(11) NOT NULL DEFAULT 0 COMMENT '总数量',
  `frozen_qty` int(11) NOT NULL DEFAULT 0 COMMENT '冻结数量 (分配中)',
  `available_qty` int(11) GENERATED ALWAYS AS (`total_qty` - `frozen_qty`) VIRTUAL COMMENT '可用数量',
  `quality_status` tinyint(1) DEFAULT 0 COMMENT '质量状态 (0:良品, 1:不良品/冻结)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`inventory_id`),
  UNIQUE KEY `uk_inventory_key` (`location_id`, `product_id`, `batch_no`),
  CONSTRAINT `fk_inv_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `base_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_inv_zone` FOREIGN KEY (`zone_id`) REFERENCES `base_zone` (`zone_id`),
  CONSTRAINT `fk_inv_location` FOREIGN KEY (`location_id`) REFERENCES `base_location` (`location_id`),
  CONSTRAINT `fk_inv_product` FOREIGN KEY (`product_id`) REFERENCES `base_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='即时库存表';

-- 3.8 库存流水/交易日志表
DROP TABLE IF EXISTS `wms_inventory_history`;
CREATE TABLE `wms_inventory_history` (
  `history_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库ID',
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '批次',
  `location_id` bigint(20) DEFAULT NULL COMMENT '库位ID',
  `change_type` tinyint(2) NOT NULL COMMENT '变动类型 (1:入库, 2:出库, 3:移库, 4:盘盈, 5:盘亏)',
  `change_qty` int(11) NOT NULL COMMENT '变动数量 (正负表示方向)',
  `after_qty` int(11) DEFAULT NULL COMMENT '变动后结存 (快照)',
  `order_no` varchar(50) DEFAULT NULL COMMENT '关联单号 (入/出库单号)',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';


-- ==========================================
-- Part 2: RBAC Data (init_rbac.sql)
-- ==========================================

USE `db_wms`;

-- ----------------------------
-- Roles
-- ----------------------------
INSERT IGNORE INTO `sys_role` (`role_id`, `role_name`, `role_key`, `status`) VALUES 
(1, '超级管理员', 'ROLE_ADMIN', 1),
(2, '普通用户', 'ROLE_USER', 1);

-- ----------------------------
-- Menus / Permissions
-- ----------------------------
-- 1. System
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `perms`, `menu_type`, `parent_id`) VALUES 
(1, '系统管理', 'system:manage', 'M', 0),
(101, '用户管理', 'system:user:list', 'C', 1),
(102, '角色管理', 'system:role:list', 'C', 1),
(103, '用户新增', 'system:user:add', 'F', 101),
(104, '用户修改', 'system:user:edit', 'F', 101),
(105, '用户删除', 'system:user:delete', 'F', 101),
(106, '角色新增', 'system:role:add', 'F', 102),
(107, '角色修改', 'system:role:edit', 'F', 102),
(108, '角色删除', 'system:role:delete', 'F', 102);

-- 2. Base Data
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `perms`, `menu_type`, `parent_id`) VALUES 
(2, '基础数据', 'base:manage', 'M', 0),
(201, '产品管理', 'base:product:list', 'C', 2),
(202, '分类管理', 'base:category:list', 'C', 2),
(203, '仓库管理', 'base:warehouse:list', 'C', 2),
(204, '库区管理', 'base:zone:list', 'C', 2),
(205, '货架管理', 'base:rack:list', 'C', 2),
(206, '库位管理', 'base:location:list', 'C', 2),
(207, '客户管理', 'base:customer:list', 'C', 2),
(208, '单位管理', 'base:unit:list', 'C', 2),
(209, '标签管理', 'base:label:list', 'C', 2),
(210, '存储类型', 'base:storage:list', 'C', 2);

-- Base Data Operations (Simplified, giving users add/edit generally for demo)
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `perms`, `menu_type`, `parent_id`) VALUES 
(211, '产品新增', 'base:product:add', 'F', 201),
(212, '产品修改', 'base:product:edit', 'F', 201),
(213, '产品删除', 'base:product:delete', 'F', 201);

-- 3. Inbound
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `perms`, `menu_type`, `parent_id`) VALUES 
(3, '入库管理', 'wms:inbound:manage', 'M', 0),
(301, '入库单', 'wms:inbound:list', 'C', 3),
(302, '入库新增', 'wms:inbound:add', 'F', 301),
(303, '入库详情', 'wms:inbound:detail', 'F', 301);

-- 4. Outbound
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `perms`, `menu_type`, `parent_id`) VALUES 
(4, '出库管理', 'wms:outbound:manage', 'M', 0),
(401, '出库单', 'wms:outbound:list', 'C', 4),
(402, '出库新增', 'wms:outbound:add', 'F', 401);

-- 5. Inventory
INSERT IGNORE INTO `sys_menu` (`menu_id`, `menu_name`, `perms`, `menu_type`, `parent_id`) VALUES 
(5, '库存管理', 'wms:inventory:manage', 'M', 0),
(501, '库存查询', 'wms:inventory:list', 'C', 5);

-- ----------------------------
-- Role-Menu Mappings
-- ----------------------------
-- Give ROLE_USER (2) access to Base Data (View), Inbound, Outbound, Inventory
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES 
-- Base View
(2, 201), (2, 202), (2, 203), (2, 204), (2, 205), (2, 206), (2, 207), (2, 208), (2, 209), (2, 210),
-- Base Ops
(2, 211), (2, 212), (2, 213),
-- Inbound
(2, 301), (2, 302), (2, 303),
-- Outbound
(2, 401), (2, 402),
-- Inventory
(2, 501);

-- Give ROLE_ADMIN (1) access to ALL menus (Grant everything)
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) 
SELECT 1, `menu_id` FROM `sys_menu`;

-- ----------------------------
-- User-Role Mappings
-- ----------------------------
-- Ensure admin has ROLE_ADMIN
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- Bind all other users to ROLE_USER (if they have no role, this is a bit rough but works for initial setup)
-- Note: IGNORE prevents duplicates if already assigned
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) 
SELECT `user_id`, 2 FROM `sys_user` WHERE `username` != 'admin';


-- ==========================================
-- Part 3: Business Data (init_data.sql)
-- ==========================================

-- 1. 清空所有业务数据
TRUNCATE TABLE wms_inventory_history;
TRUNCATE TABLE wms_inventory;
TRUNCATE TABLE wms_receiving_task;
TRUNCATE TABLE wms_picking_task;
TRUNCATE TABLE wms_inbound_detail;
TRUNCATE TABLE wms_inbound_order;
TRUNCATE TABLE wms_outbound_detail;
TRUNCATE TABLE wms_outbound_order;

-- 2. 清空基础数据 (保证ID重置)
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


-- ==========================================
-- Part 4: Data Fixes (fix_location_zone_id.sql)
-- ==========================================

-- 修复库位表中的zone_id字段 (确保一致性)
-- 更新所有库位的zone_id，从其所属货架获取
UPDATE base_location bl
INNER JOIN base_rack br ON bl.rack_id = br.rack_id
SET bl.zone_id = br.zone_id
WHERE bl.del_flag = '0' AND br.del_flag = '0';

SET FOREIGN_KEY_CHECKS = 1;
