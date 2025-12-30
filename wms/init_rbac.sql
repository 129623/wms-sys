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

-- ----------------------------
-- User-Role Mappings
-- ----------------------------
-- Ensure admin has ROLE_ADMIN
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- Bind all other users to ROLE_USER (if they have no role, this is a bit rough but works for initial setup)
-- Note: IGNORE prevents duplicates if already assigned
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) 
SELECT `user_id`, 2 FROM `sys_user` WHERE `username` != 'admin';
