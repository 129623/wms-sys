package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WmsInventory;
import com.wms.entity.WmsInventoryHistory;
import com.wms.mapper.WmsInventoryHistoryMapper;
import com.wms.mapper.WmsInventoryMapper;
import com.wms.service.WmsInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class WmsInventoryServiceImpl extends ServiceImpl<WmsInventoryMapper, WmsInventory>
        implements WmsInventoryService {

    @Autowired
    private WmsInventoryHistoryMapper inventoryHistoryMapper;

    @Autowired
    private com.wms.service.BaseLocationService locationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInventory(WmsInventory inventory, String orderNo, String operator) {
        // 自动补充 ZoneId
        if (inventory.getZoneId() == null && inventory.getLocationId() != null) {
            com.wms.entity.BaseLocation location = locationService.getById(inventory.getLocationId());
            if (location == null) {
                throw new RuntimeException("指定的库位不存在 (ID: " + inventory.getLocationId() + ")");
            }
            if (location.getZoneId() == null) {
                throw new RuntimeException("该库位数据异常，未关联库区 (LocationID: " + inventory.getLocationId() + ")");
            }
            inventory.setWarehouseId(location.getWarehouseId()); // 确保仓库ID也一致
            inventory.setZoneId(location.getZoneId());
        } else if (inventory.getZoneId() == null) {
            throw new RuntimeException("入库必须指定库位或库区");
        }

        // 1. 查询当前库位+产品+批次 是否已存在
        LambdaQueryWrapper<WmsInventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmsInventory::getLocationId, inventory.getLocationId())
                .eq(WmsInventory::getProductId, inventory.getProductId())
                .eq(WmsInventory::getBatchNo, inventory.getBatchNo());

        WmsInventory existInv = this.getOne(queryWrapper);

        int changeQty = inventory.getTotalQty();
        int afterQty = 0;

        if (existInv != null) {
            // 更新
            existInv.setTotalQty(existInv.getTotalQty() + changeQty);
            existInv.setUpdateTime(new Date());
            this.updateById(existInv);
            afterQty = existInv.getTotalQty();
        } else {
            // 新增
            inventory.setFrozenQty(0);
            inventory.setCreateTime(new Date());
            inventory.setUpdateTime(new Date());
            this.save(inventory);
            afterQty = inventory.getTotalQty();
        }

        // 2. 记录流水
        WmsInventoryHistory history = new WmsInventoryHistory();
        history.setWarehouseId(inventory.getWarehouseId()); // 假设传入的对象里有WarehouseId
        history.setProductId(inventory.getProductId());
        history.setBatchNo(inventory.getBatchNo());
        history.setLocationId(inventory.getLocationId());
        history.setChangeType(1); // 1:入库
        history.setChangeQty(changeQty);
        history.setAfterQty(afterQty);
        history.setOrderNo(orderNo);
        history.setOperator(operator);
        history.setOperateTime(new Date());

        inventoryHistoryMapper.insert(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockInventory(Long inventoryId, Integer qty) {
        WmsInventory inventory = this.getById(inventoryId);
        if (inventory == null) {
            throw new RuntimeException("库存不存在");
        }
        if (inventory.getTotalQty() - inventory.getFrozenQty() < qty) {
            throw new RuntimeException("可用库存不足");
        }
        inventory.setFrozenQty(inventory.getFrozenQty() + qty);
        this.updateById(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductInventory(Long inventoryId, Integer qty, String orderNo, String operator) {
        WmsInventory inventory = this.getById(inventoryId);
        if (inventory == null) {
            throw new RuntimeException("库存不存在");
        }
        // 出库时，扣减totalQty和frozenQty (假设之前已分配锁定)
        if (inventory.getFrozenQty() < qty || inventory.getTotalQty() < qty) {
            throw new RuntimeException("库存异常：扣减量大于锁定或总量");
        }

        inventory.setTotalQty(inventory.getTotalQty() - qty);
        inventory.setFrozenQty(inventory.getFrozenQty() - qty);
        this.updateById(inventory);

        // 记录流水
        WmsInventoryHistory history = new WmsInventoryHistory();
        history.setWarehouseId(inventory.getWarehouseId());
        history.setProductId(inventory.getProductId());
        history.setBatchNo(inventory.getBatchNo());
        history.setLocationId(inventory.getLocationId());
        history.setChangeType(2); // 2:出库
        history.setChangeQty(-qty); // 负数
        history.setAfterQty(inventory.getTotalQty());
        history.setOrderNo(orderNo);
        history.setOperator(operator);
        history.setOperateTime(new Date());

        inventoryHistoryMapper.insert(history);
    }
}
