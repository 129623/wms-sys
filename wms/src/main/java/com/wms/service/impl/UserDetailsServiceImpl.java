package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.entity.SysRole;
import com.wms.entity.SysUser;
import com.wms.entity.SysUserRole;
import com.wms.mapper.SysMenuMapper;
import com.wms.mapper.SysRoleMapper;
import com.wms.mapper.SysUserRoleMapper;

import com.wms.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 超级管理员赋予 ROLE_ADMIN
        if ("admin".equalsIgnoreCase(sysUser.getUsername())) {
            System.out.println("Assigning ROLE_ADMIN to user: " + sysUser.getUsername());
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // 加载用户角色并添加到 authorities
        List<SysUserRole> userRoles = sysUserRoleMapper
                .selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getUserId()));
        for (SysUserRole ur : userRoles) {
            SysRole role = sysRoleMapper.selectById(ur.getRoleId());
            if (role != null && role.getStatus() == 1) { // 仅加载启用角色的权限
                authorities.add(new SimpleGrantedAuthority(role.getRoleKey()));
            }
        }

        // 加载数据库中的权限
        List<String> perms = sysMenuMapper.selectPermsByUserId(sysUser.getUserId());
        if (perms != null) {
            for (String perm : perms) {
                if (perm != null && !perm.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(perm));
                }
            }
        }
        System.out.println("User " + username + " authorities: " + authorities);

        return new org.springframework.security.core.userdetails.User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                authorities);
    }
}
