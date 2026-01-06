<script setup>
import { ref, onMounted, reactive } from 'vue';
import { Search, Eye, X } from 'lucide-vue-next';
import request from '../utils/request';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  productId: null // Backend supports exact match on Id, but maybe we can support other filters later
});

// Meta Data
const products = ref([]);
const warehouses = ref([]);

// Modal State
const showModal = ref(false);
const currentItem = ref({});

const fetchMeta = async () => {
  try {
    const [resProd, resWh] = await Promise.all([
      request.get('/product/list'),
      request.get('/warehouse/list')
    ]);
    if (resProd.code === 200) products.value = resProd.data;
    if (resWh.code === 200) warehouses.value = resWh.data;
  } catch (e) {
    console.error("Meta fetch failed", e);
  }
};

const getProductName = (id) => products.value.find(p => p.productId === id)?.productName || id;
const getWarehouseName = (id) => warehouses.value.find(w => w.warehouseId === id)?.warehouseName || id;

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/inventory/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('Failed to fetch inventory:', error);
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
}

const openDetail = (item) => {
  currentItem.value = {
    ...item,
    ...item,
    // Backend VOs now provide these names, but we keep fallback just in case or for older data
    productName: item.productName || getProductName(item.productId),
    warehouseName: item.warehouseName || getWarehouseName(item.warehouseId),
    locationCode: item.locationCode || '-'
  };
  showModal.value = true;
};

onMounted(() => {
  fetchMeta();
  fetchData();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>库存查询</h2>
        <p class="subtitle">查询当前仓库库存状态</p>
      </div>
    </div>

    <!-- Optional: Search by Product Dropdown if needed, but existing is simple page -->
    <!-- 
    <div class="search-bar">
       ... potentially add filters here ...
    </div> 
    -->

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>货品</th>
            <th>库位</th>
            <th>仓库</th>
            <th>批次号</th>
            <th>总数量</th>
            <th>冻结数量</th>
            <th>可用数量</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.inventoryId">
            <td class="font-medium">{{ item.productName || getProductName(item.productId) }}</td>
            <td>{{ item.locationCode || '-' }}</td>
            <td>{{ item.warehouseName || getWarehouseName(item.warehouseId) }}</td>
            <td>{{ item.batchNo }}</td>
            <td class="font-bold">{{ item.totalQty }}</td>
            <td class="text-secondary">{{ item.frozenQty }}</td>
            <td class="text-primary font-bold">{{ item.availableQty }}</td>
            <td>
              <button class="action-btn text-blue" @click="openDetail(item)">
                <Eye :size="16" style="margin-right: 4px; vertical-align: middle;" />详情
              </button>
            </td>
          </tr>
           <tr v-if="!loading && tableData.length === 0">
            <td colspan="7" class="text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>
       <div class="pagination" v-if="total > 0">
         <span class="text-sm text-secondary">共 {{ total }} 条记录</span>
         <div class="pagination-controls" v-if="total > queryParams.pageSize">
             <button :disabled="queryParams.pageNum <= 1" @click="handlePageChange(queryParams.pageNum - 1)">上一页</button>
             <span>{{ queryParams.pageNum }}</span>
             <button :disabled="queryParams.pageNum * queryParams.pageSize >= total" @click="handlePageChange(queryParams.pageNum + 1)">下一页</button>
         </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>库存详情</h3>
          <button @click="showModal = false"><X :size="20" /></button>
        </div>
        <div class="modal-body">
          <div class="info-grid">
            <div class="info-item">
              <label>货品名称</label>
              <span>{{ currentItem.productName }}</span>
            </div>
            <div class="info-item">
              <label>仓库</label>
              <span>{{ currentItem.warehouseName }}</span>
            </div>
             <div class="info-item">
              <label>库位</label>
              <span>{{ currentItem.locationCode }}</span>
            </div>
            <div class="info-item">
              <label>批次号</label>
              <span>{{ currentItem.batchNo }}</span>
            </div>
             <div class="info-item">
              <label>入库时间</label>
              <span>{{ currentItem.createTime }}</span>
            </div>
          </div>
          <div class="qty-section">
             <div class="qty-box">
                <label>总数量</label>
                <span class="val">{{ currentItem.totalQty }}</span>
             </div>
             <div class="qty-box">
                <label>冻结数量</label>
                <span class="val">{{ currentItem.frozenQty }}</span>
             </div>
             <div class="qty-box highlight">
                <label>可用数量</label>
                <span class="val">{{ currentItem.availableQty }}</span>
             </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="showModal = false">关闭</button>
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
.text-primary { color: var(--primary-color); } /* Fixed variable name from previous snippet which used primary-dark? Assuming var(--primary-color) exists */
.text-secondary { color: var(--text-secondary); }
.font-bold { font-weight: 600; }
.action-btn { background: none; border: none; cursor: pointer; color: #8b5cf6; display: inline-flex; align-items: center; }
.action-btn:hover { opacity: 0.8; }
.text-center { text-align: center; }
.pagination { padding: 1rem; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); }
.pagination-controls button { padding: 0.25rem 0.75rem; border: 1px solid var(--border-color); background: white; border-radius: 0.25rem; cursor: pointer; margin: 0 0.25rem; }
.pagination-controls button:disabled { opacity: 0.5; cursor: not-allowed; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal { background: white; border-radius: 0.5rem; width: 500px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { padding: 1rem 1.5rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.125rem; font-weight: 600; }
.modal-body { padding: 1.5rem; }
.modal-footer { padding: 1rem 1.5rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 0.5rem; }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem; }
.info-item { display: flex; flex-direction: column; gap: 0.25rem; }
.info-item label { font-size: 0.75rem; color: var(--text-secondary); }
.info-item span { font-weight: 500; font-size: 1rem; }

.qty-section { display: flex; gap: 1rem; background: #f9fafb; padding: 1rem; border-radius: 0.5rem; }
.qty-box { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 0.25rem; }
.qty-box label { font-size: 0.75rem; color: var(--text-secondary); }
.qty-box .val { font-size: 1.25rem; font-weight: 600; }
.qty-box.highlight .val { color: var(--primary-color); }
</style>
