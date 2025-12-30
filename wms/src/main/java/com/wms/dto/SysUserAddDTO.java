package com.wms.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class SysUserAddDTO implements Serializable {
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private Integer status;
    private List<Long> roleIds;
}
