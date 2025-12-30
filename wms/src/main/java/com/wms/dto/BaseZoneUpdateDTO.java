package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseZoneUpdateDTO implements Serializable {
    private Long zoneId;
    private Long warehouseId;
    private String zoneCode;
    private String zoneName;
    private String zoneType;
    private Integer status;
}
