package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.WmsPickingTask;

public interface WmsPickingTaskService extends IService<WmsPickingTask> {

    /**
     * 完成拣货任务
     * 
     * @param taskId   任务ID
     * @param operator 操作人
     */
    void completeTask(Long taskId, Long locationId, String operator);
}
