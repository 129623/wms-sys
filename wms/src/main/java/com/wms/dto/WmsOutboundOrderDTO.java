package com.wms.dto;

import com.wms.entity.WmsOutboundDetail;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class WmsOutboundOrderDTO implements Serializable {
    private Long outboundId;
    private String orderNo;
    private Long warehouseId;
    private Long customerId;
    private Integer orderType;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private Integer status;
    private List<WmsOutboundDetail> details;
}
