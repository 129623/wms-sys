package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class SysUserDTO implements Serializable {
    private Long userId;
    private String username;
    private String password; // Optional: Only for updates/creates
    private String realName;
    private String email;
    private String phone;
    private Integer status;
    private java.util.List<Long> roleIds;
}
