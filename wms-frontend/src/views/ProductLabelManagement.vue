<script setup>
import { ref, onMounted, reactive } from 'vue';
import { Search, Plus, Edit, Trash2, X, Save, Tag } from 'lucide-vue-next';
import request from '../utils/request';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
});

// Modal State
const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const form = reactive({
  labelId: null,
  labelCode: '',
  labelName: ''
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/product-label/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records || res.data;
      total.value = res.data.total || tableData.value.length;
    }
  } catch (error) {
    console.error('Failed to fetch labels:', error);
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

const openAddModal = () => {
  isEdit.value = false;
  Object.assign(form, {
    labelId: null,
    labelCode: '',
    labelName: ''
  });
  showModal.value = true;
};

const openEditModal = (item) => {
  isEdit.value = true;
  Object.assign(form, item);
  showModal.value = true;
};

const handleDelete = async (id) => {
  if (!confirm('确定要删除该标签吗？')) return;
  try {
    const res = await request.delete(`/product-label/${id}`);
    if (res.code === 200) {
      fetchData();
    } else {
      alert(res.message);
    }
  } catch (error) {
    console.error(error);
  }
};

const submitForm = async () => {
  if (!form.labelName) return alert('请输入标签名称');
  if (!form.labelCode) return alert('请输入标签编码');
  
  submitting.value = true;
  try {
    const url = isEdit.value ? '/product-label/update' : '/product-label/add';
    const method = isEdit.value ? 'put' : 'post';
    const res = await request[method](url, form);
    
    if (res.code === 200) {
      showModal.value = false;
      fetchData();
    } else {
      alert(res.message);
    }
  } catch (error) {
    console.error(error);
  } finally {
    submitting.value = false;
  }
};

// 预设颜色
const presetColors = [
  '#ef4444', '#f59e0b', '#22c55e', '#3b82f6', '#8b5cf6', 
  '#ec4899', '#6b7280', '#14b8a6', '#f97316', '#a855f7'
];

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>货物标签管理</h2>
        <p class="subtitle">管理货物分类标签</p>
      </div>
      <button class="btn btn-primary" @click="openAddModal">
        <Plus :size="16" style="margin-right: 4px" /> 新增标签
      </button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>标签编码</th>
            <th>标签名称</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="3" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.labelId">
            <td class="font-medium">{{ item.labelCode }}</td>
            <td>{{ item.labelName }}</td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue" @click="openEditModal(item)"><Edit :size="16" /></button>
                <button class="action-btn text-red" @click="handleDelete(item.labelId)"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
           <tr v-if="!loading && tableData.length === 0">
            <td colspan="3" class="text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>
       <div class="pagination" v-if="total > 0">
         <span class="page-info">共 {{ total }} 条</span>
         <button class="btn btn-sm btn-outline" :disabled="queryParams.pageNum === 1" @click="handlePageChange(queryParams.pageNum - 1)">上一页</button>
         <span class="page-info">第 {{ queryParams.pageNum }} 页 / 共 {{ Math.ceil(total / queryParams.pageSize) }} 页</span>
         <button class="btn btn-sm btn-outline" :disabled="queryParams.pageNum >= Math.ceil(total / queryParams.pageSize)" @click="handlePageChange(queryParams.pageNum + 1)">下一页</button>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑标签' : '新增标签' }}</h3>
          <button @click="showModal = false"><X :size="20" /></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>标签编码 <span class="text-red">*</span></label>
            <input type="text" v-model="form.labelCode" placeholder="例如: HOT" :disabled="isEdit" />
          </div>
          <div class="form-group">
            <label>标签名称 <span class="text-red">*</span></label>
            <input type="text" v-model="form.labelName" placeholder="例如: 热销" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" :disabled="submitting" @click="submitForm">
             <Save :size="16" class="mr-2" /> 保存
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
.font-medium { font-weight: 500; }
.pagination { 
    padding: 1rem; 
    border-top: 1px solid #e5e7eb; 
    display: flex; 
    justify-content: flex-end; 
    align-items: center; 
    gap: 1rem; 
}
.page-info { color: #6b7280; font-size: 0.875rem; }
.btn-sm { padding: 0.25rem 0.75rem; font-size: 0.875rem; }

/* Label Preview */
.label-preview { display: flex; align-items: center; gap: 0.5rem; }
.color-display { display: flex; align-items: center; gap: 0.5rem; }
.color-box { width: 20px; height: 20px; border-radius: 0.25rem; border: 1px solid var(--border-color); }

/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal { background: white; border-radius: 0.5rem; width: 500px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { padding: 1rem 1.5rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.125rem; font-weight: 600; }
.modal-body { padding: 1.5rem; }
.modal-footer { padding: 1rem 1.5rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 0.5rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.5rem; font-size: 0.875rem; color: var(--text-secondary); }
.form-group input, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 0.375rem; outline: none; box-sizing: border-box;}
.form-group input:focus, .form-group textarea:focus { border-color: var(--primary-color); }
.form-group textarea { resize: vertical; font-family: inherit; }

/* Color Picker */
.color-picker { display: flex; gap: 0.5rem; align-items: center; }
.color-picker input[type="color"] { width: 50px; height: 38px; border: 1px solid var(--border-color); border-radius: 0.375rem; cursor: pointer; }
.color-picker input[type="text"] { flex: 1; }
.color-presets { display: flex; gap: 0.5rem; margin-top: 0.5rem; flex-wrap: wrap; }
.color-preset { width: 32px; height: 32px; border-radius: 0.375rem; cursor: pointer; border: 2px solid transparent; transition: all 0.2s; }
.color-preset:hover { transform: scale(1.1); }
.color-preset.active { border-color: var(--primary-color); box-shadow: 0 0 0 2px var(--primary-light); }

.mr-2 { margin-right: 0.5rem; }
.text-red { color: #ef4444; }
</style>
