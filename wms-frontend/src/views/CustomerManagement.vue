<script setup>
import { ref, onMounted, reactive } from 'vue';
import { Search, Plus, Edit, Trash2 } from 'lucide-vue-next';
import request from '../utils/request';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  customerName: ''
});

// Modal State
const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const form = reactive({
  customerId: null,
  customerCode: '',
  customerName: '',
  customerType: 1,
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  city: '',
  status: 1
});

const customerTypes = [
  { value: 1, label: '客户' },
  { value: 2, label: '供应商' },
  { value: 3, label: '全部' }
];

const fetchData = async () => {
  loading.value = true;
  try {
    // Backend uses POST /base/customer/list with Body
    const res = await request.post('/base/customer/list', queryParams);
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('Failed to fetch customers:', error);
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
    customerId: null,
    customerCode: 'C' + new Date().getTime().toString().slice(-6),
    customerName: '',
    customerType: 1,
    contactPerson: '',
    phone: '',
    email: '',
    address: '',
    city: '',
    status: 1
  });
  showModal.value = true;
};

const openEdit = (row) => {
  isEdit.value = true;
  Object.assign(form, row);
  showModal.value = true;
};

const handleDelete = async (id) => {
  if (!confirm('确认删除该客户吗？')) return;
  try {
    // Backend uses POST /base/customer/delete/{id}
    const res = await request.post(`/base/customer/delete/${id}`);
    if (res.code === 200) {
      fetchData();
    } else {
      alert(res.message);
    }
  } catch (e) {
    console.error(e);
  }
};

const handleSubmit = async () => {
  if (!form.customerName) return alert('请输入客户名称');
  
  submitting.value = true;
  try {
    const url = isEdit.value ? '/base/customer/update' : '/base/customer/add';
    // Backend uses POST for both
    const res = await request.post(url, form);
    
    if (res.code === 200) {
      showModal.value = false;
      fetchData();
    } else {
      alert(res.message);
    }
  } catch (e) {
    console.error(e);
  } finally {
    submitting.value = false;
  }
};

