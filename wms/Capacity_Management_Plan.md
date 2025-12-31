# Wms系统 - 智能库位容量管理设计方案

## 1. 核心目标
解决"避免库位满了还能进东西"的问题，实现基于**体积**和**重量**的双重限制管理。在商品上架（Allocating）前预先校验目标库位的剩余容量。

## 2. 数据库表结构分析与增强

### 2.1 基础库位表 (base_location)
目前已存在的关键字段：
- `max_weight`: 最大承重 (kg)
- `max_volume`: 最大体积 (m³)
- `status`: 状态 (0:空闲, 1:占用, 2:锁定) -> *建议增加逻辑状态，而非仅物理占用*

**建议增强**：
- 确保 `max_volume` 和 `max_weight` 有默认值（如不限制则设为极在大值，避免为0导致无法入库）。

### 2.2 基础产品表 (base_product)
目前已存在的关键字段：
- `weight`: 单个重量 (kg)
- `length`: 长 (cm)
- `width`: 宽 (cm)
- `height`: 高 (cm)

**计算公式**：
- 单品体积 (m³) = `(length * width * height) / 1,000,000`
- 注意单位换算：数据库存的是cm，计算体积通常用m³。

### 2.3 库存表 (wms_inventory)
目前记录了：
- `location_id`
- `product_id`
- `total_qty`

## 3. 容量计算核心逻辑

### 3.1 实时占用计算 (Dynamic Calculation)
不建议在 `base_location` 表中硬编码 "current_weight" 或 "current_volume" 字段，因为容易导致数据不一致。
**推荐方案**：每次分配时，实时查询该库位的当前库存进行计算。

**伪代码逻辑**：
```java
// 1. 获取目标库位信息
BaseLocation location = locationService.getById(locationId);

// 2. 获取该库位当前所有库存
List<WmsInventory> inventories = inventoryService.listByLocation(locationId);

// 3. 计算当前已用重量和体积
BigDecimal currentWeight = BigDecimal.ZERO;
BigDecimal currentVolume = BigDecimal.ZERO;

for (WmsInventory inv : inventories) {
    BaseProduct product = productService.getById(inv.getProductId());
    // 重量 = 单重 * 数量
    currentWeight += product.getWeight() * inv.getTotalQty();
    // 体积 = (长*宽*高) * 数量
    BigDecimal singleVol = (product.getLength() * product.getWidth() * product.getHeight()) / 1000000;
    currentVolume += singleVol * inv.getTotalQty();
}

// 4. 计算本次入库物品的重量和体积
BaseProduct newProduct = productService.getById(incomingProductId);
BigDecimal newTotalWeight = currentWeight + (newProduct.getWeight() * incomingQty);
BigDecimal newTotalVolume = currentVolume + (singleIncomingVol * incomingQty);

// 5. 校验
if (newTotalWeight > location.getMaxWeight()) {
    throw new RuntimeException("超重！库位最大承重: " + location.getMaxWeight() + ", 预计总重: " + newTotalWeight);
}
if (newTotalVolume > location.getMaxVolume()) {
    throw new RuntimeException("超体积！库位最大体积: " + location.getMaxVolume() + ", 预计总体积: " + newTotalVolume);
}
```

## 4. 业务流程集成

### 4.1 自动分配策略 (Smart Strategy)
当生成上架任务（Putaway Task）时，系统自动推荐库位，逻辑如下：

1.  **输入**：入库商品及数量。
2.  **过滤**：
    *   找出所有属于该商品存储类型（如冷链、常温）的库区。
    *   找出这些库区下所有状态为"空闲"或"部分占用"的库位。
3.  **筛选 (Capacity Check)**：
    *   遍历候选库位，执行上述 [3.1] 的容量计算。
    *   剔除容量不足的库位。
4.  **排序 (Strategy)**：
    *   **策略A (清空优先/Empty First)**：优先填满已有库存的库位（相同产品），减少碎片化。
    *   **策略B (负载均衡)**：优先分配给最空的库位。
5.  **输出**：推荐库位ID。

### 4.2 人工指定校验
如果操作员手动指定库位，后端必须执行 [3.1] 的校验逻辑，如果不满足则抛出异常，前端提示用户更换库位。

## 5. 特殊情况处理

1.  **体积膨胀**：某些软包装商品（如棉衣）实际占用体积可能小于理论长宽高的乘积，可以增加一个 `volume_factor` (体积系数，如 0.8) 来修正。
2.  **混放限制**：不同类型的产品（如食品和化工品）不能放入同一个库位，这需要在分配逻辑中增加 `ProductCategory` 或 `ProductLabel` 的校验。

## 6. 下一步开发建议

1.  **完善数据**：确保所有 `BaseProduct` 的长宽高重量都不为 NULL 或 0。
2.  **编写容量服务**：创建一个 `LocationCapacityService`，专门提供 `verifyCapacity(locationId, productId, qty)` 方法。
3.  **上架策略**：实现简单的自动分配接口，根据容量推荐库位。
