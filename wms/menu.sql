-- 1. 系统管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('系统管理', 0, 1, NULL, 'M'); -- ID: 1
-- 用户管理 (ID: 2) -> 按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('用户管理', 1, 1, 'system:user:list', 'C');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('用户新增', 2, 1, 'system:user:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('用户修改', 2, 2, 'system:user:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('用户删除', 2, 3, 'system:user:delete', 'F');
-- 角色管理 (ID: 6) -> 按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('角色管理', 1, 2, 'system:role:list', 'C');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('角色新增', 6, 1, 'system:role:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('角色修改', 6, 2, 'system:role:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('角色删除', 6, 3, 'system:role:delete', 'F');
-- 菜单管理 (ID: 10) -> 按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('菜单管理', 1, 3, 'system:menu:list', 'C');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('菜单新增', 10, 1, 'system:menu:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('菜单修改', 10, 2, 'system:menu:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('菜单删除', 10, 3, 'system:menu:delete', 'F');

-- 2. 基础数据
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('基础数据', 0, 2, NULL, 'M'); -- ID: 14
-- 仓库管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('仓库管理', 14, 1, 'base:warehouse:list', 'C'); -- ID: 15
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('仓库新增', 15, 1, 'base:warehouse:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('仓库删除', 15, 2, 'base:warehouse:delete', 'F');
-- 产品管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('产品管理', 14, 2, 'base:product:list', 'C'); -- ID: 18
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('产品新增', 18, 1, 'base:product:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('产品删除', 18, 2, 'base:product:delete', 'F');
-- 客户管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('客户管理', 14, 3, 'base:customer:list', 'C'); -- ID: 40
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('客户新增', 40, 1, 'base:customer:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('客户修改', 40, 2, 'base:customer:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('客户删除', 40, 3, 'base:customer:delete', 'F');
-- 分类管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('分类管理', 14, 4, 'base:category:list', 'C'); -- ID: 21
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('分类新增', 21, 1, 'base:category:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('分类删除', 21, 2, 'base:category:delete', 'F');
-- 库位管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('库位管理', 14, 5, 'base:location:list', 'C'); -- ID: 24
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('库位新增', 24, 1, 'base:location:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('库位删除', 24, 2, 'base:location:delete', 'F');
-- 计量单位
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('单位管理', 14, 6, 'base:unit:list', 'C'); -- ID: 43
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('单位新增', 43, 1, 'base:unit:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('单位修改', 43, 2, 'base:unit:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('单位删除', 43, 3, 'base:unit:delete', 'F');
-- 产品标签
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('标签管理', 14, 7, 'base:label:list', 'C'); -- ID: 46
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('标签新增', 46, 1, 'base:label:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('标签修改', 46, 2, 'base:label:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('标签删除', 46, 3, 'base:label:delete', 'F');
-- 货架管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('货架管理', 14, 8, 'base:rack:list', 'C'); -- ID: 49
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('货架新增', 49, 1, 'base:rack:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('货架修改', 49, 2, 'base:rack:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('货架删除', 49, 3, 'base:rack:delete', 'F');
-- 存储类型
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('存储类型', 14, 9, 'base:strgtype:list', 'C'); -- ID: 52
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('类型新增', 52, 1, 'base:strgtype:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('类型修改', 52, 2, 'base:strgtype:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('类型删除', 52, 3, 'base:strgtype:delete', 'F');
-- 库区管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('库区管理', 14, 10, 'base:zone:list', 'C'); -- ID: 55
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('库区新增', 55, 1, 'base:zone:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('库区修改', 55, 2, 'base:zone:edit', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('库区删除', 55, 3, 'base:zone:delete', 'F');

-- 3. 业务管理 (入库/出库/库存)
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('业务管理', 0, 3, NULL, 'M'); -- ID: 27
-- 入库
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('入库管理', 27, 1, 'wms:inbound:list', 'C'); -- ID: 28
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('入库单新增', 28, 1, 'wms:inbound:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('入库确认', 28, 2, 'wms:inbound:receipt', 'F'); -- 对应 /inbound/receipt
-- 出库
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('出库管理', 27, 2, 'wms:outbound:list', 'C'); -- ID: 32
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('出库单新增', 32, 1, 'wms:outbound:add', 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('拣货确认', 32, 2, 'wms:outbound:pick', 'F'); -- 对应 /outbound/pick
-- 库存
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type) VALUES ('库存查询', 27, 3, 'wms:inventory:list', 'C');