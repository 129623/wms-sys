package com.wms.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BaseLocationUpdateDTO implements Serializable {
    private Long locationId;
    private Long rackId;
    private Long warehouseId;
    private Long zoneId;
    private String locationCode;
    private Integer rowNo;
    private Integer layerNo;
    private Integer status;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
}
