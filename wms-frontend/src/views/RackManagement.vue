<script setup>
import { ref, onMounted, reactive, watch } from 'vue';
import { Search, Plus, Edit, Trash2 } from 'lucide-vue-next';
import request from '../utils/request';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const queryParams = reactive({ pageNum: 1, pageSize: 10, rackCode: '' });

const warehouses = ref([]);
const zones = ref([]);
const filteredZones = ref([]);

const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const form = reactive({
  rackId: null, warehouseId: null, zoneId: null,
  rackCode: '', rackType: '', relatedRackId: null
});

const fetchMeta = async () => {
    try {
        const [wRes, zRes] = await Promise.all([request.get('/warehouse/list'), request.get('/zone/list')]);
        if(wRes.code===200) warehouses.value = wRes.data;
        if(zRes.code===200) zones.value = zRes.data;
    } catch(e){}
}
const fetchData = async () => {
    loading.value = true;
    try {
        const res = await request.get('/rack/page', { params: queryParams });
        if(res.code===200) { tableData.value = res.data.records; total.value = res.data.total; }
    } catch(e){} finally { loading.value = false; }
}

// Watch warehouse change to filter zones in modal
watch(() => form.warehouseId, (newVal) => {
    if(newVal) filteredZones.value = zones.value.filter(z => z.warehouseId === newVal);
    else filteredZones.value = [];
});

const openAdd = () => {
    isEdit.value = false;
    Object.assign(form, { rackId: null, warehouseId: null, zoneId: null, rackCode: 'R'+Date.now().toString().slice(-6), rackType: '' });
    showModal.value = true;
}
const openEdit = (row) => {
    isEdit.value = true;
    Object.assign(form, row);
    showModal.value = true;
}
const handleDelete = async (id) => {
    if(!confirm('确认删除？')) return;
    try { await request.delete(`/rack/${id}`); fetchData(); } catch(e){}
}
const handleSubmit = async () => {
    if(!form.zoneId) return alert('请选择库区');
    submitting.value = true;
    try {
        const url = isEdit.value ? '/rack/update' : '/rack/add';
        const method = isEdit.value ? 'put' : 'post';
        await request[method](url, form);
        showModal.value = false; fetchData();
    } catch(e){} finally { submitting.value = false; }
}

const getMetaName = (list, id, key, nameKey) => list.find(i=>i[key]===id)?.[nameKey] || id;

onMounted(() => { fetchMeta(); fetchData(); });
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div><h2>货架管理</h2><p class="subtitle">管理仓库货架</p></div>
      <button class="btn btn-primary" @click="openAdd"><Plus :size="16" /> 新增货架</button>
    </div>
    
    <div class="table-container">
       <table>
         <thead><tr><th>所属仓库</th><th>所属库区</th><th>货架编码</th><th>类型</th><th>操作</th></tr></thead>
         <tbody>
            <tr v-if="loading"><td colspan="5" class="text-center">加载中...</td></tr>
            <tr v-else v-for="item in tableData" :key="item.rackId">
                <td>{{ getMetaName(warehouses, item.warehouseId, 'warehouseId', 'warehouseName') }}</td>
                <td>{{ getMetaName(zones, item.zoneId, 'zoneId', 'zoneName') }}</td>
                <td class="font-medium">{{ item.rackCode }}</td>
                <td>{{ item.rackType || '-' }}</td>
                <td>
                    <button class="action-btn text-blue" @click="openEdit(item)"><Edit :size="16"/></button>
                    <button class="action-btn text-red" @click="handleDelete(item.rackId)"><Trash2 :size="16"/></button>
                </td>
            </tr>
         </tbody>
       </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
       <div class="modal">
          <div class="modal-header"><h3>{{ isEdit?'编辑':'新增' }}货架</h3></div>
          <div class="modal-body">
             <div class="form-group">
                <label>所属仓库</label>
                <select v-model="form.warehouseId" class="form-input">
                    <option v-for="w in warehouses" :key="w.warehouseId" :value="w.warehouseId">{{ w.warehouseName }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>所属库区</label>
                <select v-model="form.zoneId" class="form-input" :disabled="!form.warehouseId">
                    <option v-for="z in filteredZones" :key="z.zoneId" :value="z.zoneId">{{ z.zoneName }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>货架编码</label>
                <input v-model="form.rackCode" class="form-input" disabled />
             </div>
             <div class="form-group">
                <label>货架类型</label>
                <input v-model="form.rackType" class="form-input" placeholder="例如：重型货架" />
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
/* Reuse styles from ZoneManagement */
.page-container { padding: 0 2rem 2rem; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 1.5rem; }
.page-header h2 { margin: 0; font-size: 1.5rem; }
.subtitle { color: #6b7280; font-size: 0.875rem; margin-top: 0.25rem; }
.table-container { background: white; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eee; }
th { background: #f9f9f9; font-weight: 600; color: #666; font-size: 0.9rem; }
.action-btn { background: none; border: none; cursor: pointer; padding: 4px; margin-right: 8px; }
.text-blue { color: #3b82f6; } .text-red { color: #ef4444; }
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
</style>
