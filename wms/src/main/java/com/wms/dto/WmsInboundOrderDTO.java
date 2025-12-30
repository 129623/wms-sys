package com.wms.dto;

import com.wms.entity.WmsInboundDetail;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class WmsInboundOrderDTO implements Serializable {
    private Long inboundId;
    private String orderNo;
    private Long warehouseId;
    private Long customerId;
    private Integer orderType;
    private String remark;
    private Integer status;
    private List<WmsInboundDetail> details;
}
