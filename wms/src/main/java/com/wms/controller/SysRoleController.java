package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.SysRole;
import com.wms.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("/list")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:role:list') or hasAuthority('ROLE_ADMIN')")
    public Result<List<SysRole>> list() {
        return Result.success(sysRoleService.list());
    }

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:role:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<SysRole>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            queryWrapper.like(SysRole::getRoleName, roleName);
        }
        return Result.success(sysRoleService.page(page, queryWrapper));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:role:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.SysRoleAddDTO addDTO) {
        return Result.success(sysRoleService.add(addDTO));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:role:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody com.wms.dto.SysRoleUpdateDTO updateDTO) {
        return Result.success(sysRoleService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:role:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysRoleService.removeById(id));
    }

    @GetMapping("/{id}/menus")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:role:list') or hasAuthority('ROLE_ADMIN')")
    public Result<List<Long>> getMenuIds(@PathVariable Long id) {
        return Result.success(sysRoleService.getMenuIdsByRoleId(id));
    }
}
