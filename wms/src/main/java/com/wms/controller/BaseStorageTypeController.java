package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.BaseStorageType;
import com.wms.service.BaseStorageTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage-type")
public class BaseStorageTypeController {

    @Autowired
    private BaseStorageTypeService typeService;

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:strgtype:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<BaseStorageType>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<BaseStorageType> page = new Page<>(pageNum, pageSize);
        return Result.success(typeService.page(page));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:strgtype:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody BaseStorageType type) {
        return Result.success(typeService.save(type));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:strgtype:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody BaseStorageType type) {
        return Result.success(typeService.updateById(type));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:strgtype:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(typeService.removeById(id));
    }
}
