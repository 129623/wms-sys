<script setup>
import { ref, onMounted, reactive } from 'vue';
import { Search, Plus, Edit, Trash2, X, Save } from 'lucide-vue-next';
import request from '../utils/request';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  productName: ''
});

// Cache for dropdowns and mapping
const categories = ref([]);
const units = ref([]);

// Mapping Helpers
const getCategoryName = (id) => categories.value.find(c => c.categoryId === id)?.categoryName || '-';
const getUnitName = (id) => units.value.find(u => u.unitId === id)?.unitName || '-';

// Modal State
const showModal = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const form = reactive({
  productId: null,
  productName: '',
  skuCode: '',
  categoryId: '',
  unitId: '',
  spec: '',
  weight: 0,
  origin: ''
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/product/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('Failed to fetch products:', error);
  } finally {
    loading.value = false;
  }
};

const fetchMeta = async () => {
  try {
    const [resCat, resUnit] = await Promise.all([
      request.get('/category/list'),
      request.get('/unit/list')
    ]);
    if (resCat.code === 200) categories.value = resCat.data;
    if (resUnit.code === 200) units.value = resUnit.data;
  } catch (e) {
    console.error("Meta fetch failed", e);
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

const openAddModal = () => {
  isEdit.value = false;
  Object.assign(form, {
    productId: null,
    productName: '',
    skuCode: '',
    categoryId: '',
    unitId: '',
    spec: '',
    weight: 0,
    origin: ''
  });
  showModal.value = true;
};

const openEditModal = (item) => {
  isEdit.value = true;
  Object.assign(form, item);
  showModal.value = true;
};

const handleDelete = async (id) => {
  if (!confirm('确定要删除该货物吗？')) return;
  try {
    const res = await request.delete(`/product/${id}`);
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
  if (!form.productName) return alert('请输入产品名称');
  
  submitting.value = true;
  try {
    const url = isEdit.value ? '/product/update' : '/product/add';
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
  fetchMeta();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>货物资料</h2>
        <p class="subtitle">管理基础货物信息</p>
      </div>
      <button class="btn btn-primary" @click="openAddModal">
        <Plus :size="16" style="margin-right: 4px" /> 新增货物
      </button>
    </div>

    <div class="search-bar">
      <div class="input-icon-wrapper">
        <Search :size="18" class="icon" />
        <input 
          v-model="queryParams.productName" 
          @keyup.enter="handleSearch"
          type="text" 
          placeholder="输入货物名称搜索..." 
        />
      </div>
      <button class="btn btn-outline" @click="handleSearch">搜索</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>产品名称</th>
            <th>SKU</th>
            <th>规格</th>
            <th>分类</th>
            <th>单位</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.productId">
            <td class="font-medium">{{ item.productName }}</td>
            <td>{{ item.skuCode }}</td>
            <td>{{ item.spec || '-' }}</td>
            <td>{{ getCategoryName(item.categoryId) }}</td>
            <td>{{ getUnitName(item.unitId) }}</td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue" @click="openEditModal(item)"><Edit :size="16" /></button>
                <button class="action-btn text-red" @click="handleDelete(item.productId)"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
           <tr v-if="!loading && tableData.length === 0">
            <td colspan="6" class="text-center">暂无数据</td>
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

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑货物' : '新增货物' }}</h3>
          <button @click="showModal = false"><X :size="20" /></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>产品名称 <span class="text-red">*</span></label>
            <input type="text" v-model="form.productName" placeholder="例如: iPhone 15" />
          </div>
           <div class="form-group">
            <label>SKU (编码)</label>
            <input type="text" v-model="form.skuCode" placeholder="例如: SKU001" />
          </div>
          <div class="row">
              <div class="form-group half">
                <label>分类</label>
                <select v-model="form.categoryId">
                    <option value="" disabled>选择分类</option>
                    <option v-for="c in categories" :key="c.categoryId" :value="c.categoryId">{{ c.categoryName }}</option>
                </select>
              </div>
               <div class="form-group half">
                <label>单位</label>
                 <select v-model="form.unitId">
                    <option value="" disabled>选择单位</option>
                    <option v-for="u in units" :key="u.unitId" :value="u.unitId">{{ u.unitName }}</option>
                </select>
              </div>
          </div>
           <div class="form-group">
            <label>规格型号</label>
            <input type="text" v-model="form.spec" placeholder="例如: 红色 / 256GB" />
          </div>
           <div class="form-group">
            <label>产地</label>
            <input type="text" v-model="form.origin" placeholder="例如: 中国" />
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
/* Reuse styles */
.page-container { padding: 0 2rem 2rem; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
.page-header h2 { font-size: 1.5rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.25rem; }
.subtitle { color: var(--text-secondary); font-size: 0.875rem; }
.search-bar { display: flex; gap: 1rem; margin-bottom: 1.5rem; align-items: center; }
.input-icon-wrapper { position: relative; }
.input-icon-wrapper .icon { position: absolute; left: 1rem; top: 50%; transform: translateY(-50%); color: var(--text-secondary); }
.input-icon-wrapper input { padding: 0.6rem 1rem 0.6rem 2.5rem; border: 1px solid var(--border-color); background: white; border-radius: 0.5rem; width: 300px; outline: none; font-size: 0.875rem; transition: all 0.2s; }
.input-icon-wrapper input:focus { box-shadow: 0 0 0 2px var(--primary-light); border-color: var(--primary-color); }
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
.pagination { padding: 1rem; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); }
.pagination-controls button { padding: 0.25rem 0.75rem; border: 1px solid var(--border-color); background: white; border-radius: 0.25rem; cursor: pointer; margin: 0 0.25rem; }
.pagination-controls button:disabled { opacity: 0.5; cursor: not-allowed; }

/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal { background: white; border-radius: 0.5rem; width: 500px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { padding: 1rem 1.5rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 1.125rem; font-weight: 600; }
.modal-body { padding: 1.5rem; }
.modal-footer { padding: 1rem 1.5rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 0.5rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; margin-bottom: 0.5rem; font-size: 0.875rem; color: var(--text-secondary); }
.form-group input, .form-group select { width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 0.375rem; outline: none; box-sizing: border-box;}
.form-group input:focus, .form-group select:focus { border-color: var(--primary-color); } 
.mr-2 { margin-right: 0.5rem; }
.row { display: flex; gap: 1rem; }
.half { flex: 1; }
</style>
