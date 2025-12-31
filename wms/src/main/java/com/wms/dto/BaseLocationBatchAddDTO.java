package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseLocationBatchAddDTO implements Serializable {
    private Long warehouseId;
    private Long zoneId;
    private Long rackId;

    // Compatibility fields
    private Integer rowNum;
    private Integer layerNum;

    // New range fields
    private Integer rowStart;
    private Integer rowEnd;
    private Integer layerStart;
    private Integer layerEnd;
}
