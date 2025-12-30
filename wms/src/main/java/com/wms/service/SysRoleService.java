package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {
    boolean saveRoleWithMenus(com.wms.dto.SysRoleDTO roleDTO);

    boolean add(com.wms.dto.SysRoleAddDTO addDTO);

    boolean update(com.wms.dto.SysRoleUpdateDTO updateDTO);

    java.util.List<Long> getMenuIdsByRoleId(Long roleId);
}
