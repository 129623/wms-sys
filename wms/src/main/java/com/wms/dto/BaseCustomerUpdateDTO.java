package com.wms.dto;

import lombok.Data;

@Data
public class BaseCustomerUpdateDTO {
    private Long customerId;
    private String customerCode;
    private String customerName;
    private Integer customerType;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String city;
    private Integer status;
}
