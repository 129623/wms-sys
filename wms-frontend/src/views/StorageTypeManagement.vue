<script setup>
import { ref, onMounted, reactive } from 'vue';
import { Search, Plus, Edit, Trash2, X, Save } from 'lucide-vue-next';
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
  typeId: null,
  typeCode: '',
  typeName: ''
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/storage-type/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records || res.data;
      total.value = res.data.total || tableData.value.length;
    }
  } catch (error) {
    console.error('Failed to fetch storage types:', error);
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
    typeId: null,
    typeCode: '',
    typeName: ''
  });
  showModal.value = true;
};

const openEditModal = (item) => {
  isEdit.value = true;
  Object.assign(form, item);
  showModal.value = true;
};

const handleDelete = async (id) => {
  if (!confirm('确定要删除该存储类型吗？')) return;
  try {
    const res = await request.delete(`/storage-type/${id}`);
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
  if (!form.typeName) return alert('请输入类型名称');
  if (!form.typeCode) return alert('请输入类型编码');
  
  submitting.value = true;
  try {
    const url = isEdit.value ? '/storage-type/update' : '/storage-type/add';
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

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>存储类型管理</h2>
        <p class="subtitle">管理货物存储类型及存储条件</p>
      </div>
      <button class="btn btn-primary" @click="openAddModal">
        <Plus :size="16" style="margin-right: 4px" /> 新增类型
      </button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>类型编码</th>
            <th>类型名称</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="4" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.typeId">
            <td class="font-medium">{{ item.typeCode }}</td>
            <td>{{ item.typeName }}</td>
            <td>{{ item.createTime || '-' }}</td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue" @click="openEditModal(item)"><Edit :size="16" /></button>
                <button class="action-btn text-red" @click="handleDelete(item.typeId)"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
           <tr v-if="!loading && tableData.length === 0">
            <td colspan="4" class="text-center">暂无数据</td>
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
          <h3>{{ isEdit ? '编辑存储类型' : '新增存储类型' }}</h3>
          <button @click="showModal = false"><X :size="20" /></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>类型编码 <span class="text-red">*</span></label>
            <input type="text" v-model="form.typeCode" placeholder="例如: NORMAL" :disabled="isEdit" />
          </div>
          <div class="form-group">
            <label>类型名称 <span class="text-red">*</span></label>
            <input type="text" v-model="form.typeName" placeholder="例如: 常温存储" />
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
.pagination { padding: 1rem; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); }
.pagination-controls button { padding: 0.25rem 0.75rem; border: 1px solid var(--border-color); background: white; border-radius: 0.25rem; cursor: pointer; margin: 0 0.25rem; }
.pagination-controls button:disabled { opacity: 0.5; cursor: not-allowed; }

/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal { background: white; border-radius: 0.5rem; width: 500px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { padding: 1rem 1.5rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.125rem; font-weight: 600; }
.modal-body { padding: 1.5rem; }
.mod.badge-gray { background: #f3f4f6; color: #1f2937; }
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
.modal-footer { padding: 1rem 1.5rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 0.5rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.5rem; font-size: 0.875rem; color: var(--text-secondary); }
.form-group input, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 0.375rem; outline: none; box-sizing: border-box;}
.form-group input:focus, .form-group textarea:focus { border-color: var(--primary-color); }
.form-group textarea { resize: vertical; font-family: inherit; }
.mr-2 { margin-right: 0.5rem; }
.text-red { color: #ef4444; }
</style>
