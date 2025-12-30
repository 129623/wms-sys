package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseWarehouseAddDTO implements Serializable {
    private String warehouseCode;
    private String warehouseName;
    private String address;
    private String manager;
    private String contact;
    private Integer status;
}
