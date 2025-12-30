package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.BaseLocation;
import com.wms.service.BaseLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
public class BaseLocationController {

    @Autowired
    private BaseLocationService locationService;

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:location:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<BaseLocation>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String locationCode) {
        Page<BaseLocation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseLocation> queryWrapper = new LambdaQueryWrapper<>();
        if (locationCode != null && !locationCode.isEmpty()) {
            queryWrapper.like(BaseLocation::getLocationCode, locationCode);
        }
        return Result.success(locationService.page(page, queryWrapper));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:location:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.BaseLocationAddDTO addDTO) {
        return Result.success(locationService.add(addDTO));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:location:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody com.wms.dto.BaseLocationUpdateDTO updateDTO) {
        return Result.success(locationService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:location:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(locationService.removeById(id));
    }

    @PostMapping("/batchAdd")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:location:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> batchAdd(@RequestBody com.wms.dto.BaseLocationBatchAddDTO batchAddDTO) {
        return Result.success(locationService.batchAdd(batchAddDTO));
    }
}
