
-- Fix Warehouse Buttons (Parent 203)
UPDATE sys_menu SET parent_id = 203 WHERE perms LIKE 'base:warehouse:%' AND menu_type = 'F';

-- Fix Zone Buttons (Parent 204)
UPDATE sys_menu SET parent_id = 204 WHERE perms LIKE 'base:zone:%' AND menu_type = 'F';

-- Fix Rack Buttons (Parent 205)
UPDATE sys_menu SET parent_id = 205 WHERE perms LIKE 'base:rack:%' AND menu_type = 'F';

-- Fix Location Buttons (Parent 206)
UPDATE sys_menu SET parent_id = 206 WHERE perms LIKE 'base:location:%' AND menu_type = 'F';

-- Fix Customer Buttons (Parent 207)
UPDATE sys_menu SET parent_id = 207 WHERE perms LIKE 'base:customer:%' AND menu_type = 'F';

-- Fix Unit Buttons (Parent 208)
UPDATE sys_menu SET parent_id = 208 WHERE perms LIKE 'base:unit:%' AND menu_type = 'F';

-- Fix Label Buttons (Parent 209)
UPDATE sys_menu SET parent_id = 209 WHERE perms LIKE 'base:label:%' AND menu_type = 'F';

-- Fix Storage Buttons (Parent 210) - Note: perms might be base:storage or base:strgtype
UPDATE sys_menu SET parent_id = 210 WHERE (perms LIKE 'base:storage:%' OR perms LIKE 'base:strgtype:%') AND menu_type = 'F';

-- Fix Product Buttons (Parent 201) - seem correct but double check
UPDATE sys_menu SET parent_id = 201 WHERE perms LIKE 'base:product:%' AND menu_type = 'F';

-- Fix Category Buttons (Parent 202)
UPDATE sys_menu SET parent_id = 202 WHERE perms LIKE 'base:category:%' AND menu_type = 'F';

