package com.wms.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BaseProductAddDTO implements Serializable {
    private Long categoryId;
    private String skuCode;
    private String productName;
    private String spec;
    private String unit;
    private Long unitId;
    private Long storageTypeId;
    private Long labelId;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Integer palletCapacity;
    private String imageUrl;
    private String origin;
}
