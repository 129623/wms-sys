package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.dto.BaseRackAddDTO;
import com.wms.dto.BaseRackUpdateDTO;
import com.wms.entity.BaseRack;
import com.wms.service.BaseRackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rack")
public class BaseRackController {

    @Autowired
    private BaseRackService rackService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('base:rack:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<BaseRack>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) String rackType) {
        Page<BaseRack> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseRack> queryWrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            queryWrapper.eq(BaseRack::getWarehouseId, warehouseId);
        }
        if (zoneId != null) {
            queryWrapper.eq(BaseRack::getZoneId, zoneId);
        }
        if (rackType != null && !rackType.isEmpty()) {
            queryWrapper.eq(BaseRack::getRackType, rackType);
        }
        return Result.success(rackService.page(page, queryWrapper));
    }

    @GetMapping("/list")
    public Result<List<BaseRack>> list(@RequestParam(required = false) Long zoneId) {
        LambdaQueryWrapper<BaseRack> queryWrapper = new LambdaQueryWrapper<>();
        if (zoneId != null) {
            queryWrapper.eq(BaseRack::getZoneId, zoneId);
        }
        return Result.success(rackService.list(queryWrapper));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('base:rack:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody BaseRackAddDTO addDTO) {
        return Result.success(rackService.add(addDTO));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('base:rack:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody BaseRackUpdateDTO updateDTO) {
        return Result.success(rackService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('base:rack:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(rackService.delete(id));
    }
}
