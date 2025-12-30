package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.BaseProduct;
import com.wms.service.BaseProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class BaseProductController {

    @Autowired
    private BaseProductService productService;

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:product:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<BaseProduct>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productName) {
        Page<BaseProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseProduct> queryWrapper = new LambdaQueryWrapper<>();
        if (productName != null && !productName.isEmpty()) {
            queryWrapper.like(BaseProduct::getProductName, productName);
        }
        return Result.success(productService.page(page, queryWrapper));
    }

    @GetMapping("/list")
    public Result<java.util.List<BaseProduct>> list(com.wms.dto.BaseProductQueryDTO queryDTO) {
        LambdaQueryWrapper<BaseProduct> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getProductName() != null && !queryDTO.getProductName().isEmpty()) {
            queryWrapper.like(BaseProduct::getProductName, queryDTO.getProductName());
        }
        if (queryDTO.getSkuCode() != null && !queryDTO.getSkuCode().isEmpty()) {
            queryWrapper.like(BaseProduct::getSkuCode, queryDTO.getSkuCode());
        }
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(BaseProduct::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getUnitId() != null) {
            queryWrapper.eq(BaseProduct::getUnitId, queryDTO.getUnitId());
        }
        if (queryDTO.getStorageTypeId() != null) {
            queryWrapper.eq(BaseProduct::getStorageTypeId, queryDTO.getStorageTypeId());
        }
        if (queryDTO.getLabelId() != null) {
            queryWrapper.eq(BaseProduct::getLabelId, queryDTO.getLabelId());
        }
        return Result.success(productService.list(queryWrapper));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:product:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.BaseProductAddDTO addDTO) {
        return Result.success(productService.add(addDTO));
    }

    @PutMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:product:edit') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody com.wms.dto.BaseProductUpdateDTO updateDTO) {
        return Result.success(productService.update(updateDTO));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:product:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productService.removeById(id));
    }
}
