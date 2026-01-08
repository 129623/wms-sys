package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.Result;
import com.wms.entity.BaseCategory;
import com.wms.service.BaseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class BaseCategoryController {

    @Autowired
    private BaseCategoryService categoryService;

    @GetMapping("/list")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:category:list') or hasAuthority('ROLE_ADMIN')")
    public Result<List<BaseCategory>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:category:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.BaseCategoryAddDTO addDTO) {
        return Result.success(categoryService.add(addDTO));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:category:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody com.wms.dto.BaseCategoryUpdateDTO updateDTO) {
        return Result.success(categoryService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:category:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(categoryService.removeById(id));
    }
}
