package com.wms.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class WmsOutboundAddDTO implements Serializable {
    private Long warehouseId;
    private Long customerId;
    private Integer orderType;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private String remark; // Ensure logic handles this if entity has it (Entity doesn't have remark
                           // visible in previous view, let's check or skip)
    // Wait, WmsOutboundOrder entity has: outboundId, orderNo, warehouseId,
    // orderType, status, receiverName, receiverPhone, address, createBy,
    // createTime.
    // It does NOT have remark. I will omit remark or add it to entity?
    // InboundOrder had remark. OutboundOrder (viewed in step 3) does NOT have
    // remark.
    // I will include the fields present in Entity.

    private List<WmsOutboundDetailAddDTO> details;
}
