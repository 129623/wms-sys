package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    @Autowired
    private com.wms.service.SysUserService sysUserService;

    @Autowired
    private com.wms.mapper.SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private com.wms.mapper.SysRoleMapper sysRoleMapper;

    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    private final org.springframework.security.web.context.SecurityContextRepository securityContextRepository = new org.springframework.security.web.context.HttpSessionSecurityContextRepository();

    @PostMapping("/api/login")
    public Result<java.util.Map<String, Object>> login(@RequestBody com.wms.dto.LoginDTO loginDTO,
            jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        try {
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken token = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(), loginDTO.getPassword());
            org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(token);

            // 显式保存 Context 到 Session (Spring Security 6 要求)
            org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder
                    .createEmptyContext();
            context.setAuthentication(authentication);
            org.springframework.security.core.context.SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            // 获取 Session ID (Spring Security 默认使用 HttpSession)
            jakarta.servlet.http.HttpSession session = request.getSession();

            // 获取用户信息
            SysUser user = sysUserService
                    .getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getUsername, loginDTO.getUsername()));

            // 获取角色名称列表
            java.util.List<String> roleNames = new java.util.ArrayList<>();
            if (user != null) {
                java.util.List<com.wms.entity.SysUserRole> userRoles = sysUserRoleMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.wms.entity.SysUserRole>()
                                .eq(com.wms.entity.SysUserRole::getUserId, user.getUserId()));

                for (com.wms.entity.SysUserRole ur : userRoles) {
                    com.wms.entity.SysRole role = sysRoleMapper.selectById(ur.getRoleId());
                    if (role != null) {
                        roleNames.add(role.getRoleName());
                    }
                }
                // Super Admin check
                if ("admin".equalsIgnoreCase(user.getUsername()) && roleNames.isEmpty()) {
                    roleNames.add("超级管理员");
                }
            }

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("sessionId", session.getId());
            String displayName = loginDTO.getUsername();
            if (user != null) {
                displayName = (user.getRealName() != null && !user.getRealName().isEmpty()) ? user.getRealName()
                        : user.getUsername();
            }
            data.put("username", displayName);
            data.put("account", loginDTO.getUsername());
            data.put("roles", roleNames);
            data.put("role", roleNames.isEmpty() ? "普通用户" : roleNames.get(0)); // Primary role for display

            // Extract permissions from authentication
            java.util.Set<String> permissions = new java.util.HashSet<>();
            for (org.springframework.security.core.GrantedAuthority authority : authentication.getAuthorities()) {
                permissions.add(authority.getAuthority());
            }
            data.put("permissions", permissions);

            return Result.success(data);
        } catch (org.springframework.security.core.AuthenticationException e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @GetMapping("/api/logout")
    public Result<String> logout() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return Result.success("退出成功");
    }

    @GetMapping("/api/unauth")
    public Result<String> unauth() {
        return Result.error("未登录或无权限");
    }
}