const getTypeName = (type) => {
    const map = { 1: '客户', 2: '供应商', 3: '客户/供应商' };
    return map[type] || '未知';
}

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>客户/供应商</h2>
        <p class="subtitle">管理往来单位信息</p>
      </div>
      <button class="btn btn-primary" @click="openAdd">
        <Plus :size="16" style="margin-right: 4px" /> 新增单位
      </button>
    </div>

    <div class="search-bar">
      <div class="input-icon-wrapper">
        <Search :size="18" class="icon" />
        <input 
          v-model="queryParams.customerName" 
          @keyup.enter="handleSearch"
          type="text" 
          placeholder="搜索客户名称..." 
        />
      </div>
      <button class="btn btn-outline" @click="handleSearch">搜索</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>编码</th>
            <th>名称</th>
            <th>类型</th>
            <th>联系人</th>
            <th>电话</th>
            <th>城市</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.customerId">
            <td>{{ item.customerCode }}</td>
            <td class="font-medium">{{ item.customerName }}</td>
            <td>
                <span class="badge badge-info">{{ getTypeName(item.customerType) }}</span>
            </td>
            <td>{{ item.contactPerson }}</td>
            <td>{{ item.phone }}</td>
            <td>{{ item.city }}</td>
            <td>
              <span class="badge" :class="item.status === 1 ? 'badge-success' : 'badge-gray'">
                {{ item.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue" @click="openEdit(item)"><Edit :size="16" /></button>
                <button class="action-btn text-red" @click="handleDelete(item.customerId)"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination" v-if="total > 0">
         <span class="text-sm text-secondary">共 {{ total }} 条记录</span>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑单位' : '新增单位' }}</h3>
        </div>
        <div class="modal-body">
          <div class="form-grid">
             <div class="form-group">
                <label>单位编码</label>
                <input v-model="form.customerCode" type="text" class="form-input" disabled />
             </div>
             <div class="form-group">
                <label>单位名称 <span class="required">*</span></label>
                <input v-model="form.customerName" type="text" class="form-input" />
             </div>
             <div class="form-group">
                <label>类型</label>
                <select v-model="form.customerType" class="form-input">
                    <option v-for="t in customerTypes" :key="t.value" :value="t.value">{{ t.label }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>联系人</label>
                <input v-model="form.contactPerson" type="text" class="form-input" />
             </div>
             <div class="form-group">
                <label>电话</label>
                <input v-model="form.phone" type="text" class="form-input" />
             </div>
             <div class="form-group">
                <label>邮箱</label>
                <input v-model="form.email" type="email" class="form-input" placeholder="例如: contact@example.com" />
             </div>
             <div class="form-group">
                <label>城市</label>
                <input v-model="form.city" type="text" class="form-input" />
             </div>
             <div class="form-group full-width">
                <label>地址</label>
                <input v-model="form.address" type="text" class="form-input" />
             </div>
             <div class="form-group">
                <label>状态</label>
                <select v-model="form.status" class="form-input">
                    <option :value="1">启用</option>
                    <option :value="0">禁用</option>
                </select>
             </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" :disabled="submitting" @click="handleSubmit">保存</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* Common Styles */
.page-container { padding: 0 2rem 2rem; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 1.5rem; }
.page-header h2 { margin: 0; font-size: 1.5rem; }
.subtitle { color: #6b7280; font-size: 0.875rem; margin: 0.25rem 0 0; }
.search-bar { display: flex; gap: 1rem; margin-bottom: 1.5rem; }
.input-icon-wrapper { position: relative; }
.input-icon-wrapper .icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #9ca3af; }
.input-icon-wrapper input { padding: 0.5rem 1rem 0.5rem 2.5rem; border: 1px solid #e5e7eb; border-radius: 0.5rem; width: 250px; }
.table-container { background: white; border-radius: 0.5rem; border: 1px solid #e5e7eb; overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 1rem; text-align: left; border-bottom: 1px solid #e5e7eb;font-size: 0.9rem; }
th { background: #f9fafb; font-weight: 600; color: #6b7280; }
.action-buttons { display: flex; gap: 0.5rem; }
.action-btn { background: none; border: none; cursor: pointer; padding: 4px; }
.text-blue { color: #3b82f6; }
.text-red { color: #ef4444; }
.badge { padding: 2px 8px; border-radius: 9999px; font-size: 0.75rem; font-weight: 500; }
.badge-success { background: #d1fae5; color: #065f46; }
.badge-info { background: #dbeafe; color: #1e40af; }
.badge-gray { background: #f3f4f6; color: #1f2937; }
.pagination { padding: 1rem; border-top: 1px solid #e5e7eb; text-align: right; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal { background: white; border-radius: 0.5rem; width: 600px; max-width: 90%; }
.modal-header { padding: 1rem 1.5rem; border-bottom: 1px solid #e5e7eb; }
.modal-header h3 { margin: 0; }
.modal-body { padding: 1.5rem; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.full-width { grid-column: 1 / -1; }
.form-group label { display: block; margin-bottom: 0.5rem; font-size: 0.875rem; }
.form-input { width: 100%; padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 0.375rem; }
.modal-footer { padding: 1rem 1.5rem; border-top: 1px solid #e5e7eb; display: flex; justify-content: flex-end; gap: 0.75rem; }
.btn-primary { background: #2563eb; color: white; padding: 0.5rem 1rem; border-radius: 0.375rem; border: none; cursor: pointer; display: flex; align-items: center; }
.btn-primary:disabled { opacity: 0.5; }
.btn-outline { background: white; border: 1px solid #d1d5db; padding: 0.5rem 1rem; border-radius: 0.375rem; cursor: pointer; }
.required { color: red; }
</style>
