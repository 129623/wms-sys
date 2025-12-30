<script setup>
import { ref, onMounted, reactive } from 'vue';
import { Search, Plus, Edit, Trash2, Key } from 'lucide-vue-next';
import request from '../../utils/request';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: ''
});

const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const form = reactive({
  userId: null,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  roleIds: [], // Changed to array for multiple roles
  status: 1
});

const roles = ref([]); // Will fetch from backend

const fetchRoles = async () => {
  try {
    const res = await request.get('/role/list');
    if (res.code === 200) {
      roles.value = res.data;
    }
  } catch (error) {
    console.error('Failed to fetch roles:', error);
  }
};

const fetchData = async () =>{
  loading.value = true;
  try {
    const res = await request.get('/user/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('Failed to fetch users:', error);
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
    userId: null,
    username: '',
    password: '123456',
    realName: '',
    email: '',
    phone: '',
    roleIds: [],
    status: 1
  });
  showModal.value = true;
};

const openEdit = (row) => {
  isEdit.value = true;
  // Initialize form with row data
  // Note: Backend might need to return current user's roleIds in the list/page API or we fetch detail
  // The current page API puts single `roleId` on the user object (viewing `SysUserController` and `SysUserServiceImpl` logic).
  // SysUserServiceImpl line 110: user.setRoleId(userRole.getRoleId()); -> only sets one.
  // We might need to handle this limitation or update backend. For now, we'll assume single role or work with what we have.
  // If we want multiple roles, we should probably fetch user detail or roles separately.
  // Given user needs "assign permissions", multiple roles is better, but if backend `page` returns one, it's tricky.
  // Let's stick to the existing behavior: `row.roleId` comes from `page` API.
  // But strictly `roleIds` in DTO is a list.
  
  let currentRoleIds = [];
  if (row.roleId) {
    currentRoleIds = [row.roleId];
  }
  
  Object.assign(form, {
    userId: row.userId,
    username: row.username,
    realName: row.realName,
    email: row.email,
    phone: row.phone,
    roleIds: currentRoleIds,
    status: row.status
  });
  showModal.value = true;
};

const handleDelete = async (id) => {
  if (!confirm('确认删除该用户？删除后无法恢复！')) return;
  try {
    const res = await request.delete(`/user/${id}`);
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

const handleResetPassword = async (id) => {
  if (!confirm('确认重置该用户密码为 123456？')) return;
  try {
    // Check if this endpoint exists, otherwise use update
    // SysUserController doesn't seem to have explicit resetPassword, but usually update can handle it.
    // Wait, the existing SystemManagement.vue used `/user/resetPassword/${id}`. Let's check if SysUserController has it.
    // The previously viewed SysUserController did NOT have resetPassword method. It only had add, update, delete, list, page.
    // So the previous frontend code might have been broken or assuming.
    // I will use `update` to reset password.
    const res = await request.put('/user/update', { userId: id, password: '123456' });
     if (res.code === 200) {
      alert('密码重置成功，新密码：123456');
    } else {
      alert('操作失败：' + res.message);
    }
  } catch (e) {
    alert('操作失败');
  }
};

const handleSubmit = async () => {
  if (!form.username || !form.realName) {
    alert('请填写必填项');
    return;
  }
  if (!isEdit.value && !form.password) {
    alert('请设置密码');
    return;
  }
  
  submitting.value = true;
  try {
    const url = isEdit.value ? '/user/update' : '/user/add';
    const method = isEdit.value ? 'put' : 'post';
    
    const payload = { ...form };

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

const getRoleName = (roleId) => {
  if (!roleId) return '-';
  const role = roles.value.find(r => r.roleId === roleId);
  return role ? role.roleName : '未知角色';
};

onMounted(() => {
  fetchRoles();
  fetchData();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p class="subtitle">管理系统用户及其角色分配</p>
      </div>
       <button class="btn btn-primary" @click="openAdd">
        <Plus :size="16" style="margin-right: 4px" /> 新增用户
      </button>
    </div>

     <div class="search-bar">
      <div class="input-icon-wrapper">
        <Search :size="18" class="icon" />
        <input 
          v-model="queryParams.username" 
          @keyup.enter="handleSearch"
          type="text" 
          placeholder="输入用户名搜索..." 
        />
      </div>
      <button class="btn btn-outline" @click="handleSearch">搜索</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>用户名</th>
            <th>姓名</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.userId">
            <td class="font-medium">{{ item.username }}</td>
            <td>{{ item.realName || '-' }}</td>
            <td>{{ getRoleName(item.roleId) }}</td>
             <td>
              <span class="badge" :class="item.status === 1 ? 'badge-success' : 'badge-danger'">
                {{ item.status === 1 ? '正常' : '停用' }}
              </span>
            </td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue" @click="openEdit(item)" title="编辑/分配角色">
                  <Edit :size="16" />
                </button>
                <button class="action-btn text-orange" @click="handleResetPassword(item.userId)" title="重置密码">
                  <Key :size="16" />
                </button>
                <button class="action-btn text-red" @click="handleDelete(item.userId)" title="删除">
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
      </div>
    </div>

    <!-- 新增/编辑用户模态框 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}用户</h3>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>用户名 <span class="required">*</span></label>
            <input 
              v-model="form.username" 
              class="form-input" 
              placeholder="请输入用户名"
              :disabled="isEdit"
            />
          </div>
          
          <div class="form-group" v-if="!isEdit">
            <label>密码 <span class="required">*</span></label>
            <input 
              v-model="form.password" 
              type="password"
              class="form-input" 
              placeholder="请输入密码"
            />
          </div>

          <div class="form-group">
            <label>真实姓名 <span class="required">*</span></label>
            <input 
              v-model="form.realName" 
              class="form-input" 
              placeholder="请输入真实姓名"
            />
          </div>

          <div class="form-group">
            <label>邮箱</label>
            <input 
              v-model="form.email" 
              type="email"
              class="form-input" 
              placeholder="请输入邮箱"
            />
          </div>

          <div class="form-group">
            <label>手机号</label>
            <input 
              v-model="form.phone" 
              class="form-input" 
              placeholder="请输入手机号"
            />
          </div>

          <div class="form-group">
            <label>角色分配</label>
            <div class="checkbox-group">
               <label v-for="role in roles" :key="role.roleId" class="checkbox-label">
                  <input type="checkbox" :value="role.roleId" v-model="form.roleIds">
                  {{ role.roleName }}
               </label>
            </div>
            <small class="text-secondary">可多选</small>
          </div>

          <div class="form-group">
            <label>状态</label>
            <select v-model="form.status" class="form-input">
              <option :value="1">正常</option>
              <option :value="0">停用</option>
            </select>
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
.text-orange { color: #f97316; }
.text-red { color: #ef4444; }
.text-center { text-align: center; }
.pagination { padding: 1rem; display: flex; justify-content: flex-end; border-top: 1px solid var(--border-color); }
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
</style>
