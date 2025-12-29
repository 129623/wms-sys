<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Save, ArrowLeft, Plus, Trash2 } from 'lucide-vue-next';
import request from '../utils/request';

const router = useRouter();
const loading = ref(false);

const form = reactive({
  warehouseId: '',
  customerId: '',
  orderType: 1, // Default 1: Purchase Inbound
  remark: '',
  details: []
});

const warehouses = ref([]);
const customers = ref([]);
const products = ref([]);

const fetchOptions = async () => {
  try {
    const [resWh, resCust, resProd] = await Promise.all([
      request.get('/warehouse/list'),
      request.post('/base/customer/list', { pageNum: 1, pageSize: 100 }), // Using post list for search but here we want logic
      request.get('/product/list') // Need to check if this exists or use /page
    ]);
    
    if (resWh.code === 200) warehouses.value = resWh.data;
    if (resCust.code === 200) customers.value = resCust.data.records;
    if (resProd.code === 200) products.value = resProd.data; // Assuming /product/list returns list
  } catch (error) {
    console.error('Failed to load options', error);
  }
};

const addDetail = () => {
  form.details.push({ productId: '', planQty: 1 });
};

const removeDetail = (index) => {
  form.details.splice(index, 1);
};

const submitForm = async () => {
  if (!form.warehouseId || !form.details.length) {
    alert('请填写完整信息');
    return;
  }
  
  loading.value = true;
  try {
    const res = await request.post('/inbound/add', form);
    if (res.code === 200) {
      router.push('/inbound');
    } else {
      alert(res.message || 'Error');
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchOptions();
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
           <h2>新增入库单</h2>
           <p class="subtitle">创建新的入库任务</p>
        </div>
      </div>
      <button class="btn btn-primary" :disabled="loading" @click="submitForm">
        <Save :size="16" class="mr-2" /> 保存订单
      </button>
    </div>

    <div class="form-card">
      <div class="grid-form">
        <div class="form-group">
          <label>入库仓库</label>
          <select v-model="form.warehouseId">
            <option value="" disabled>请选择仓库</option>
            <option v-for="w in warehouses" :key="w.warehouseId" :value="w.warehouseId">{{ w.warehouseName }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>供应商 / 客户</label>
          <select v-model="form.customerId">
            <option value="" disabled>请选择供应商</option>
            <option v-for="c in customers" :key="c.customerId" :value="c.customerId">{{ c.customerName }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>入库类型</label>
          <select v-model="form.orderType">
            <option :value="1">采购入库</option>
            <option :value="2">退货入库</option>
            <option :value="3">调拨入库</option>
          </select>
        </div>
        <div class="form-group full-width">
          <label>备注</label>
          <textarea v-model="form.remark" rows="2" placeholder="填写备注信息..."></textarea>
        </div>
      </div>

      <div class="details-section">
        <div class="details-header">
          <h3>入库明细</h3>
          <button class="btn btn-outline btn-sm" @click="addDetail">
            <Plus :size="16" /> 添加商品
          </button>
        </div>
        
        <table class="details-table">
          <thead>
            <tr>
              <th>商品名称</th>
              <th width="150">计划数量</th>
              <th width="80">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in form.details" :key="index">
              <td>
                <select v-model="item.productId" class="table-input">
                   <option value="" disabled>选择商品</option>
                   <option v-for="p in products" :key="p.productId" :value="p.productId">
                     {{ p.productName }} ({{ p.productCode }})
                   </option>
                </select>
              </td>
              <td>
                <input type="number" v-model="item.planQty" min="1" class="table-input" />
              </td>
              <td class="text-center">
                <button class="icon-btn text-red" @click="removeDetail(index)">
                  <Trash2 :size="16" />
                </button>
              </td>
            </tr>
            <tr v-if="form.details.length === 0">
               <td colspan="3" class="text-center text-muted">暂无明细，请添加商品</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container { padding: 2rem; max-width: 1000px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.header-left { display: flex; align-items: center; gap: 1rem; }
.back-btn { background: none; border: none; cursor: pointer; color: var(--text-secondary); transition: color 0.2s; }
.back-btn:hover { color: var(--text-main); }
.page-header h2 { font-size: 1.5rem; font-weight: 700; color: var(--text-main); margin: 0; }
.subtitle { color: var(--text-secondary); font-size: 0.875rem; margin: 0; }

.form-card { background: white; border-radius: 1rem; padding: 2rem; box-shadow: var(--shadow-sm); }
.grid-form { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1.5rem; margin-bottom: 2rem; }
.form-group { display: flex; flexDirection: column; gap: 0.5rem; }
.form-group.full-width { grid-column: span 2; }
.form-group label { font-size: 0.875rem; font-weight: 500; color: var(--text-secondary); }
.form-group select, .form-group textarea, .form-group input { padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 0.5rem; font-size: 0.875rem; outline: none; transition: border-color 0.2s; }
.form-group select:focus, .form-group textarea:focus, .form-group input:focus { border-color: var(--primary-color); }

.details-section { border-top: 1px solid var(--border-color); padding-top: 2rem; }
.details-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.details-header h3 { font-size: 1.125rem; font-weight: 600; color: var(--text-main); margin: 0; }

.details-table { width: 100%; border-collapse: collapse; }
.details-table th { text-align: left; padding: 0.75rem; font-size: 0.875rem; color: var(--text-secondary); background: #f9fafb; border-bottom: 1px solid var(--border-color); }
.details-table td { padding: 0.75rem; border-bottom: 1px solid var(--border-color); }
.table-input { width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 0.375rem; outline: none; }
.table-input:focus { border-color: var(--primary-color); }
.btn-sm { padding: 0.4rem 0.8rem; font-size: 0.8rem; display: flex; align-items: center; gap: 0.25rem; }
.text-center { text-align: center; }
.text-red { color: #ef4444; }
.text-muted { color: #9ca3af; padding: 2rem; }
.mr-2 { margin-right: 0.5rem; }
</style>
