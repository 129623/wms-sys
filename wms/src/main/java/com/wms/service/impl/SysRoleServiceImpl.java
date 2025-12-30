package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.SysRole;
import com.wms.mapper.SysRoleMapper;
import com.wms.service.SysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.wms.mapper.SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public boolean saveRoleWithMenus(com.wms.dto.SysRoleDTO roleDTO) {
        return false;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public boolean add(com.wms.dto.SysRoleAddDTO addDTO) {
        SysRole role = new SysRole();
        org.springframework.beans.BeanUtils.copyProperties(addDTO, role);
        this.save(role);

        if (addDTO.getMenuIds() != null) {
            for (Long menuId : addDTO.getMenuIds()) {
                com.wms.entity.SysRoleMenu roleMenu = new com.wms.entity.SysRoleMenu();
                roleMenu.setRoleId(role.getRoleId());
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        return true;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public boolean update(com.wms.dto.SysRoleUpdateDTO updateDTO) {
        SysRole role = this.getById(updateDTO.getRoleId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        org.springframework.beans.BeanUtils.copyProperties(updateDTO, role);
        this.updateById(role);

        if (updateDTO.getMenuIds() != null) {
            sysRoleMenuMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.wms.entity.SysRoleMenu>()
                            .eq("role_id", role.getRoleId()));
            for (Long menuId : updateDTO.getMenuIds()) {
                com.wms.entity.SysRoleMenu roleMenu = new com.wms.entity.SysRoleMenu();
                roleMenu.setRoleId(role.getRoleId());
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        return true;
    }

    @Override
    public java.util.List<Long> getMenuIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return java.util.Collections.emptyList();
        }
        java.util.List<com.wms.entity.SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.wms.entity.SysRoleMenu>()
                        .eq("role_id", roleId));
        return roleMenus.stream().map(com.wms.entity.SysRoleMenu::getMenuId)
                .collect(java.util.stream.Collectors.toList());
    }
}
