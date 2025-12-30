package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.BaseWarehouse;
import com.wms.service.BaseWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
public class BaseWarehouseController {

    @Autowired
    private BaseWarehouseService warehouseService;

    @GetMapping("/list")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:warehouse:list') or hasAuthority('ROLE_ADMIN')")
    public Result<List<BaseWarehouse>> list() {
        // 只查询未删除的仓库（status != 0）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BaseWarehouse> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.ne(BaseWarehouse::getStatus, 0);
        return Result.success(warehouseService.list(wrapper));
    }

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:warehouse:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<BaseWarehouse>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<BaseWarehouse> page = new Page<>(pageNum, pageSize);
        // 只查询未删除的仓库（status != 0）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BaseWarehouse> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.ne(BaseWarehouse::getStatus, 0);
        return Result.success(warehouseService.page(page, wrapper));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:warehouse:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.BaseWarehouseAddDTO addDTO) {
        return Result.success(warehouseService.add(addDTO));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:warehouse:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody com.wms.dto.BaseWarehouseUpdateDTO updateDTO) {
        return Result.success(warehouseService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:warehouse:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(warehouseService.delete(id));
    }
}
