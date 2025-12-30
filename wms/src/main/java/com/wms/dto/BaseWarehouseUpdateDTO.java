package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseWarehouseUpdateDTO implements Serializable {
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String address;
    private String manager;
    private String contact;
    private Integer status;
}
