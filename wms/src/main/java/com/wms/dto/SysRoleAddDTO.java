package com.wms.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class SysRoleAddDTO implements Serializable {
    private String roleName;
    private String roleKey;
    private Integer status;
    private String remark;
    private List<Long> menuIds;
}
