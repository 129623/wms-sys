package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    boolean saveUserWithRoles(com.wms.dto.SysUserDTO userDTO);

    boolean add(com.wms.dto.SysUserAddDTO addDTO);

    boolean update(com.wms.dto.SysUserUpdateDTO updateDTO);
}
