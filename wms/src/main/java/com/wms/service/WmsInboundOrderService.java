package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.WmsInboundOrder;

public interface WmsInboundOrderService extends IService<WmsInboundOrder> {

    /**
     * 创建入库单
     * 
     * @param addDTO 入库单信息
     * @return 是否成功
     */
    boolean addInbound(com.wms.dto.WmsInboundAddDTO addDTO);

    /**
     * 执行入库（上架）
     * 
     * @param receiptDTO 入库参数
     * @return 是否成功
     */
    boolean executeReceipt(com.wms.dto.WmsInboundReceiptDTO receiptDTO, String operator);
}
