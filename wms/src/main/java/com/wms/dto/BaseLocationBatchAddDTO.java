package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseLocationBatchAddDTO implements Serializable {
    private Long warehouseId;
    private Long zoneId;
    private Long rackId;
    private Integer rowNum; // 总列数
    private Integer layerNum; // 总层数
}
