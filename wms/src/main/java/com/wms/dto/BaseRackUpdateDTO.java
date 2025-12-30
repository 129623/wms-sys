package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseRackUpdateDTO implements Serializable {
    private Long rackId;
    private Long zoneId;
    private Long warehouseId;
    private String rackCode;
    private String rackType;
    private Long relatedRackId;
}
