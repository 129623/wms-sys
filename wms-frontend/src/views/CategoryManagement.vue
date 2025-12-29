<script setup>
import { ref, onMounted, reactive } from 'vue';
import { Plus, Edit, Trash2, X, Save } from 'lucide-vue-next';
import request from '../utils/request';

const loading = ref(false);
const tableData = ref([]);

// Modal State
const showModal = ref(false);
const isEdit = ref(false);
const form = reactive({
  categoryId: null,
  categoryName: ''
});
const submitting = ref(false);

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/category/list');
    if (res.code === 200) {
      tableData.value = res.data;
    }
  } catch (error) {
    console.error('Failed to fetch categories:', error);
  } finally {
    loading.value = false;
  }
};

const openAddModal = () => {
  isEdit.value = false;
  form.categoryId = null;
  form.categoryName = '';
  showModal.value = true;
};

const openEditModal = (item) => {
  isEdit.value = true;
  form.categoryId = item.categoryId;
  form.categoryName = item.categoryName;
  showModal.value = true;
};

const handleDelete = async (id) => {
  if (!confirm('确定要删除该分类吗？')) return;
  try {
    const res = await request.delete(`/category/${id}`);
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
  if (!form.categoryName) return alert('请输入分类名称');
  
  submitting.value = true;
  try {
    const url = isEdit.value ? '/category/update' : '/category/add';
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
        <h2>分类管理</h2>
        <p class="subtitle">管理货物分类信息</p>
      </div>
      <button class="btn btn-primary" @click="openAddModal">
        <Plus :size="16" style="margin-right: 4px" /> 新增分类
      </button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>分类名称</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="4" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.categoryId">
            <td>{{ item.categoryId }}</td>
            <td class="font-medium">{{ item.categoryName }}</td>
            <td>{{ item.createTime }}</td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue" @click="openEditModal(item)"><Edit :size="16" /></button>
                <button class="action-btn text-red" @click="handleDelete(item.categoryId)"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
           <tr v-if="!loading && tableData.length === 0">
            <td colspan="4" class="text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑分类' : '新增分类' }}</h3>
          <button @click="showModal = false"><X :size="20" /></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>分类名称</label>
            <input type="text" v-model="form.categoryName" placeholder="例如: 电子产品" />
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

/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal { background: white; border-radius: 0.5rem; width: 400px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { padding: 1rem 1.5rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.125rem; font-weight: 600; }
.modal-body { padding: 1.5rem; }
.modal-footer { padding: 1rem 1.5rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 0.5rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.5rem; font-size: 0.875rem; color: var(--text-secondary); }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 0.375rem; outline: none; box-sizing: border-box;}
.form-group input:focus { border-color: var(--primary-color); } 
.mr-2 { margin-right: 0.5rem; }
</style>
