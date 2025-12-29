<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Save, Plus, Trash2, ArrowLeft } from 'lucide-vue-next';
import request from '../utils/request';

const router = useRouter();
const loading = ref(false);
const submitting = ref(false);

// Form Data
const form = reactive({
  warehouseId: null,
  customerId: null,
  orderType: 1, // Default 1: Sales Outbound
  receiverName: '',
  receiverPhone: '',
  address: '',
  remark: '',
  details: []
});

// Meta Data
const warehouses = ref([]);
const customers = ref([]); // Reuse customer API assuming it exists, or just mock
const products = ref([]);

// UI State
const showProductSelector = ref(false);

const orderTypes = [
  { label: '销售出库', value: 1 },
  { label: '其他出库', value: 2 },
  { label: '调拨出库', value: 3 }
];

const fetchMeta = async () => {
  try {
    const [resWh, resProd] = await Promise.all([
      request.get('/warehouse/list'),
      request.get('/product/list'),
      // Assuming customer API exists or we skip
      // request.get('/customer/list') 
    ]);
    if (resWh.code === 200) warehouses.value = resWh.data || [];
    if (resProd.code === 200) products.value = resProd.data || [];
    // Mock customers if API missing
    customers.value = [
      { customerId: 1, customerName: '示例客户A' },
      { customerId: 2, customerName: '示例客户B' }
    ];
  } catch (e) {
    console.error("Meta fetch failed", e);
  }
};

const addProductLine = () => {
  form.details.push({
    productId: null,
    planQty: 1
  });
};

const removeProductLine = (index) => {
  form.details.splice(index, 1);
};

const getProductSpec = (id) => {
  const p = products.value.find(x => x.productId === id);
  return p ? p.spec : '-';
};

const getProductUnit = (id) => {
    // Ideally we map unitId to name, but for now just placeholder or skip
    return '-'; 
}

