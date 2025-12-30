package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class SysMenuUpdateDTO implements Serializable {
    private Long menuId;
    private Long parentId;
    private String menuName;
    private String perms;
    private String menuType; // M:目录, C:菜单, F:按钮
    private Integer orderNum;
}
