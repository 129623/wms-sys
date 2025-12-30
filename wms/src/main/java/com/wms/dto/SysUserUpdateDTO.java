package com.wms.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class SysUserUpdateDTO implements Serializable {
    private Long userId;
    private String realName;
    private String email;
    private String phone;
    private Integer status;
    private List<Long> roleIds;
    // Optional: Allow password reset during update, or separate endpoint?
    // Plan said: "Include generic password update support for now, optional."
    private String password;
    private String username; // Usually immutable, but included as optional if needed
}
