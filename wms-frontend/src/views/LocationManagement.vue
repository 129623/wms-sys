<script setup>
import { ref, onMounted, reactive, watch } from 'vue';
import { Search, Plus, Edit, Trash2 } from 'lucide-vue-next';
import request from '../utils/request';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const queryParams = reactive({ pageNum: 1, pageSize: 10, locationCode: '' });

const warehouses = ref([]);
const zones = ref([]);
const racks = ref([]);
const filteredZones = ref([]);
const filteredRacks = ref([]);

const showModal = ref(false);
const isEdit = ref(false);
const showBatchModal = ref(false); // Batch Add Modal
const submitting = ref(false);
const form = reactive({
  locationId: null, warehouseId: null, zoneId: null, rackId: null,
  locationCode: '', rowNo: null, layerNo: null, status: 1,
  maxWeight: 0.00, maxVolume: 0.00
});

const batchForm = reactive({
    warehouseId: null, zoneId: null, rackId: null,
    rowStart: 1, rowEnd: 1, layerStart: 1, layerEnd: 1
});

// Meta logic
const fetchMeta = async () => {
    try {
        const [wRes, zRes, rRes] = await Promise.all([
            request.get('/warehouse/list'),
            request.get('/zone/list'),
            request.get('/rack/list')
        ]);
        if(wRes.code===200) warehouses.value = wRes.data;
        if(zRes.code===200) zones.value = zRes.data;
        if(rRes.code===200) racks.value = rRes.data;
    } catch(e){}
}
const fetchData = async () => {
    loading.value = true;
    try {
        const res = await request.get('/location/page', { params: queryParams });
        if(res.code===200) { tableData.value = res.data.records; total.value = res.data.total; }
    } catch(e){} finally { loading.value = false; }
}

// Cascading filters
const updateZones = (wid, targetRef) => {
    if(wid) targetRef.value = zones.value.filter(z => z.warehouseId === wid);
    else targetRef.value = [];
}
const updateRacks = (zid, targetRef) => {
    if(zid) targetRef.value = racks.value.filter(r => r.zoneId === zid);
    else targetRef.value = [];
}

watch(() => form.warehouseId, (val) => updateZones(val, filteredZones));
watch(() => form.zoneId, (val) => updateRacks(val, filteredRacks));

watch(() => batchForm.warehouseId, (val) => updateZones(val, filteredZones)); // Reuse same filtered list ok? No, careful. 
// Ideally separate filtered lists or just dynamic. For simplicity, reusing logic but beware conflict if both modals open (impossible).

const openAdd = () => {
    isEdit.value = false;
    Object.assign(form, { 
        locationId: null, warehouseId: null, zoneId: null, rackId: null, 
        locationCode: '', rowNo: 1, layerNo: 1, status: 1,
        maxWeight: 0.00, maxVolume: 0.00
    });
    showModal.value = true;
}
const openBatchAdd = () => {
    Object.assign(batchForm, { warehouseId: null, zoneId: null, rackId: null, rowStart: 1, rowEnd: 5, layerStart: 1, layerEnd: 3 });
    showBatchModal.value = true;
}

const handleDelete = async (id) => {
    if(!confirm('确认删除？')) return;
    try { await request.delete(`/location/${id}`); fetchData(); } catch(e){}
}
const handleSubmit = async () => {
    // Auto generate code if empty? Or required? Usually Location Code = Zone-Rack-Row-Layer
    // Let's simple format
    if(!form.rackId || !form.rowNo || !form.layerNo) return alert('请完善信息');
    
    // Auto format code: {RackCode}-{Row}-{Layer} 
    // We need RackCode. 
    const rack = racks.value.find(r => r.rackId === form.rackId);
    if(rack) form.locationCode = `${rack.rackCode}-${form.rowNo}-${form.layerNo}`;
    
    submitting.value = true;
    try {
        const url = isEdit.value ? '/location/update' : '/location/add';
        const method = isEdit.value ? 'put' : 'post';
        await request[method](url, form);
        showModal.value = false; fetchData();
    } catch(e){} finally { submitting.value = false; }
}

const handleBatchSubmit = async () => {
     if(!batchForm.rackId) return alert('请选择货架');
     submitting.value = true;
     try {
         await request.post('/location/batchAdd', batchForm);
         showBatchModal.value = false; fetchData();
     } catch(e) { console.error(e); } finally { submitting.value = false; }
}

const getMetaName = (list, id, key, nameKey) => list.find(i=>i[key]===id)?.[nameKey] || id;

