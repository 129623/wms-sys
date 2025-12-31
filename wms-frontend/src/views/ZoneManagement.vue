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
  zoneName: ''
});

// Meta
const warehouses = ref([]);

// Modal
const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const form = reactive({
  zoneId: null,
  warehouseId: null,
  zoneCode: '',
  zoneName: '',
  zoneType: '',
  status: 1
});

const fetchMeta = async () => {
   try { const res = await request.get('/warehouse/list'); if(res.code===200) warehouses.value = res.data; } catch(e){}
}

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/zone/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) { console.error(error); } finally { loading.value = false; }
};

const handleSearch = () => { queryParams.pageNum = 1; fetchData(); };

const openAdd = () => {
  isEdit.value = false;
  Object.assign(form, { zoneId: null, warehouseId: null, zoneCode: 'Z'+Date.now().toString().slice(-6), zoneName: '', zoneType: '', status: 1 });
  showModal.value = true;
};

const openEdit = (row) => {
  isEdit.value = true;
  Object.assign(form, row);
  showModal.value = true;
};

const handleDelete = async (id) => {
  if(!confirm('确认删除？')) return;
  try {
      const res = await request.delete(`/zone/${id}`);
      if(res.code===200) fetchData(); else alert(res.message);
  } catch(e) { console.error(e); }
}

const handleSubmit = async () => {
  if(!form.zoneName || !form.warehouseId) return alert('请完善信息');
  submitting.value = true;
  try {
      const url = isEdit.value ? '/zone/update' : '/zone/add';
      const method = isEdit.value ? 'put' : 'post';
      const res = await request[method](url, form);
      if(res.code===200) { showModal.value = false; fetchData(); } else { alert(res.message); }
  } catch(e){ console.error(e); } finally { submitting.value = false; }
}

const getWarehouseName = (id) => warehouses.value.find(w=>w.warehouseId===id)?.warehouseName || id;

onMounted(() => { fetchMeta(); fetchData(); });
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div><h2>库区管理</h2><p class="subtitle">管理仓库分区</p></div>
      <button class="btn btn-primary" @click="openAdd"><Plus :size="16" /> 新增库区</button>
    </div>
    
    <div class="search-bar">
       <input v-model="queryParams.zoneName" @keyup.enter="handleSearch" class="search-input" placeholder="库区名称" />
       <button class="btn btn-outline" @click="handleSearch">搜索</button>
    </div>

    <div class="table-container">
       <table>
         <thead><tr><th>所属仓库</th><th>库区编码</th><th>库区名称</th><th>类型</th><th>状态</th><th>操作</th></tr></thead>
         <tbody>
            <tr v-if="loading"><td colspan="6" class="text-center">加载中...</td></tr>
            <tr v-else v-for="item in tableData" :key="item.zoneId">
                <td>{{ getWarehouseName(item.warehouseId) }}</td>
                <td>{{ item.zoneCode }}</td>
                <td class="font-medium">{{ item.zoneName }}</td>
                <td>{{ item.zoneType }}</td>
                <td><span class="badge" :class="item.status===1?'badge-success':'badge-gray'">{{ item.status===1?'启用':'禁用' }}</span></td>
                <td>
                    <button class="action-btn text-blue" @click="openEdit(item)"><Edit :size="16"/></button>
                    <button class="action-btn text-red" @click="handleDelete(item.zoneId)"><Trash2 :size="16"/></button>
                </td>
            </tr>
         </tbody>
       </table>
    </div>
    
    <!-- Pagination -->
    <div class="pagination">
        <button class="btn btn-outline btn-sm" :disabled="queryParams.pageNum <= 1" @click="queryParams.pageNum--; fetchData()">上一页</button>
        <span class="page-info">第 {{ queryParams.pageNum }} 页 / 共 {{ Math.ceil(total / queryParams.pageSize) }} 页 (总数: {{ total }})</span>
        <button class="btn btn-outline btn-sm" :disabled="queryParams.pageNum >= Math.ceil(total / queryParams.pageSize)" @click="queryParams.pageNum++; fetchData()">下一页</button>
    </div>
    
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
       <div class="modal">
          <div class="modal-header"><h3>{{ isEdit?'编辑':'新增' }}库区</h3></div>
          <div class="modal-body">
             <div class="form-group">
                <label>所属仓库</label>
                <select v-model="form.warehouseId" class="form-input">
                    <option v-for="w in warehouses" :key="w.warehouseId" :value="w.warehouseId">{{ w.warehouseName }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>库区名称</label>
                <input v-model="form.zoneName" class="form-input" />
             </div>
             <div class="form-group">
                <label>库区编码</label>
                <input v-model="form.zoneCode" class="form-input" disabled />
             </div>
             <div class="form-group">
                <label>类型</label>
                <input v-model="form.zoneType" class="form-input" placeholder="例如：存储区、拣货区" />
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
             <button class="btn btn-primary" @click="handleSubmit">保存</button>
          </div>
       </div>
    </div>
  </div>
</template>

<style scoped>
/* Simplified shared styles */
.page-container { padding: 0 2rem 2rem; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 1.5rem; }
.page-header h2 { margin: 0; font-size: 1.5rem; }
.subtitle { color: #6b7280; font-size: 0.875rem; margin-top: 0.25rem; }
.search-bar { display: flex; gap: 1rem; margin-bottom: 1rem; }
.search-input { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
.table-container { background: white; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eee; }
th { background: #f9f9f9; font-weight: 600; color: #666; font-size: 0.9rem; }
.action-btn { background: none; border: none; cursor: pointer; padding: 4px; margin-right: 8px; }
.text-blue { color: #3b82f6; } .text-red { color: #ef4444; }
.badge { padding: 2px 8px; border-radius: 12px; font-size: 12px; }
.badge-success { background: #d1fae5; color: #065f46; }
.badge-gray { background: #f3f4f6; color: #333; }
.btn-primary { background: #3b82f6; color: white; padding: 8px 16px; border-radius: 4px; border: none; cursor: pointer; display: flex; align-items: center; gap: 4px; }
.btn-outline { background: white; border: 1px solid #ddd; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
.text-center { text-align: center; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 50; }
.modal { background: white; width: 400px; padding: 20px; border-radius: 8px; }
.modal-header { font-size: 1.1rem; font-weight: bold; margin-bottom: 20px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-size: 0.9rem; }
.form-input { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.pagination { display: flex; justify-content: flex-end; align-items: center; gap: 1rem; margin-top: 1rem; padding: 0 0.5rem; }
.page-info { color: #666; font-size: 0.9rem; }
.btn-sm { padding: 4px 12px; font-size: 0.85rem; }
</style>
