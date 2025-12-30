package com.wms.dto;

import lombok.Data;

@Data
public class BaseCustomerAddDTO {
    private String customerCode;
    private String customerName;
    private Integer customerType; // 1:Client, 2:Supplier, 3:Both
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String city;
    private Integer status; // 1:Active, 0:Inactive
}
