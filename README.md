


# WMS仓库管理系统

## 项目简介

WMS（Warehouse Management System）仓库管理系统是一套完整的仓库管理解决方案，采用前后端分离架构。系统实现了仓库运营的全流程管理，包括入库管理、出库管理、库存管理、库位管理等功能，并提供了完善的权限控制体系。

## 技术栈

### 后端技术
- **Spring Boot 2.7** - 应用框架
- **MyBatis-Plus 3.5** - ORM框架
- **Spring Security** - 安全框架
- **MySQL 8.0** - 数据库
- **Lombok** - 代码简化工具

### 前端技术
- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Vue Router** - 路由管理
- **Axios** - HTTP客户端

## 主要功能模块

### 基础数据管理
- 仓库管理（仓库信息、库区、货架、库位）
- 产品管理（产品信息、分类、单位、标签）
- 客户管理
- 存储类型管理

### 仓储作业管理
- 入库管理（入库单创建、收货登记）
- 出库管理（出库单创建、拣货任务）
- 库存管理（库存查询、库存变动历史）

### 系统管理
- 用户管理（用户账号、角色分配）
- 角色管理（角色权限配置）
- 菜单管理（动态菜单配置）
- 数据权限控制

## 项目结构

```
wms-sys/
├── wms/                          # 后端项目
│   ├── src/main/java/com/wms/
│   │   ├── config/               # 配置类
│   │   ├── controller/           # 控制器层
│   │   ├── dto/                  # 数据传输对象
│   │   ├── entity/               # 实体类
│   │   ├── mapper/               # 数据访问层
│   │   ├── service/              # 业务逻辑层
│   │   └── vo/                   # 视图对象
│   └── src/main/resources/
│       ├── application.yml       # 配置文件
│       └── mapper/               # MyBatis映射文件
│
└── wms-frontend/                 # 前端项目
    ├── src/
    │   ├── components/           # 公共组件
    │   ├── views/                # 页面视图
    │   │   └── system/           # 系统管理页面
    │   ├── router/               # 路由配置
    │   ├── utils/                # 工具函数
    │   └── App.vue               # 根组件
    └── public/                   # 静态资源
```

## 数据库设计

系统包含以下核心数据表：

**基础数据表**
- `base_warehouse` - 仓库表
- `base_zone` - 库区表
- `base_rack` - 货架表
- `base_location` - 库位表
- `base_product` - 产品表
- `base_category` - 分类表
- `base_unit` - 单位表
- `base_customer` - 客户表

**库存业务表**
- `wms_inventory` - 库存表
- `wms_inventory_history` - 库存历史表
- `wms_inbound_order` - 入库单表
- `wms_inbound_detail` - 入库明细表
- `wms_outbound_order` - 出库单表
- `wms_outbound_detail` - 出库明细表
- `wms_receiving_task` - 收货任务表
- `wms_picking_task` - 拣货任务表

**系统权限表**
- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_menu` - 菜单表
- `sys_user_role` - 用户角色关联表
- `sys_role_menu` - 角色菜单关联表

## 环境要求

- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

## 快速启动

### 1. 数据库配置

创建数据库并导入初始化脚本：

```sql
CREATE DATABASE wms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入数据库脚本（按顺序）：
- `wms/wms_schema.sql` - 表结构
- `wms/init_data.sql` - 基础数据
- `wms/init_rbac.sql` - 权限数据

### 2. 后端配置

修改 `wms/src/main/resources/application.yml` 中的数据库连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

启动后端服务：

```bash
cd wms
mvn spring-boot:run
```

后端服务默认运行在 **8080** 端口。

### 3. 前端配置

安装依赖并启动：

```bash
cd wms-frontend
npm install
npm run dev
```

前端服务默认运行在 **5173** 端口。

## 默认账号

系统初始化了一个超级管理员账号：

- **用户名**：admin
- **密码**：123456

## API文档

系统提供了RESTful API，主要接口包括：

### 登录认证
- `POST /login` - 用户登录

### 基础数据
- `GET /warehouse/list` - 仓库列表
- `GET /location/page` - 库位分页
- `GET /product/page` - 产品分页
- `GET /category/list` - 分类列表

### 仓储作业
- `GET /inbound/page` - 入库单分页
- `POST /inbound/add` - 创建入库单
- `POST /inbound/receipt` - 收货登记
- `GET /outbound/page` - 出库单分页
- `POST /outbound/add` - 创建出库单
- `POST /outbound/pick` - 拣货确认

### 库存管理
- `GET /inventory/page` - 库存分页查询

### 系统管理
- `GET /user/page` - 用户分页
- `GET /role/page` - 角色分页
- `GET /menu/list` - 菜单列表

## 权限说明

系统采用RBAC（基于角色的访问控制）权限模型：

- **超级管理员（ROLE_ADMIN）** - 拥有所有权限
- **普通角色** - 通过权限标识控制访问

主要权限标识：
- `system:user:list` - 用户列表
- `system:role:list` - 角色列表
- `base:warehouse:list` - 仓库列表
- `wms:inbound:list` - 入库列表
- `wms:outbound:list` - 出库列表
- `wms:inventory:list` - 库存列表

## License

本项目仅供学习和研究使用。
