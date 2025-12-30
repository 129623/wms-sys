package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.WmsOutboundOrder;

public interface WmsOutboundOrderService extends IService<WmsOutboundOrder> {
    boolean addOutbound(com.wms.dto.WmsOutboundAddDTO addDTO);

    boolean executePick(com.wms.dto.WmsOutboundPickDTO pickDTO, String operator);
}
