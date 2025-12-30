package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class WmsOutboundDetailAddDTO implements Serializable {
    private Long productId;
    private Integer planQty;
}
