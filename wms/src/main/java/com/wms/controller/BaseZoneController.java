package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.dto.BaseZoneAddDTO;
import com.wms.dto.BaseZoneUpdateDTO;
import com.wms.entity.BaseZone;
import com.wms.service.BaseZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/zone")
public class BaseZoneController {

    @Autowired
    private BaseZoneService zoneService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('base:zone:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<BaseZone>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String zoneName,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status) {
        Page<BaseZone> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseZone> queryWrapper = new LambdaQueryWrapper<>();
        if (zoneName != null && !zoneName.isEmpty()) {
            queryWrapper.like(BaseZone::getZoneName, zoneName);
        }
        if (warehouseId != null) {
            queryWrapper.eq(BaseZone::getWarehouseId, warehouseId);
        }
        if (status != null) {
            queryWrapper.eq(BaseZone::getStatus, status);
        } else {
            // 默认只显示未删除的库区（status != 0）
            queryWrapper.ne(BaseZone::getStatus, 0);
        }
        // Usually list available zones or all? Logic delete implies we might want to
        // hide deleted ones usually, or filter by status.
        // If status is not provided, maybe we should default to show all or only
        // active?
        // Requirement says: "Query Criteria: Name, Warehouse, Status". If status not
        // provided, list all.

        return Result.success(zoneService.page(page, queryWrapper));
    }

    @GetMapping("/list")
    public Result<List<BaseZone>> list(@RequestParam(required = false) Long warehouseId) {
        LambdaQueryWrapper<BaseZone> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseZone::getStatus, 1); // Only list active zones for selection
        if (warehouseId != null) {
            queryWrapper.eq(BaseZone::getWarehouseId, warehouseId);
        }
        return Result.success(zoneService.list(queryWrapper));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('base:zone:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody BaseZoneAddDTO addDTO) {
        return Result.success(zoneService.add(addDTO));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('base:zone:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody BaseZoneUpdateDTO updateDTO) {
        return Result.success(zoneService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('base:zone:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(zoneService.delete(id));
    }
}
