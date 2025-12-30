package com.wms.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class WmsInboundAddDTO implements Serializable {
    private Long warehouseId;
    private Long customerId;
    private Integer orderType; // 1:采购入库
    private String remark;
    private List<WmsInboundDetailAddDTO> details;
}
