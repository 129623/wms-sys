<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, Box, X } from 'lucide-vue-next';
import request from '../utils/request';

const route = useRoute();
const router = useRouter();
const inboundId = route.params.id;

const order = ref({});
const details = ref([]);
const loading = ref(false);
const products = ref({}); // Map: productId -> productName

// Receipt Modal
const showReceiptModal = ref(false);
const receiptForm = ref({
  inboundId: parseInt(inboundId),
  detailId: null,
  locationId: '',
  qty: 0,
  description: ''
});
const currentDetail = ref(null);
const locations = ref([]);

const statusMap = {
  0: '未入库',
  1: '入库中',
  2: '已入库',
  3: '入库失败'
};

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'badge-info';
    case 1: return 'badge-warning';
    case 2: return 'badge-success';
    default: return 'badge-secondary';
  }
};

const fetchLocations = async () => {
    try {
        const res = await request.get('/location/page?pageNum=1&pageSize=100'); // Or list if available
        if (res.code === 200) {
            locations.value = res.data.records;
        }
    } catch (e) {
        console.error(e);
    }
}

const fetchProducts = async () => {
    try {
        const res = await request.get('/product/list');
        if (res.code === 200) {
            products.value = {}; // Reset
            res.data.forEach(p => {
                products.value[p.productId] = p.productName;
            });
        }
    } catch(e) {
        console.error("Fetch products failed", e);
    }
}

const fetchData = async () => {
  loading.value = true;
  try {
    // Fetch order first
    const resOrder = await request.get(`/inbound/${inboundId}`);
    if (resOrder.code === 200) {
        order.value = resOrder.data;
    }
    
    // Fetch details separately so one failure doesn't block the other (or at least better debug)
    const resDetails = await request.get(`/inbound/details/${inboundId}`);
    if (resDetails.code === 200) {
        details.value = resDetails.data;
    }
    
  } catch (error) {
    console.error('Fetch error:', error);
  } finally {
    loading.value = false;
  }
};

const errors = ref({});
const submitting = ref(false);

const openReceiptModal = (detail) => {
    currentDetail.value = detail;
    receiptForm.value.detailId = detail.detailId;
    // Auto-fill with remaining quantity logic
    // But default to empty or 0 to let user confirm, OR fill max for convenience.
    // Let's fill max for convenience as this is a common case
    receiptForm.value.qty = detail.planQty - (detail.receivedQty || 0);
    receiptForm.value.locationId = '';
    errors.value = {};
    showReceiptModal.value = true;
    if(locations.value.length === 0) fetchLocations();
};

const fillRemaining = () => {
    if(currentDetail.value) {
        receiptForm.value.qty = currentDetail.value.planQty - (currentDetail.value.receivedQty || 0);
    }
}

const submitReceipt = async () => {
    errors.value = {};
    
    if(!receiptForm.value.locationId) {
        errors.value.locationId = true;
    }
    
    if(!receiptForm.value.qty || receiptForm.value.qty <= 0) {
        errors.value.qty = true;
        errors.value.qtyMsg = '请输入有效的数量';
    } else if (currentDetail.value) {
        const remaining = currentDetail.value.planQty - (currentDetail.value.receivedQty || 0);
        if (receiptForm.value.qty > remaining) {
            errors.value.qty = true;
            errors.value.qtyMsg = `数量不能超过剩余计划 (${remaining})`;
        }
    }
    
    if(Object.keys(errors.value).length > 0) return;
    
    submitting.value = true;
    try {
        const res = await request.post('/inbound/receipt', receiptForm.value);
        if(res.code === 200) {
            // alert('入库成功'); // Use toast or notification if available, or just close
            showReceiptModal.value = false;
            fetchData(); // Refresh
        } else {
            alert(res.message);
        }
    } catch(e) {
        console.error(e);
        alert('操作失败');
    } finally {
        submitting.value = false;
    }
};