const handleSubmit = async () => {
  if (!form.warehouseId) return alert('请选择仓库');
  if (form.details.length === 0) return alert('请至少添加一行明细');
  if (form.details.some(d => !d.productId || d.planQty <= 0)) return alert('请完善明细信息');

  submitting.value = true;
  try {
    // Map details to match DTO
    const payload = {
      ...form,
      details: form.details.map(d => ({
        productId: d.productId,
        planQty: d.planQty
      }))
    };
    
    const res = await request.post('/outbound/add', payload);
    if (res.code === 200) {
      // alert('创建成功');
      router.push('/outbound');
    } else {
      alert(res.message || '创建失败');
    }
  } catch (e) {
    console.error(e);
    alert('提交异常');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  fetchMeta();
  // Default first warehouse if available
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
          <h2>新增出库单</h2>
          <p class="subtitle">创建新的出库任务</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" :disabled="submitting" @click="handleSubmit">
          <Save :size="18" style="margin-right: 6px" />
          {{ submitting ? '提交中...' : '保存单据' }}
        </button>
      </div>
    </div>

    <div class="form-container">
      <!-- Master Info -->
      <div class="card master-info">
        <h3 class="card-title">基本信息</h3>
        <div class="form-grid">
          <div class="form-group">
            <label>出库仓库 <span class="required">*</span></label>
            <select v-model="form.warehouseId" class="form-input">
              <option :value="null" disabled>请选择仓库</option>
              <option v-for="w in warehouses" :key="w.warehouseId" :value="w.warehouseId">
                {{ w.warehouseName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>业务类型</label>
            <select v-model="form.orderType" class="form-input">
              <option v-for="t in orderTypes" :key="t.value" :value="t.value">{{ t.label }}</option>
            </select>
          </div>
           <!-- Optional fields -->
          <div class="form-group">
            <label>客户</label>
             <select v-model="form.customerId" class="form-input">
              <option :value="null">请选择客户</option>
              <option v-for="c in customers" :key="c.customerId" :value="c.customerId">
                {{ c.customerName }}
              </option>
            </select>
          </div>
           <div class="form-group">
            <label>收货人</label>
            <input v-model="form.receiverName" type="text" class="form-input" placeholder="输入姓名" />
          </div>
           <div class="form-group">
            <label>联系电话</label>
            <input v-model="form.receiverPhone" type="text" class="form-input" placeholder="输入电话" />
          </div>
           <div class="form-group full-width">
            <label>收货地址</label>
            <input v-model="form.address" type="text" class="form-input" placeholder="输入详细地址" />
          </div>
          <div class="form-group full-width">
            <label>备注</label>
            <textarea v-model="form.remark" class="form-input" rows="2" placeholder="备注信息..."></textarea>
          </div>
        </div>
      </div>

      <!-- Details Info -->
      <div class="card details-info">
        <div class="card-header-row">
          <h3 class="card-title">出库明细</h3>
          <button class="btn btn-outline" @click="addProductLine">
            <Plus :size="16" style="margin-right: 4px" /> 添加行
          </button>
        </div>
        
        <table class="details-table">
          <thead>
            <tr>
              <th width="40%">产品</th>
              <th>规格</th>
              <th width="15%">计划数量</th>
              <th width="10%">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in form.details" :key="idx">
              <td>
                <select v-model="row.productId" class="form-input table-input">
                  <option :value="null" disabled>选择产品</option>
                  <option v-for="p in products" :key="p.productId" :value="p.productId">
                    {{ p.productName }} ({{ p.skuCode }})
                  </option>
                </select>
              </td>
              <td class="text-secondary">{{ getProductSpec(row.productId) }}</td>
              <td>
                <input v-model.number="row.planQty" type="number" min="1" class="form-input table-input" />
              </td>
              <td class="text-center">
                <button class="icon-btn text-red" @click="removeProductLine(idx)">
                  <Trash2 :size="18" />
                </button>
              </td>
            </tr>
            <tr v-if="form.details.length === 0">
              <td colspan="4" class="empty-text">请添加出库明细</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container { padding: 0 2rem 2rem; max-width: 1200px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.header-left { display: flex; align-items: center; gap: 1rem; }
.back-btn { background: white; border: 1px solid var(--border-color); width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; color: var(--text-secondary); transition: all 0.2s; }
.back-btn:hover { background: #f3f4f6; color: var(--text-main); }
.page-header h2 { font-size: 1.5rem; font-weight: 700; color: var(--text-main); margin: 0; }
.subtitle { color: var(--text-secondary); font-size: 0.875rem; margin: 0; }

.form-container { display: flex; flex-direction: column; gap: 1.5rem; }
.card { background: white; border-radius: 1rem; padding: 1.5rem; box-shadow: var(--shadow-sm); }
.card-title { margin-bottom: 1.5rem; font-size: 1.125rem; font-weight: 600; padding-bottom: 0.75rem; border-bottom: 1px solid #f3f4f6; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid #f3f4f6; padding-bottom: 0.75rem; }
.card-header-row .card-title { margin-bottom: 0; border-bottom: none; padding-bottom: 0; }

.form-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
.form-group { display: flex; flex-direction: column; gap: 0.5rem; }
.full-width { grid-column: 1 / -1; }
.form-group label { font-size: 0.875rem; font-weight: 500; color: var(--text-main); }
.required { color: #ef4444; }
.form-input { padding: 0.625rem 0.75rem; border: 1px solid var(--border-color); border-radius: 0.5rem; font-size: 0.875rem; transition: border-color 0.2s; background: white; }
.form-input:focus { border-color: var(--primary-color); outline: none; box-shadow: 0 0 0 2px var(--primary-light); }
textarea.form-input { resize: vertical; }

.details-table { width: 100%; border-collapse: collapse; }
.details-table th { text-align: left; padding: 0.75rem; background: #f9fafb; font-size: 0.75rem; color: var(--text-secondary); font-weight: 600; border-bottom: 1px solid var(--border-color); }
.details-table td { padding: 0.75rem; border-bottom: 1px solid var(--border-color); vertical-align: middle; }
.table-input { width: 100%; margin: 0; padding: 0.4rem 0.5rem; }
.icon-btn { background: none; border: none; cursor: pointer; padding: 4px; border-radius: 4px; transition: background 0.2s; }
.icon-btn:hover { background: #fee2e2; }
.text-red { color: #ef4444; }
.text-secondary { color: var(--text-secondary); font-size: 0.875rem; }
.empty-text { text-align: center; color: var(--text-secondary); padding: 2rem; font-style: italic; }
.text-center { text-align: center; }

@media (max-width: 768px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
