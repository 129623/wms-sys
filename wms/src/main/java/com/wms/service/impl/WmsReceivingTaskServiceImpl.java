package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WmsInventory;
import com.wms.entity.WmsReceivingTask;
import com.wms.mapper.WmsReceivingTaskMapper;
import com.wms.service.WmsInventoryService;
import com.wms.service.WmsReceivingTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class WmsReceivingTaskServiceImpl extends ServiceImpl<WmsReceivingTaskMapper, WmsReceivingTask>
        implements WmsReceivingTaskService {

    @Autowired
    private WmsInventoryService inventoryService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.wms.service.WmsInboundOrderService inboundOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId, Long actualLocationId, String operator) {
        WmsReceivingTask task = this.getById(taskId);
        if (task == null || task.getStatus() == 1) {
            throw new RuntimeException("任务不存在或已完成");
        }

        // 1. 更新任务状态
        task.setActualLocationId(actualLocationId);
        task.setStatus(1); // 已上架
        task.setOperator(operator);
        task.setOperateTime(new Date());
        this.updateById(task);

        // Get Warehouse ID from Inbound Order
        com.wms.entity.WmsInboundOrder order = inboundOrderService.getById(task.getInboundId());

        // 2. 增加库存
        WmsInventory inventory = new WmsInventory();
        inventory.setWarehouseId(order.getWarehouseId());
        inventory.setLocationId(actualLocationId);
        inventory.setProductId(task.getProductId());
        inventory.setBatchNo(task.getBatchNo());
        inventory.setTotalQty(task.getQty());

        // 调用库存服务
        inventoryService.addInventory(inventory, "REC-" + taskId, operator);
    }
}
