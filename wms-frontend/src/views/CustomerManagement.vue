<script setup>
import { ref, onMounted, reactive, watch } from 'vue';
import { Search, Plus, Edit, Trash2, PackageOpen, Loader2, X } from 'lucide-vue-next';
import request from '../utils/request';
import { showToast } from '../utils/toast';

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

// 防抖计时器
let debounceTimer = null;

// 监听搜索词变化，实现防抖搜索
watch(() => queryParams.customerName, () => {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    handleSearch();
  }, 300);
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.post('/base/customer/list', queryParams);
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('Failed to fetch customers:', error);
    showToast('获取数据失败', 'error');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.pageNum = 1;
  fetchData();
};

const handlePageChange = (newPage) => {
  queryParams.pageNum = newPage;
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
    const res = await request.post(`/base/customer/delete/${id}`);
    if (res.code === 200) {
      showToast('删除成功', 'success');
      fetchData();
    } else {
      showToast(res.message, 'error');
    }
  } catch (e) {
    console.error(e);
  }
};

const handleSubmit = async () => {
  if (!form.customerName) {
    showToast('请输入客户名称', 'warning');
    return;
  }
  
  submitting.value = true;
  try {
    const url = isEdit.value ? '/base/customer/update' : '/base/customer/add';
    const res = await request.post(url, form);
    
    if (res.code === 200) {
      showToast(isEdit.value ? '更新成功' : '创建成功', 'success');
      showModal.value = false;
      fetchData();
    } else {
      showToast(res.message, 'error');
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
      <button class="btn btn-primary ripple" @click="openAdd">
        <Plus :size="16" style="margin-right: 4px" /> 新增单位
      </button>
    </div>

    <div class="search-bar">
      <div class="input-icon-wrapper">
        <Search :size="18" class="icon" />
        <input 
          v-model="queryParams.customerName" 
          type="text" 
          placeholder="搜索客户名称..." 
          class="search-input"
        />
      </div>
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
            <td colspan="8">
              <div class="loading-state">
                <Loader2 class="animate-spin text-primary" :size="24" />
                <span>加载中...</span>
              </div>
            </td>
          </tr>
          <tr v-else-if="tableData.length === 0">
             <td colspan="8">
                <div class="empty-state">
                   <PackageOpen :size="48" class="text-gray-300 mb-2" />
                   <p>暂无数据</p>
                </div>
             </td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.customerId" class="hover-row">
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
         <span class="page-info">共 {{ total }} 条</span>
         <div class="pagination-actions">
            <button class="btn btn-sm btn-outline" :disabled="queryParams.pageNum === 1" @click="handlePageChange(queryParams.pageNum - 1)">上一页</button>
            <span class="page-cur">{{ queryParams.pageNum }}</span>
            <button class="btn btn-sm btn-outline" :disabled="queryParams.pageNum >= Math.ceil(total / queryParams.pageSize)" @click="handlePageChange(queryParams.pageNum + 1)">下一页</button>
         </div>
      </div>
    </div>

    <!-- Modal with Transition -->
    <Transition name="fade">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal slide-in">
          <div class="modal-header">
            <h3>{{ isEdit ? '编辑单位' : '新增单位' }}</h3>
            <button class="close-btn" @click="showModal = false"><X :size="20" /></button>
          </div>
          <div class="modal-body">
            <div class="form-grid">
               <div class="form-group">
                  <label>单位编码</label>
                  <input v-model="form.customerCode" type="text" class="form-input bg-gray-50 cursor-not-allowed" disabled />
               </div>
               <div class="form-group">
                  <label>单位名称 <span class="required">*</span></label>
                  <input v-model="form.customerName" type="text" class="form-input" placeholder="输入公司全称" />
               </div>
               <div class="form-group">
                  <label>类型</label>
                  <select v-model="form.customerType" class="form-input">
                      <option v-for="t in customerTypes" :key="t.value" :value="t.value">{{ t.label }}</option>
                  </select>
               </div>
               <div class="form-group">
                  <label>联系人</label>
                  <input v-model="form.contactPerson" type="text" class="form-input" placeholder="输入联系人姓名" />
               </div>
               <div class="form-group">
                  <label>电话</label>
                  <input v-model="form.phone" type="text" class="form-input" placeholder="输入联系电话" />
               </div>
               <div class="form-group">
                  <label>邮箱</label>
                  <input v-model="form.email" type="email" class="form-input" placeholder="contact@example.com" />
               </div>
               <div class="form-group">
                  <label>城市</label>
                  <input v-model="form.city" type="text" class="form-input" placeholder="例如：上海" />
               </div>
               <div class="form-group full-width">
                  <label>详细地址</label>
                  <input v-model="form.address" type="text" class="form-input" placeholder="输入街道地址" />
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
            <button class="btn btn-primary" :disabled="submitting" @click="handleSubmit">
               <Loader2 v-if="submitting" class="animate-spin mr-1" :size="16" />
               {{ submitting ? '保存中' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<style scoped>
/* Common Styles */
.page-container { padding: 0 2rem 2rem; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 1.5rem; }
.page-header h2 { margin: 0; font-size: 1.5rem; font-weight: 700; color: #111827; }
.subtitle { color: #6b7280; font-size: 0.875rem; margin: 0.25rem 0 0; }
.search-bar { display: flex; gap: 1rem; margin-bottom: 1.5rem; }
.input-icon-wrapper { position: relative; width: 100%; max-width: 300px; }
.input-icon-wrapper .icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; }
.search-input { 
  width: 100%; 
  padding: 0.625rem 1rem 0.625rem 2.5rem; 
  border: 1px solid #e5e7eb; 
  border-radius: 0.5rem; 
  transition: all 0.2s;
  outline: none;
}
.search-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }

/* Table */
.table-container { background: white; border-radius: 0.75rem; border: 1px solid #e5e7eb; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 1rem 1.5rem; text-align: left; border-bottom: 1px solid #e5e7eb; font-size: 0.875rem; }
th { background: #f9fafb; font-weight: 600; color: #4b5563; }
.hover-row:hover { background: #f9fafb; transition: background 0.15s; }

/* Loading & Empty States */
.loading-state, .empty-state { padding: 4rem 2rem; text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #6b7280; }
.animate-spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.action-buttons { display: flex; gap: 0.5rem; }
.action-btn { background: none; border: none; cursor: pointer; padding: 6px; border-radius: 4px; transition: background 0.2s; }
.action-btn:hover { background: #f3f4f6; }
.text-blue { color: #3b82f6; }
.text-red { color: #ef4444; }
.text-gray-300 { color: #d1d5db; }

/* Badges */
.badge { padding: 2px 10px; border-radius: 9999px; font-size: 0.75rem; font-weight: 500; }
.badge-success { background: #ecfdf5; color: #059669; }
.badge-info { background: #eff6ff; color: #2563eb; }
.badge-gray { background: #f3f4f6; color: #4b5563; }

/* Pagination */
.pagination { padding: 1rem 1.5rem; border-top: 1px solid #e5e7eb; display: flex; justify-content: flex-end; align-items: center; gap: 1rem; }
.page-cur { font-weight: 600; color: #111827; margin: 0 0.5rem; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 50; backdrop-filter: blur(2px); }
.modal { background: white; border-radius: 0.75rem; width: 600px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { padding: 1.25rem 1.5rem; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.125rem; font-weight: 600; color: #111827; }
.close-btn { background: none; border: none; cursor: pointer; color: #9ca3af; transition: color 0.2s; }
.close-btn:hover { color: #4b5563; }
.modal-body { padding: 1.5rem; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; }
.full-width { grid-column: 1 / -1; }
.form-group label { display: block; margin-bottom: 0.5rem; font-size: 0.875rem; font-weight: 500; color: #374151; }
.form-input { width: 100%; padding: 0.625rem; border: 1px solid #d1d5db; border-radius: 0.375rem; transition: border-color 0.2s; font-size: 0.875rem; }
.form-input:focus { border-color: #3b82f6; outline: none; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }
.form-input:disabled { background-color: #f9fafb; cursor: not-allowed; }
.modal-footer { padding: 1.25rem 1.5rem; border-top: 1px solid #e5e7eb; display: flex; justify-content: flex-end; gap: 0.75rem; background: #f9fafb; border-bottom-left-radius: 0.75rem; border-bottom-right-radius: 0.75rem; }

/* Buttons */
.btn { padding: 0.5rem 1rem; border-radius: 0.5rem; border: none; cursor: pointer; font-size: 0.875rem; font-weight: 500; transition: all 0.2s; display: inline-flex; align-items: center; justify-content: center; }
.btn-primary { background: #2563eb; color: white; box-shadow: 0 1px 2px rgba(37, 99, 235, 0.3); }
.btn-primary:hover { background: #1d4ed8; }
.btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }
.btn-outline { background: white; border: 1px solid #d1d5db; color: #374151; }
.btn-outline:hover { background: #f3f4f6; border-color: #9ca3af; }
.btn-sm { padding: 0.25rem 0.75rem; font-size: 0.8125rem; }
.ripple:active { transform: scale(0.98); }

.required { color: #ef4444; margin-left: 2px; }

/* Animations */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-in { animation: slideIn 0.3s ease-out; }
@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
</style>
