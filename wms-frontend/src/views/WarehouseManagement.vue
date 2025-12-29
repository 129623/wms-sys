<script setup>
import { ref, onMounted, reactive } from 'vue';
import { Search, Plus, Edit, Trash2 } from 'lucide-vue-next';
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
  warehouseId: null,
  warehouseCode: '',
  warehouseName: '',
  address: '',
  principal: '',
  phone: '',
  status: 1
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/warehouse/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('Failed to fetch warehouses:', error);
  } finally {
    loading.value = false;
  }
};

const openAdd = () => {
    isEdit.value = false;
    Object.assign(form, {
        warehouseId: null,
        warehouseCode: 'WH'+Date.now().toString().slice(-6),
        warehouseName: '',
        address: '',
        principal: '',
        phone: '',
        status: 1
    });
    showModal.value = true;
}

const openEdit = (row) => {
    isEdit.value = true;
    Object.assign(form, row);
    showModal.value = true;
}

const handleDelete = async (id) => {
    if(!confirm('确认删除仓库？\n\n⚠️ 警告：这将同时删除关联的所有库区、货架和库位！')) return;
    try {
        const res = await request.delete(`/warehouse/${id}`);
        if(res.code===200) fetchData(); else alert(res.message);
    } catch(e) { console.error(e); }
}

const handleSubmit = async () => {
    if(!form.warehouseName) return alert('请填写仓库名称');
    submitting.value = true;
    try {
        const url = isEdit.value ? '/warehouse/update' : '/warehouse/add';
        const method = isEdit.value ? 'put' : 'post';
        const res = await request[method](url, form);
        if(res.code===200) {
            showModal.value = false;
            fetchData();
        } else {
            alert(res.message);
        }
    } catch(e){ console.error(e); } finally { submitting.value = false; }
}

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>仓库管理</h2>
        <p class="subtitle">管理仓库及库区信息</p>
      </div>
      <button class="btn btn-primary" @click="openAdd">
        <Plus :size="16" style="margin-right: 4px" /> 新增仓库
      </button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>仓库名称</th>
            <th>编码</th>
            <th>地址</th>
            <th>负责人</th>
            <th>联系方式</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.warehouseId">
            <td class="font-medium">{{ item.warehouseName }}</td>
            <td>{{ item.warehouseCode }}</td>
            <td>{{ item.address }}</td>
            <td>{{ item.principal }}</td>
            <td>{{ item.phone }}</td>
            <td><span class="badge" :class="item.status===1 ? 'badge-success' : 'badge-danger'"> {{ item.status===1?'启用':'已删除' }}</span></td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue" @click="openEdit(item)"><Edit :size="16" /></button>
                <button class="action-btn text-red" @click="handleDelete(item.warehouseId)"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
           <tr v-if="!loading && tableData.length === 0">
            <td colspan="7" class="text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>
      <div class="pagination" v-if="total > 0">
         <span class="text-sm text-secondary">共 {{ total }} 条记录</span>
      </div>
    </div>
    
    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
       <div class="modal">
          <div class="modal-header"><h3>{{ isEdit?'编辑':'新增' }}仓库</h3></div>
          <div class="modal-body">
             <div class="form-group">
                <label>仓库名称</label>
                <input v-model="form.warehouseName" class="form-input" />
             </div>
             <div class="form-group">
                <label>仓库编码</label>
                <input v-model="form.warehouseCode" class="form-input" disabled />
             </div>
             <div class="form-group">
                <label>地址</label>
                <input v-model="form.address" class="form-input" />
             </div>
             <div class="form-group">
                <label>负责人</label>
                <input v-model="form.principal" class="form-input" />
             </div>
             <div class="form-group">
                <label>联系方式</label>
                <input v-model="form.phone" class="form-input" />
             </div>
             <div class="form-group">
                <label>状态</label>
                <select v-model="form.status" class="form-input">
                    <option :value="1">启用</option>
                    <option :value="0">禁用</option>
                </select>
             </div>
          </div>
          <div class="modal-footer">
             <button class="btn btn-outline" @click="showModal=false">取消</button>
             <button class="btn btn-primary" :disabled="submitting" @click="handleSubmit">保存</button>
          </div>
       </div>
    </div>
  </div>
</template>

<style scoped>
/* Reuse styles */
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
.pagination { padding: 1rem; display: flex; justify-content: flex-end; border-top: 1px solid var(--border-color); }

.badge { padding: 2px 8px; border-radius: 12px; font-size: 12px; }
.badge-success { background: #d1fae5; color: #065f46; }
.badge-gray { background: #f3f4f6; color: #333; }
.badge-danger { background: #fee2e2; color: #991b1b; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 50; }
.modal { background: white; width: 400px; padding: 20px; border-radius: 8px; }
.modal-header { font-size: 1.1rem; font-weight: bold; margin-bottom: 20px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-size: 0.9rem; }
.form-input { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn-primary { background: #3b82f6; color: white; padding: 8px 16px; border-radius: 4px; border: none; cursor: pointer; display: flex; align-items: center; gap: 4px; }
.btn-outline { background: white; border: 1px solid #ddd; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
</style>