onMounted(() => { fetchMeta(); fetchData(); });
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div><h2>库位管理</h2><p class="subtitle">管理货架储位</p></div>
      <div class="header-actions">
           <button class="btn btn-outline" @click="openBatchAdd" style="margin-right: 10px">批量生成</button>
           <button class="btn btn-primary" @click="openAdd"><Plus :size="16" /> 新增库位</button>
      </div>
    </div>
    
    <div class="table-container">
       <table>
         <thead><tr><th>仓库</th><th>库区</th><th>货架</th><th>库位编码</th><th>行</th><th>层</th><th>限重</th><th>限体积</th><th>操作</th></tr></thead>
         <tbody>
            <tr v-if="loading"><td colspan="7" class="text-center">加载中...</td></tr>
            <tr v-else v-for="item in tableData" :key="item.locationId">
                <td>{{ getMetaName(warehouses, item.warehouseId, 'warehouseId', 'warehouseName') }}</td>
                <td>{{ getMetaName(zones, item.zoneId, 'zoneId', 'zoneName') }}</td>
                <td>{{ getMetaName(racks, item.rackId, 'rackId', 'rackCode') }}</td>
                <td class="font-medium">{{ item.locationCode }}</td>
                <td>{{ item.rowNo }}</td>
                <td>{{ item.layerNo }}</td>
                <td>{{ item.maxWeight ? item.maxWeight + ' kg' : '-' }}</td>
                <td>{{ item.maxVolume ? item.maxVolume + ' m³' : '-' }}</td>
                <td>
                    <button class="action-btn text-red" @click="handleDelete(item.locationId)"><Trash2 :size="16"/></button>
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

    <!-- Add Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
       <div class="modal">
          <div class="modal-header"><h3>新增库位</h3></div>
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
                <label>所属货架</label>
                <select v-model="form.rackId" class="form-input" :disabled="!form.zoneId">
                    <option v-for="r in filteredRacks" :key="r.rackId" :value="r.rackId">{{ r.rackCode }}</option>
                </select>
             </div>
             <div class="two-col">
                 <div class="form-group">
                    <label>行号</label>
                    <input v-model.number="form.rowNo" type="number" class="form-input" />
                 </div>
                 <div class="form-group">
                    <label>层号</label>
                    <input v-model.number="form.layerNo" type="number" class="form-input" />
                 </div>
             </div>
             <div class="two-col">
                 <div class="form-group">
                    <label>最大重量 (kg)</label>
                    <input v-model.number="form.maxWeight" type="number" step="0.01" class="form-input" placeholder="0.00" />
                 </div>
                 <div class="form-group">
                    <label>最大体积 (m³)</label>
                    <input v-model.number="form.maxVolume" type="number" step="0.01" class="form-input" placeholder="0.00" />
                 </div>
             </div>
          </div>
          <div class="modal-footer">
             <button class="btn btn-outline" @click="showModal=false">取消</button>
             <button class="btn btn-primary" @click="handleSubmit">保存</button>
          </div>
       </div>
    </div>
    
    <!-- Batch Modal -->
     <div v-if="showBatchModal" class="modal-overlay" @click.self="showBatchModal=false">
       <div class="modal">
          <div class="modal-header"><h3>批量生成库位</h3></div>
          <div class="modal-body">
              <div class="form-group">
                <label>所属仓库</label>
                <select v-model="batchForm.warehouseId" class="form-input">
                    <option v-for="w in warehouses" :key="w.warehouseId" :value="w.warehouseId">{{ w.warehouseName }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>所属库区</label>
                <select v-model="batchForm.zoneId" class="form-input" :disabled="!batchForm.warehouseId">
                    <option v-for="z in filteredZones" :key="z.zoneId" :value="z.zoneId">{{ z.zoneName }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>所属货架</label>
                <select v-model="batchForm.rackId" class="form-input" :disabled="!batchForm.zoneId">
                    <option v-for="r in filteredRacks" :key="r.rackId" :value="r.rackId">{{ r.rackCode }}</option>
                </select>
             </div>
             <div class="two-col">
                 <div class="form-group"><label>起始行</label><input v-model.number="batchForm.rowStart" type="number" class="form-input"/></div>
                 <div class="form-group"><label>结束行</label><input v-model.number="batchForm.rowEnd" type="number" class="form-input"/></div>
             </div>
              <div class="two-col">
                 <div class="form-group"><label>起始层</label><input v-model.number="batchForm.layerStart" type="number" class="form-input"/></div>
                 <div class="form-group"><label>结束层</label><input v-model.number="batchForm.layerEnd" type="number" class="form-input"/></div>
             </div>
          </div>
          <div class="modal-footer">
             <button class="btn btn-outline" @click="showBatchModal=false">取消</button>
             <button class="btn btn-primary" @click="handleBatchSubmit">生成</button>
          </div>
       </div>
    </div>

  </div>
</template>

<style scoped>
/* Reuse styles */
.page-container { padding: 0 2rem 2rem; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 1.5rem; }
.page-header h2 { margin: 0; font-size: 1.5rem; }
.subtitle { color: #6b7280; font-size: 0.875rem; margin-top: 0.25rem; }
.table-container { background: white; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eee; }
th { background: #f9f9f9; font-weight: 600; color: #666; font-size: 0.9rem; }
.action-btn { background: none; border: none; cursor: pointer; padding: 4px; margin-right: 8px; }
.text-red { color: #ef4444; }
.btn-primary { background: #3b82f6; color: white; padding: 8px 16px; border-radius: 4px; border: none; cursor: pointer; display: flex; align-items: center; gap: 4px; }
.btn-outline { background: white; border: 1px solid #ddd; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
.text-center { text-align: center; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 50; }
.modal { background: white; width: 450px; padding: 20px; border-radius: 8px; }
.modal-header { font-size: 1.1rem; font-weight: bold; margin-bottom: 20px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-size: 0.9rem; }
.form-input { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.pagination { display: flex; justify-content: flex-end; align-items: center; gap: 1rem; margin-top: 1rem; padding: 0 0.5rem; }
.page-info { color: #666; font-size: 0.9rem; }
.btn-sm { padding: 4px 12px; font-size: 0.85rem; }
</style>
