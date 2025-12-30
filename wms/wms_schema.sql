/*
 * WMS 仓储管理系统数据库脚本
 * Database: MySQL 5.7+
 * Date: 2025-12-28
 * Description: 包含全局外键约束和软删除 (del_flag)
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 系统管理模块 (System Management)
-- ----------------------------

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

-- ----------------------------
-- 2. 基础数据模块 (Master Data)
-- ----------------------------

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

-- 2.10 客户/供应商表 (Newly Added)
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

-- ----------------------------
-- 3. 业务数据模块 (Business Data)
-- ----------------------------

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

SET FOREIGN_KEY_CHECKS = 1;
