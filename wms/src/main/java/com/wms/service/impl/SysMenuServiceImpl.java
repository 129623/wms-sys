package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.SysMenu;
import com.wms.mapper.SysMenuMapper;
import com.wms.service.SysMenuService;
import org.springframework.stereotype.Service;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Override
    public boolean add(com.wms.dto.SysMenuAddDTO addDTO) {
        SysMenu menu = new SysMenu();
        org.springframework.beans.BeanUtils.copyProperties(addDTO, menu);
        return this.save(menu);
    }

    @Override
    public boolean update(com.wms.dto.SysMenuUpdateDTO updateDTO) {
        SysMenu menu = this.getById(updateDTO.getMenuId());
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        org.springframework.beans.BeanUtils.copyProperties(updateDTO, menu);
        return this.updateById(menu);
    }
}
