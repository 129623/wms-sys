package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WmsInventory;
import com.wms.entity.WmsPickingTask;
import com.wms.mapper.WmsPickingTaskMapper;
import com.wms.service.WmsInventoryService;
import com.wms.service.WmsPickingTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class WmsPickingTaskServiceImpl extends ServiceImpl<WmsPickingTaskMapper, WmsPickingTask>
        implements WmsPickingTaskService {

    @Autowired
    private WmsInventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId, Long locationId, String operator) {
        WmsPickingTask task = this.getById(taskId);
        if (task == null || task.getStatus() == 1) {
            throw new RuntimeException("任务不存在或已完成");
        }

        // 1. 更新任务状态
        task.setLocationId(locationId); // Confirm actual picked location
        task.setStatus(1);
        task.setOperator(operator);
        task.setOperateTime(new Date());
        this.updateById(task);

        // 2. 扣减库存 (实际出库)
        LambdaQueryWrapper<WmsInventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmsInventory::getLocationId, locationId) // Use confirmed location
                .eq(WmsInventory::getProductId, task.getProductId())
                .eq(WmsInventory::getBatchNo, task.getBatchNo());

        WmsInventory inv = inventoryService.getOne(queryWrapper);
        if (inv == null) {
            throw new RuntimeException("库存记录异常丢失 (Loc:" + locationId + ")");
        }

        inventoryService.deductInventory(inv.getInventoryId(), task.getQty(), "PICK-" + taskId, operator);
    }
}
