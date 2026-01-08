package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.BaseProductLabel;
import com.wms.service.BaseProductLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-label")
public class BaseProductLabelController {

    @Autowired
    private BaseProductLabelService labelService;

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:label:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<BaseProductLabel>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String labelName) {
        Page<BaseProductLabel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseProductLabel> queryWrapper = new LambdaQueryWrapper<>();
        if (labelName != null && !labelName.isEmpty()) {
            queryWrapper.like(BaseProductLabel::getLabelName, labelName);
        }
        return Result.success(labelService.page(page, queryWrapper));
    }

    @GetMapping("/list")
    public Result<java.util.List<BaseProductLabel>> list(@RequestParam(required = false) String labelName) {
        LambdaQueryWrapper<BaseProductLabel> queryWrapper = new LambdaQueryWrapper<>();
        if (labelName != null && !labelName.isEmpty()) {
            queryWrapper.like(BaseProductLabel::getLabelName, labelName);
        }
        return Result.success(labelService.list(queryWrapper));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:label:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody BaseProductLabel label) {
        return Result.success(labelService.save(label));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:label:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody BaseProductLabel label) {
        return Result.success(labelService.updateById(label));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:label:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(labelService.removeById(id));
    }
}
