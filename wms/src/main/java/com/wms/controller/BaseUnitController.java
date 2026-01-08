package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.BaseUnit;
import com.wms.service.BaseUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unit")
public class BaseUnitController {

    @Autowired
    private BaseUnitService unitService;

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:unit:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<BaseUnit>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String unitName) {
        Page<BaseUnit> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseUnit> queryWrapper = new LambdaQueryWrapper<>();
        if (unitName != null && !unitName.isEmpty()) {
            queryWrapper.like(BaseUnit::getUnitName, unitName);
        }
        return Result.success(unitService.page(page, queryWrapper));
    }

    @GetMapping("/list")
    public Result<java.util.List<BaseUnit>> list() {
        return Result.success(unitService.list());
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:unit:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody BaseUnit unit) {
        return Result.success(unitService.save(unit));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:unit:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody BaseUnit unit) {
        return Result.success(unitService.updateById(unit));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:unit:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(unitService.removeById(id));
    }
}
