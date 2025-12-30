package com.wms.dto;

import lombok.Data;

@Data
public class BaseCustomerQueryDTO {
    private String customerName;
    private String customerCode;
    private Integer customerType;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
