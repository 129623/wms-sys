package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.SysMenu;
import com.wms.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping("/list")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:menu:list') or hasAuthority('ROLE_ADMIN')")
    public Result<List<SysMenu>> list() {
        // 实际项目中通常需要构建树形结构，这里简单返回列表
        return Result.success(sysMenuService.list());
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:menu:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.SysMenuAddDTO addDTO) {
        return Result.success(sysMenuService.add(addDTO));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:menu:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody com.wms.dto.SysMenuUpdateDTO updateDTO) {
        return Result.success(sysMenuService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('system:menu:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysMenuService.removeById(id));
    }
}
