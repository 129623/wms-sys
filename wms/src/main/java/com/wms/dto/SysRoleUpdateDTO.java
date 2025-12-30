package com.wms.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class SysRoleUpdateDTO implements Serializable {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private Integer status;
    private String remark;
    private List<Long> menuIds;
}
