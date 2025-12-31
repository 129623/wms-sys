package com.wms.service;

import com.wms.entity.BaseLocation;
import com.wms.entity.BaseProduct;

import java.math.BigDecimal;

/**
 * 库位容量管理服务
 * 负责计算库位当前的重量和体积使用情况，并校验是否允许继续入库
 */
public interface LocationCapacityService {

    /**
     * 校验库位容量是否足够
     *
     * @param locationId 目标库位ID
     * @param productId  入库产品ID
     * @param quantity   入库数量
     * @throws RuntimeException 如果容量不足，抛出异常说明原因
     */
    void checkCapacity(Long locationId, Long productId, Integer quantity);

    /**
     * 获取库位当前负载详情（重量和体积）
     * 
     * @param locationId 库位ID
     * @return 负载详情字符串，用于日志或调试
     */
    String getLoadDetails(Long locationId);
}
