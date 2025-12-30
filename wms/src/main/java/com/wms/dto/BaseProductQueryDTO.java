package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseProductQueryDTO implements Serializable {
    private String productName;
    private String skuCode;
    private Long categoryId;
    private Long unitId;
    private Long storageTypeId;
    private Long labelId;
}
