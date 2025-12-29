<script setup>
import { ArrowRight, Box, Settings, Package, ArrowDown, ArrowUp, RotateCw } from 'lucide-vue-next';
</script>

<template>
  <div class="dashboard">
    <div class="hero-section">
      <div class="hero-card">
        <div class="hero-content">
          <h1>智能仓储管理系统</h1>
          <p>实时追踪库存与作业状态</p>
          <div class="hero-actions">
            <button class="btn btn-primary bg-white text-primary">查看报表</button>
            <button class="btn btn-outline bg-transparent text-white border-white hover:bg-white-10">库存管理</button>
          </div>
        </div>
        <img src="https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80" alt="Warehouse" class="hero-bg" />
        <div class="hero-overlay"></div>
      </div>

      <div class="stats-card-main">
        <h3>快速统计</h3>
        <div class="stat-row">
          <div class="big-stat">
            <span class="number">12</span>
            <span class="label">待入库任务</span>
          </div>
          <div class="big-stat">
            <span class="number">8</span>
            <span class="label">待出库任务</span>
          </div>
        </div>
        <a href="#" class="link-arrow">
          前往任务中心 <ArrowRight :size="16" />
        </a>
        <div class="decoration-icon">
          <Box :size="64" />
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="left-col">
        <div class="section-header">
          <h3>最新库存</h3>
          <a href="#">查看全部 ></a>
        </div>
        <div class="inventory-cards">
          <div class="card">
            <div class="icon-box"><Box :size="32" /></div>
            <h4>电子主板</h4>
            <p>A区 - 01货架</p>
          </div>
          <div class="card">
            <div class="icon-box"><Settings :size="32" /></div>
            <h4>机械零件</h4>
            <p>B区 - 12货架</p>
          </div>
          <div class="card">
            <div class="icon-box"><Package :size="32" /></div>
            <h4>包装材料</h4>
            <p>C区 - 05货架</p>
          </div>
        </div>

        <div class="capacity-section">
          <h3>仓库容量</h3>
          <div class="capacity-cards">
            <div class="cap-card">
              <div class="cap-info">
                <h4>A区 (电子区)</h4>
                <span>4,200 / 5,000</span>
              </div>
              <div class="progress-bar">
                <div class="progress" style="width: 85%"></div>
              </div>
              <span class="muted">85% 已满</span>
            </div>
            <div class="cap-card">
              <div class="cap-info">
                <h4>B区 (零件区)</h4>
                <span>1,250 / 3,000</span>
              </div>
              <div class="progress-bar">
                <div class="progress warning" style="width: 45%"></div>
              </div>
              <span class="muted">45% 已满</span>
            </div>
          </div>
        </div>
      </div>

      <div class="right-col">
        <div class="section-header">
          <h3>最近动态</h3>
          <a href="#">查看全部</a>
        </div>
        <div class="activity-list">
          <div class="activity-item">
            <div class="act-icon in"><ArrowDown :size="16" /></div>
            <div class="act-details">
              <h4>入库单 #10023</h4>
              <p>收到500件产品</p>
            </div>
            <span class="time">10分钟前</span>
          </div>
          <div class="activity-item">
            <div class="act-icon out"><ArrowUp :size="16" /></div>
            <div class="act-details">
              <h4>出库单 #90012</h4>
              <p>发货20托盘</p>
            </div>
            <span class="time">45分钟前</span>
          </div>
          <div class="activity-item">
            <div class="act-icon audit"><RotateCw :size="16" /></div>
            <div class="act-details">
              <h4>库存盘点</h4>
              <p>A区盘点完成</p>
            </div>
            <span class="time">2小时前</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0 2rem 2rem;
}

.hero-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.hero-card {
  position: relative;
  background: #111827;
  border-radius: 1rem;
  overflow: hidden;
  height: 240px;
  display: flex;
  align-items: center;
  padding: 2rem;
  color: white;
}

.hero-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 60%;
  height: 100%;
  object-fit: cover;
  opacity: 0.5;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, #111827 40%, transparent);
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 60%;
}

.hero-content h1 {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  line-height: 1.2;
}

.hero-content p {
  color: #9ca3af;
  margin-bottom: 1.5rem;
}

.hero-actions {
  display: flex;
  gap: 1rem;
}

.text-primary { color: var(--primary-dark) !important; }
.bg-white-10 { background: rgba(255,255,255,0.1); }
.border-white { border: 1px solid rgba(255,255,255,0.3) !important; color: white !important;}

.stats-card-main {
  background: #bef264; /* Lime 300 equivalent */
  border-radius: 1rem;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  margin: 1.5rem 0;
}

.big-stat {
  display: flex;
  flex-direction: column;
}

.big-stat .number {
  font-size: 2.5rem;
  font-weight: 800;
  color: #365314; /* Lime 950 */
  line-height: 1;
}

.big-stat .label {
  font-size: 0.875rem;
  color: #4d7c0f; /* Lime 700 */
  font-weight: 500;
}

.link-arrow {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #365314;
  font-size: 0.875rem;
}

.decoration-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  opacity: 0.2;
  color: #365314;
  transform: rotate(-15deg);
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h3 {
  font-size: 1.125rem;
  font-weight: 700;
}

.section-header a {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.inventory-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

.card {
  background: white;
  padding: 1.5rem;
  border-radius: 0.75rem;
  text-align: center;
  border: 1px solid var(--border-color);
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.icon-box {
  background: var(--bg-color);
  width: 64px;
  height: 64px;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  color: var(--text-secondary);
}

.card h4 {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.card p {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.capacity-cards {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cap-card {
  background: white;
  padding: 1.25rem;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
}

.cap-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.cap-info h4 { font-weight: 600; font-size: 0.875rem; }
.cap-info span { font-size: 0.875rem; font-weight: 700; }

.progress-bar {
  background: var(--bg-color);
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress {
  background: var(--primary-color);
  height: 100%;
  border-radius: 4px;
}

.progress.warning {
  background: #facc15; /* Yellow 400 */
}

.muted {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.right-col {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  border: 1px solid var(--border-color);
  height: fit-content;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.activity-item {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.act-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.act-icon.in { background: #dcfce7; color: #16a34a; }
.act-icon.out { background: #fee2e2; color: #dc2626; }
.act-icon.audit { background: #fff7ed; color: #ea580c; }

.act-details h4 {
  font-size: 0.875rem;
  font-weight: 600;
}

.act-details p {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.time {
  margin-left: auto;
  font-size: 0.75rem;
  color: var(--text-secondary);
}
</style>
