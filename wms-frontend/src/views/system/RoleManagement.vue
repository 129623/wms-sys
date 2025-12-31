<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { Search, Plus, Edit, Trash2 } from 'lucide-vue-next';
import request from '../../utils/request';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleName: ''
});

const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const form = reactive({
  roleId: null,
  roleName: '',
  roleKey: '',
  status: 1,
  remark: '',
  menuIds: []
});

const menuList = ref([]);
const menuTree = computed(() => {
  const buildTree = (parentId) => {
    return menuList.value
      .filter(m => m.parentId === parentId)
      .map(m => ({
        ...m,
        children: buildTree(m.menuId)
      }));
  };
  return buildTree(0);
});

const fetchMenus = async () => {
  try {
    const res = await request.get('/menu/list');
    if (res.code === 200) {
      menuList.value = res.data;
    }
  } catch (error) {
    console.error('Failed to fetch menus:', error);
  }
};

const fetchData = async () =>{
  loading.value = true;
  try {
    const res = await request.get('/role/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('Failed to fetch roles:', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.pageNum = 1;
  fetchData();
};

const openAdd = () => {
  isEdit.value = false;
  Object.assign(form, {
    roleId: null,
    roleName: '',
    roleKey: '',
    status: 1,
    remark: '',
    menuIds: []
  });
  showModal.value = true;
};

const openEdit = async (row) => {
  isEdit.value = true;
  // Initialize simple fields
  Object.assign(form, {
    roleId: row.roleId,
    roleName: row.roleName,
    roleKey: row.roleKey,
    status: row.status,
    remark: row.remark,
    menuIds: [] 
  });
  
  // Fetch existing menuIds for this role
  try {
    const res = await request.get(`/role/${row.roleId}/menus`);
    if (res.code === 200) {
      const allPerms = res.data || [];
      // We need to filter out 'implicit' permissions to avoid over-checking in UI.
      // But this is tricky because we don't know which ones were added explicitly vs implicitly.
      // However, usually 'list' permissions (like base:warehouse:list) are dependencies.
      // If a user has 'base:warehouse:list' but NOT 'base:warehouse:add' (or other buttons), 
      // it's likely a dependency, NOT an explicit 'Warehouse Management' full grant.
      // 
      // Strategy:
      // For each menu item in the tree (Page level):
      // Check if we have its ID in the returned list.
      // IF we have it, ALSO check if we have at least one significant child/button (like 'add' or 'edit') 
      // OR if it has no children (leaf).
      // If we ONLY have the 'list' permission (which corresponds to the menu itself usually), 
      // AND it serves as a dependency for others, we might want to uncheck it visually 
      // UNLESS it was explicitly granted. 
      //
      // Better approach for now to match user expectation:
      // If a menu has children (buttons), only check the parent if at least one 'action' button (non-list?) is also present?
      // Or simply: Do not check the box if it's considered a "dependency-only" grant.
      // A dependency is currently defined in 'dependencyMap'.
      // If 'base:warehouse:list' is present, check if we have any OTHER permission that requires it?
      // No, that's reverse.
      //
      // Let's look at the Menu Structure.
      // Warehouse Management (Menu) -> perms: base:warehouse:list
      //   -> Add (Button) -> perms: base:warehouse:add
      //   -> Edit (Button) -> perms: base:warehouse:edit
      //
      // If I have ONLY base:warehouse:list, it might be because I'm an "Inbound Manager". 
      // In this case, "Warehouse Management" checkbox should probably be UNCHECKED or INDETERMINATE?
      // But we are using simple checkboxes. Unchecked is safer to avoid "explicit grant" confusion.
      
      const dependencyValues = new Set();
      Object.values(dependencyMap).forEach(deps => deps.forEach(d => dependencyValues.add(d)));
      
      const filteredIds = allPerms.filter(id => {
          const menu = menuList.value.find(m => m.menuId === id);
          if (!menu) return false;
          
          // 如果这个 menu 的 perms 在 dependencyMap 的 values 里（即它是被依赖的）
          if (menu.perms && dependencyValues.has(menu.perms)) {
             // 它是某人的依赖项。我们需要区分它是"仅仅作为依赖被动选择"还是"用户显式选择"。
             // 策略：
             // 1. 如果它有子节点 (非叶子)，检查是否有子节点被选中。如果有，则保留（显式）。如果没有，则可能是隐式，过滤。
             const children = menuList.value.filter(m => m.parentId === menu.menuId);
             if (children.length > 0) {
                const hasChild = children.some(child => allPerms.includes(child.menuId));
                return hasChild;
             }
             
             // 2. 如果它是叶子节点 (没有子节点)，我们无法区分它是隐式还是显式。
             // 为了避免"勾选了却不回显"的Bug，必须保留它。除非我们有更复杂的元数据记录"source"。
             // 目前最安全的方式是保留所有叶子节点。
             return true; 
          }
          return true;
      });

      form.menuIds = filteredIds;
    }
  } catch (error) {
    console.error('Failed to fetch role menus:', error);
    // Continue even if fail, just empty
  }
  
  showModal.value = true;
};

const handleDelete = async (id) => {
  if (!confirm('确认删除该角色？')) return;
  try {
    const res = await request.delete(`/role/${id}`);
    if (res.code === 200) {
      alert('删除成功');
      fetchData();
    } else {
      alert('删除失败：' + res.message);
    }
  } catch (e) {
    alert('删除失败');
  }
};

const handleSubmit = async () => {
  if (!form.roleName || !form.roleKey) {
    alert('请填写必填项');
    return;
  }
  
  submitting.value = true;
  try {
    const url = isEdit.value ? '/role/update' : '/role/add';
    const method = isEdit.value ? 'put' : 'post';
    
    // Payload should match SysRoleAddDTO / SysRoleUpdateDTO
    // Payload should match SysRoleAddDTO / SysRoleUpdateDTO
    // Calculate final menu IDs including dependencies
    const finalMenuIds = new Set(form.menuIds);
    
    // 1. Grant all children (buttons) for selected menus
    // Users click the parent menu (e.g., 'Location Management'), we must auto-grant 'add', 'edit', 'delete' etc.
    // Iterating form.menuIds which contains the selected parents
    form.menuIds.forEach(id => {
       // Find all children in the flat list where parentId equals this id
       const children = menuList.value.filter(m => m.parentId === id);
       children.forEach(child => finalMenuIds.add(child.menuId));
    });

    // 2. Add dependencies (hidden)
    // iterate over selected IDs (and newly added children? dependencies are usually on the parent module level)
    // using Array.from(finalMenuIds) to include the just-added children if they trigger dependencies (though usually top-level does)
    Array.from(finalMenuIds).forEach(id => {
       const menuItem = menuList.value.find(m => m.menuId === id);
       if (menuItem && menuItem.perms) {
          for (const [key, dependencies] of Object.entries(dependencyMap)) {
             // Check if permission starts with key (e.g. 'base:location' matches 'base:location:list', 'base:location:add')
             // or exact match for directory level if it has perms.
            if (menuItem.perms.startsWith(key)) {
               dependencies.forEach(depPerm => {
                  const depMenu = menuList.value.find(m => m.perms === depPerm);
                  if (depMenu) {
                     finalMenuIds.add(depMenu.menuId);
                  }
               });
            }
          }
       }
    });

    const payload = {
        ...form,
        menuIds: Array.from(finalMenuIds)
    };

    const res = await request[method](url, payload);
    if (res.code === 200) {
      alert(isEdit.value ? '更新成功' : '添加成功');
      showModal.value = false;
      fetchData();
    } else {
      alert('操作失败：' + res.message);
    }
  } catch (e) {
    alert('操作失败');
    console.error(e);
  } finally {
    submitting.value = false;
  }
};

const dependencyMap = {
  // Inbound needs Warehouse, Product, Supplier
  'wms:inbound': ['base:warehouse:list', 'base:product:list', 'base:supplier:list'],
  // Outbound needs Warehouse, Product, Customer, Inventory
  'wms:outbound': ['base:warehouse:list', 'base:product:list', 'base:customer:list', 'wms:inventory:list'],
  // Inventory needs Warehouse, Product
  'wms:inventory': ['base:warehouse:list', 'base:product:list'],
  // Base Data Dependencies
  // Zone, Rack, Location generally need to read Warehouse info
  'base:zone': ['base:warehouse:list'],
  'base:rack': ['base:warehouse:list', 'base:zone:list'],
  'base:location': ['base:warehouse:list', 'base:zone:list', 'base:rack:list'],
  // Unit and Storage Types might be standalone but if they relate to products/warehouse, add as needed.
  // Assuming they are relatively standalone dictionary data, but keeping consistency.
  // If Location depends on Zone and Rack, we could chain them, but flat list with Warehouse is safest minimum.
};

const handleMenuCheck = (item, checked) => {
  // Toggle the item itself
  const toggleItem = (menuId, isChecked) => {
    if (isChecked) {
      if (!form.menuIds.includes(menuId)) form.menuIds.push(menuId);
    } else {
      const index = form.menuIds.indexOf(menuId);
      if (index > -1) form.menuIds.splice(index, 1);
    }
  };

  // Recursively toggle children
  const toggleChildren = (node, isChecked) => {
    toggleItem(node.menuId, isChecked);
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => toggleChildren(child, isChecked));
    }
  };

  // Process the tree from the clicked item
  toggleChildren(item, checked);
};