onMounted(() => {
  fetchData();
  fetchProducts();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <button class="back-btn" @click="router.back()">
          <ArrowLeft :size="20" />
        </button>
        <div>
           <h2>入库单详情</h2>
           <p class="subtitle">{{ order.orderNo }}</p>
        </div>
      </div>
      <div>
         <span class="badge" :class="getStatusClass(order.status)">{{ statusMap[order.status] }}</span>
      </div>
    </div>

    <!-- Header Info -->
    <div class="info-card">
        <div class="info-row">
            <div class="info-item">
                <span class="label">仓库 ID</span>
                <span class="value">{{ order.warehouseId }}</span>
            </div>
             <div class="info-item">
                <span class="label">供应商 ID</span>
                <span class="value">{{ order.customerId }}</span>
            </div>
             <div class="info-item">
                <span class="label">入库类型</span>
                <span class="value">{{ order.orderType === 1 ? '采购入库' : '其他' }}</span>
            </div>
            <div class="info-item">
                <span class="label">创建时间</span>
                <span class="value">{{ order.createTime }}</span>
            </div>
        </div>
        <div class="info-row" style="margin-top: 1rem;">
             <div class="info-item full">
                <span class="label">备注</span>
                <span class="value">{{ order.remark || '-' }}</span>
            </div>
        </div>
    </div>

    <!-- Details List -->
    <div class="details-section">
        <h3>入库明细</h3>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>商品</th>
                        <th>计划数量</th>
                        <th>已入库数量</th>
                        <th>进度</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in details" :key="item.detailId">
                        <td>{{ products[item.productId] || item.productId }}</td>
                        <td>{{ item.planQty }}</td>
                        <td>{{ item.receivedQty || 0 }}</td>
                        <td>
                             <div class="progress-bar">
                                <div class="progress" :style="{ width: Math.min(((item.receivedQty || 0) / item.planQty) * 100, 100) + '%' }"></div>
                             </div>
                        </td>
                        <td>
                            <button 
                                v-if="(item.receivedQty || 0) < item.planQty"
                                class="btn btn-sm btn-primary"
                                @click="openReceiptModal(item)"
                            >
                                入库
                            </button>
                             <span v-else class="text-green text-sm">已完成</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    
    <!-- Receipt Modal -->
    <div v-if="showReceiptModal" class="modal-overlay">
        <div class="modal">
            <div class="modal-header">
                <h3>商品入库</h3>
                <button @click="showReceiptModal = false"><X :size="20" /></button>
            </div>
            <div class="modal-body" v-if="currentDetail">
                <div class="product-info-box">
                    <p class="product-name">{{ products[currentDetail.productId] || currentDetail.productId }}</p>
                    <div class="qty-stats">
                        <div class="stat-item">
                            <span class="stat-label">计划数量</span>
                            <span class="stat-value">{{ currentDetail.planQty }}</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-label">已入库</span>
                            <span class="stat-value">{{ currentDetail.receivedQty || 0 }}</span>
                        </div>
                         <div class="stat-item highlight">
                            <span class="stat-label">剩余待收</span>
                            <span class="stat-value">{{ currentDetail.planQty - (currentDetail.receivedQty || 0) }}</span>
                        </div>
                    </div>
                </div>
                
                <div class="form-group">
                    <label>上架库位 <span class="required">*</span></label>
                    <select v-model="receiptForm.locationId" :class="{ 'error': errors.locationId }">
                        <option value="" disabled>请选择存放库位</option>
                        <option v-for="loc in locations" :key="loc.locationId" :value="loc.locationId">
                            {{ loc.locationCode }} ({{ loc.locationName }})
                        </option>
                    </select>
                    <span v-if="errors.locationId" class="error-msg">请选择库位</span>
                </div>
                
                <div class="form-group">
                    <label>本次入库数量 <span class="required">*</span></label>
                    <div class="input-with-action">
                        <input 
                            type="number" 
                            v-model="receiptForm.qty" 
                            :max="currentDetail.planQty - (currentDetail.receivedQty || 0)" 
                            min="1"
                            placeholder="输入数量"
                            :class="{ 'error': errors.qty }"
                        />
                         <button class="btn-text" @click="fillRemaining">全部入库</button>
                    </div>
                     <span v-if="errors.qty" class="error-msg">{{ errors.qtyMsg }}</span>
                     <p class="hint-text">注: 系统限制不能超过剩余计划数量</p>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-outline" @click="showReceiptModal = false">取消</button>
                <button class="btn btn-primary" :disabled="submitting" @click="submitReceipt">
                    {{ submitting ? '提交中...' : '确认入库' }}
                </button>
            </div>
        </div>
    </div>

  </div>
</template>

<style scoped>
.page-container { padding: 2rem; max-width: 1200px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.header-left { display: flex; align-items: center; gap: 1rem; }
.back-btn { background: none; border: none; cursor: pointer; color: var(--text-secondary); transition: color 0.2s; }
.back-btn:hover { color: var(--text-main); }
.page-header h2 { font-size: 1.5rem; font-weight: 700; color: var(--text-main); margin: 0; }
.subtitle { color: var(--text-secondary); font-size: 0.875rem; margin: 0; }

.info-card { background: white; padding: 1.5rem; border-radius: 0.5rem; box-shadow: var(--shadow-sm); margin-bottom: 2rem; }
.info-row { display: flex; flex-wrap: wrap; gap: 2rem; }
.info-item { display: flex; flex-direction: column; gap: 0.25rem; }
.info-item.full { width: 100%; }
.label { font-size: 0.75rem; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em; }
.value { font-size: 1rem; font-weight: 500; color: var(--text-main); }

.details-section h3 { margin-bottom: 1rem; font-size: 1.25rem; }
.table-container { background: white; border-radius: 0.5rem; overflow: hidden; box-shadow: var(--shadow-sm); }
table { width: 100%; border-collapse: collapse; }
th { text-align: left; padding: 1rem; background: #f9fafb; font-size: 0.875rem; color: var(--text-secondary); }
td { padding: 1rem; border-bottom: 1px solid var(--border-color); color: var(--text-main); }

.progress-bar { width: 100px; height: 6px; background: #e5e7eb; border-radius: 3px; overflow: hidden; }
.progress { height: 100%; background: var(--primary-color); transition: width 0.3s; }

.badge { padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; display: inline-block; }
.badge-info { background: #e0f2fe; color: #0369a1; }
.badge-secondary { background: #f3f4f6; color: #4b5563; }
.badge-success { background: #dcfce7; color: #16a34a; }
.badge-warning { background: #fef9c3; color: #a16207; }

.btn-sm { padding: 0.25rem 0.75rem; font-size: 0.75rem; }
.text-green { color: #16a34a; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal { background: white; border-radius: 0.5rem; width: 400px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { padding: 1rem 1.5rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.125rem; }
.modal-body { padding: 1.5rem; }
.modal-footer { padding: 1rem 1.5rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 0.5rem; }

.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.5rem; font-size: 0.875rem; color: var(--text-secondary); }
.form-group select, .form-group input { width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 0.375rem; outline: none; box-sizing: border-box;}
.mb-4 { margin-bottom: 1rem; }
/* Modal enhancements */
.product-info-box { background: #f9fafb; padding: 1rem; border-radius: 0.5rem; margin-bottom: 1.5rem; border: 1px solid var(--border-color); }
.product-name { font-size: 1.125rem; font-weight: 600; color: var(--text-main); margin-bottom: 0.75rem; }
.qty-stats { display: flex; gap: 1.5rem; }
.stat-item { display: flex; flex-direction: column; gap: 0.25rem; }
.stat-label { font-size: 0.75rem; color: var(--text-secondary); text-transform: uppercase; }
.stat-value { font-size: 1.25rem; font-weight: 600; color: var(--text-main); }
.stat-item.highlight .stat-value { color: var(--primary-color); }

.required { color: #ef4444; margin-left: 2px; }
.input-with-action { display: flex; gap: 0.5rem; }
.btn-text { background: none; border: none; color: var(--primary-color); font-size: 0.875rem; cursor: pointer; white-space: nowrap; font-weight: 500; }
.btn-text:hover { text-decoration: underline; }
.hint-text { font-size: 0.75rem; color: var(--text-secondary); margin-top: 0.25rem; }
.error { border-color: #ef4444 !important; }
.error-msg { font-size: 0.75rem; color: #ef4444; margin-top: 0.25rem; display: block; }
</style>
