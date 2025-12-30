package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseZoneAddDTO implements Serializable {
    private Long warehouseId;
    private String zoneCode;
    private String zoneName;
    private String zoneType;
    private Integer status;
}
