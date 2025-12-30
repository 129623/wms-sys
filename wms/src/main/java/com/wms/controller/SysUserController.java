package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.SysUser;
import com.wms.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/list")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:user:list') or hasAuthority('ROLE_ADMIN')")
    public Result<List<SysUser>> list() {
        return Result.success(sysUserService.list());
    }

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:user:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        return Result.success(sysUserService.page(page, queryWrapper));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:user:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.SysUserAddDTO addDTO) {
        return Result.success(sysUserService.add(addDTO));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:user:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody com.wms.dto.SysUserUpdateDTO updateDTO) {
        return Result.success(sysUserService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:user:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysUserService.removeById(id));
    }
}
