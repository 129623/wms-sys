package com.wms.vo;

import com.wms.entity.WmsInventory;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsInventoryVO extends WmsInventory {

    private String productName;
    private String skuCode;
    private String productSpec;
    private String categoryName;
    private String unitName;

    private String warehouseName;
    private String locationName;
    private String locationCode;
    private String zoneName;
}
