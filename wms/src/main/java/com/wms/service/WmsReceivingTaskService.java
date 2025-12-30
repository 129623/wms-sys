package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.WmsReceivingTask;

public interface WmsReceivingTaskService extends IService<WmsReceivingTask> {

    /**
     * 完成上架任务
     * 
     * @param taskId           任务ID
     * @param actualLocationId 实际库位ID
     * @param operator         操作人
     */
    void completeTask(Long taskId, Long actualLocationId, String operator);
}
