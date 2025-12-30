package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class WmsInboundDetailAddDTO implements Serializable {
    private Long productId;
    private Integer planQty;
    // Remark can be added here if needed
}
