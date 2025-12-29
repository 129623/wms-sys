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
  username: ''
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/user/page', { params: queryParams });
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    }
  } catch (error) {
    console.error('Failed to fetch users:', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.pageNum = 1;
  fetchData();
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>系统管理</h2>
        <p class="subtitle">用户、角色及权限管理</p>
      </div>
       <button class="btn btn-primary">
        <Plus :size="16" style="margin-right: 4px" /> 新增用户
      </button>
    </div>

     <div class="search-bar">
      <div class="input-icon-wrapper">
        <Search :size="18" class="icon" />
        <input 
          v-model="queryParams.username" 
          @keyup.enter="handleSearch"
          type="text" 
          placeholder="输入用户名搜索..." 
        />
      </div>
      <button class="btn btn-outline" @click="handleSearch">搜索</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>用户名</th>
            <th>姓名</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="text-center">加载中...</td>
          </tr>
          <tr v-else v-for="item in tableData" :key="item.id">
            <td class="font-medium">{{ item.username }}</td>
            <td>{{ item.fullname || item.name || '-' }}</td> <!-- Fallback -->
            <td>{{ item.roleId || '普通用户' }}</td>
             <td>
              <span class="badge badge-success">正常</span>
            </td>
            <td>
              <div class="action-buttons">
                <button class="action-btn text-blue"><Edit :size="16" /></button>
                <button class="action-btn text-red"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
           <tr v-if="!loading && tableData.length === 0">
            <td colspan="5" class="text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>
      <div class="pagination" v-if="total > 0">
         <span class="text-sm text-secondary">共 {{ total }} 条记录</span>
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
.pagination { padding: 1rem; display: flex; justify-content: flex-end; border-top: 1px solid var(--border-color); }
.badge { display: inline-flex; padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; }
.badge-success { background: #dcfce7; color: #16a34a; }
</style>
