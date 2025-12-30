package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.dto.SysUserAddDTO;
import com.wms.dto.SysUserUpdateDTO;
import com.wms.entity.SysRole;
import com.wms.entity.SysUser;
import com.wms.entity.SysUserRole;
import com.wms.mapper.SysRoleMapper;
import com.wms.mapper.SysUserMapper;
import com.wms.mapper.SysUserRoleMapper;
import com.wms.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public boolean saveUserWithRoles(com.wms.dto.SysUserDTO userDTO) {
        return false;
    }

    @Override
    @Transactional
    public boolean add(SysUserAddDTO addDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(addDTO, user);

        // 密码加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        this.save(user);

        // 保存角色关联
        if (addDTO.getRoleIds() != null) {
            for (Long roleId : addDTO.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean update(SysUserUpdateDTO updateDTO) {
        SysUser user = this.getById(updateDTO.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        BeanUtils.copyProperties(updateDTO, user);

        // 如果传了新密码则更新，否则保持原样
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        this.updateById(user);

        // 更新角色关联
        if (updateDTO.getRoleIds() != null) {
            // 删除旧关联
            sysUserRoleMapper.delete(
                    new QueryWrapper<SysUserRole>()
                            .eq("user_id", user.getUserId()));
            // 插入新关联
            for (Long roleId : updateDTO.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
        return true;
    }

    @Override
    public <E extends com.baomidou.mybatisplus.core.metadata.IPage<SysUser>> E page(E page,
            Wrapper<SysUser> queryWrapper) {
        E result = super.page(page, queryWrapper);
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (SysUser user : result.getRecords()) {
                // 查询关联角色
                SysUserRole userRole = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getUserId())
                        .last("LIMIT 1")); // 暂时只取一个角色
                if (userRole != null) {
                    user.setRoleId(userRole.getRoleId());
                    SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
                    if (role != null) {
                        user.setRoleName(role.getRoleName());
                    }
                }
            }
        }
        return result;
    }
}
