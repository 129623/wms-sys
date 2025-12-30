package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseRackAddDTO implements Serializable {
    private Long zoneId;
    private Long warehouseId;
    private String rackCode;
    private String rackType;
    private Long relatedRackId;
}
