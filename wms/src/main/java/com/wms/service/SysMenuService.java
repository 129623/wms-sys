package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.SysMenu;

public interface SysMenuService extends IService<SysMenu> {
    boolean add(com.wms.dto.SysMenuAddDTO addDTO);

    boolean update(com.wms.dto.SysMenuUpdateDTO updateDTO);
}
