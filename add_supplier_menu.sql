
-- Insert Supplier Management Menu
INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, menu_type, order_num, del_flag) 
VALUES (215, 14, '供应商管理', 'base:supplier:list', 'C', 11, 0);

-- Insert Supplier Buttons
-- Add
INSERT INTO sys_menu (parent_id, menu_name, perms, menu_type, order_num, del_flag) 
VALUES (215, '供应商新增', 'base:supplier:add', 'F', 1, 0);

-- Edit
INSERT INTO sys_menu (parent_id, menu_name, perms, menu_type, order_num, del_flag) 
VALUES (215, '供应商修改', 'base:supplier:edit', 'F', 2, 0);

-- Delete
INSERT INTO sys_menu (parent_id, menu_name, perms, menu_type, order_num, del_flag) 
VALUES (215, '供应商删除', 'base:supplier:delete', 'F', 3, 0);

-- Import
INSERT INTO sys_menu (parent_id, menu_name, perms, menu_type, order_num, del_flag) 
VALUES (215, '供应商导入', 'base:supplier:import', 'F', 4, 0);

-- Export
INSERT INTO sys_menu (parent_id, menu_name, perms, menu_type, order_num, del_flag) 
VALUES (215, '供应商导出', 'base:supplier:export', 'F', 5, 0);