onMounted(() => {
  fetchMenus();
  fetchData();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>角色管理</h2>
        <p class="subtitle">管理角色及其权限 (菜单) 分配</p>
      </div>
       <button class="btn btn-primary" @click="openAdd">
        <Plus :size="16" style="margin-right: 4px" /> 新增角色
      </button>
    </div>

     <div class="search-bar">
      <div class="input-icon-wrapper">
        <Search :size="18" class="icon" />
        <input 
          v-model="queryParams.roleName" 
          @keyup.enter="handleSearch"
          type="text" 
          placeholder="输入角色名称搜索..." 
        />
      </div>
      <button class="btn btn-outline" @click="handleSearch">搜索</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>角色名称</th>
            <th>权限字符</th>
            <th>状态</th>
            <th>备注</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.roleId">
            <td class="font-medium">{{ item.roleName }}</td>
            <td>{{ item.roleKey }}</td>
             <td>
              <span class="badge" :class="item.status === 1 ? 'badge-success' : 'badge-danger'">
                {{ item.status === 1 ? '正常' : '停用' }}
              </span>
            </td>
            <td class="text-secondary">{{ item.remark || '-' }}</td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue" @click="openEdit(item)" title="编辑/分配权限">
                  <Edit :size="16" />
                </button>
                <button class="action-btn text-red" @click="handleDelete(item.roleId)" title="删除">
                  <Trash2 :size="16" />
                </button>
              </div>
            </td>
          </tr>
           <tr v-if="!loading && tableData.length === 0">
            <td colspan="5" class="text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>
      <div class="pagination" v-if="total > 0">
         <span class="text-sm text-secondary">共 {{ total }} 条记录</span>
         <div class="pagination-controls">
            <button class="btn btn-sm btn-outline" :disabled="queryParams.pageNum <= 1" @click="() => { queryParams.pageNum--; fetchData(); }">上一页</button>
            <span class="page-info">第 {{ queryParams.pageNum }} 页 / 共 {{ Math.ceil(total / queryParams.pageSize) }} 页</span>
            <button class="btn btn-sm btn-outline" :disabled="queryParams.pageNum >= Math.ceil(total / queryParams.pageSize)" @click="() => { queryParams.pageNum++; fetchData(); }">下一页</button>
         </div>
      </div>
    </div>

    <!-- 新增/编辑角色模态框 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}角色</h3>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>角色名称 <span class="required">*</span></label>
            <input 
              v-model="form.roleName" 
              class="form-input" 
              placeholder="请输入角色名称"
            />
          </div>
          
          <div class="form-group">
            <label>权限字符 <span class="required">*</span></label>
            <input 
              v-model="form.roleKey" 
              class="form-input" 
              placeholder="例如: ROLE_USER"
            />
          </div>

          <div class="form-group">
            <label>权限分配 (菜单)</label>
            <div class="menu-permission-container">
              <div v-for="parent in menuTree" :key="parent.menuId" class="menu-group">
                <div class="menu-parent">
                   <label class="checkbox-label">
                    <input type="checkbox" :value="parent.menuId" v-model="form.menuIds" @change="(e) => handleMenuCheck(parent, e.target.checked)">
                    {{ parent.menuName }}
                  </label>
                </div>
                <div class="menu-children" v-if="parent.children && parent.children.length">
                   <div v-for="child in parent.children" :key="child.menuId" class="menu-sub-group">
                     <label class="checkbox-label child-label">
                        <input type="checkbox" :value="child.menuId" v-model="form.menuIds" @change="(e) => handleMenuCheck(child, e.target.checked)">
                        {{ child.menuName }}
                      </label>
                   </div>
                </div>
              </div>
            </div>
          </div>


          <div class="form-group">
            <label>状态</label>
            <select v-model="form.status" class="form-input">
              <option :value="1">正常</option>
              <option :value="0">停用</option>
            </select>
          </div>
          
           <div class="form-group">
            <label>备注</label>
            <textarea 
              v-model="form.remark" 
              class="form-input" 
              rows="2"
              placeholder="备注信息"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal=false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">
            {{ submitting ? '提交中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container { padding: 0 2rem 2rem; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
.page-header h2 { font-size: 1.5rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.25rem; }
.subtitle { color: var(--text-secondary); font-size: 0.875rem; }
.search-bar { display: flex; gap: 1rem; margin-bottom: 1.5rem; align-items: center; }
.input-icon-wrapper { position: relative; }
.input-icon-wrapper .icon { position: absolute; left: 1rem; top: 50%; transform: translateY(-50%); color: var(--text-secondary); }
.input-icon-wrapper input { padding: 0.6rem 1rem 0.6rem 2.5rem; border: 1px solid var(--border-color); background: white; border-radius: 0.5rem; width: 300px; outline: none; font-size: 0.875rem; transition: all 0.2s; }
.input-icon-wrapper input:focus { box-shadow: 0 0 0 2px var(--primary-light); border-color: var(--primary-color); }
.table-container { background: white; border-radius: 1rem; overflow: hidden; box-shadow: var(--shadow-sm); }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 1rem 1.5rem; font-size: 0.875rem; color: var(--text-secondary); font-weight: 600; border-bottom: 1px solid var(--border-color); background: #f9fafb; }
td { padding: 1rem 1.5rem; font-size: 0.875rem; border-bottom: 1px solid var(--border-color); color: var(--text-main); }
tr:last-child td { border-bottom: none; }
.action-buttons { display: flex; gap: 0.75rem; }
.action-btn { background: none; border: none; cursor: pointer; transition: opacity 0.2s; }
.action-btn:hover { opacity: 0.7; }
.text-blue { color: #8b5cf6; }
.text-red { color: #ef4444; }
.text-center { text-align: center; }
.badge-danger { background: #fee2e2; color: #dc2626; }

.pagination-controls { display: flex; align-items: center; gap: 1rem; }
.pagination { padding: 1rem; display: flex; justify-content: flex-end; align-items: center; gap: 1rem; border-top: 1px solid var(--border-color); }
.badge { display: inline-flex; padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; }
.badge-success { background: #dcfce7; color: #16a34a; }
.badge-danger { background: #fee2e2; color: #dc2626; }

/* Modal styles */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 50; }
.modal { background: white; width: 500px; max-height: 90vh; overflow-y: auto; padding: 1.5rem; border-radius: 1rem; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); }
.modal-header { font-size: 1.25rem; font-weight: 700; margin-bottom: 1.5rem; color: var(--text-main); }
.modal-body { display: flex; flex-direction: column;  gap: 1rem; }
.form-group { display: flex; flex-direction: column; gap: 0.5rem; }
.form-group label { font-size: 0.875rem; font-weight: 600; color: var(--text-main); }
.required { color: #ef4444; }
.form-input { padding: 0.6rem 1rem; border: 1px solid var(--border-color); border-radius: 0.5rem; font-size: 0.875rem; outline: none; transition: all 0.2s; }
.form-input:focus { box-shadow: 0 0 0 2px var(--primary-light); border-color: var(--primary-color); }
.form-input:disabled { background: #f9fafb; cursor: not-allowed; }
.modal-footer { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1.5rem; }
.btn { padding: 0.6rem 1.25rem; border-radius: 0.5rem; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: all 0.2s; border: none; display: flex; align-items: center; }
.btn-primary { background: var(--primary-color); color: white; }
.btn-primary:hover { background: var(--primary-dark); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-outline { background: white; border: 1px solid var(--border-color); color: var(--text-main); }
.btn-outline:hover { background: #f9fafb; }
.checkbox-group { display: flex; flex-wrap: wrap; gap: 1rem; }
.checkbox-label { display: flex; align-items: center; gap: 0.5rem; font-size: 0.875rem; cursor: pointer; }
.text-secondary { color: var(--text-secondary); font-size: 0.75rem; }

/* Menu permissions styles */
.menu-permission-container { max-height: 200px; overflow-y: auto; border: 1px solid var(--border-color); border-radius: 0.5rem; padding: 0.5rem; }
.menu-group { margin-bottom: 0.5rem; border-bottom: 1px dashed var(--border-color); padding-bottom: 0.5rem; }
.menu-group:last-child { border-bottom: none; }
.menu-parent { font-weight: 600; margin-bottom: 0.25rem; }
.menu-children { display: flex; flex-wrap: wrap; gap: 1rem; padding-left: 1.5rem; }
</style>
