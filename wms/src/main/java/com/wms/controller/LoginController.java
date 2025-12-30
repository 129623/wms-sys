package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    private final org.springframework.security.web.context.SecurityContextRepository securityContextRepository = new org.springframework.security.web.context.HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public Result<String> login(@RequestBody com.wms.dto.LoginDTO loginDTO,
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

            return Result.success("登录成功: " + session.getId());
        } catch (org.springframework.security.core.AuthenticationException e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @GetMapping("/logout")
    public Result<String> logout() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return Result.success("退出成功");
    }

    @GetMapping("/unauth")
    public Result<String> unauth() {
        return Result.error("未登录或无权限");
    }
}
