package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.WmsInventory;
import com.wms.service.WmsInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class WmsInventoryController {

    @Autowired
    private WmsInventoryService inventoryService;

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:inventory:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<WmsInventory>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productId) {
        Page<WmsInventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsInventory> queryWrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            queryWrapper.eq(WmsInventory::getProductId, productId);
        }
        return Result.success(inventoryService.page(page, queryWrapper));
    }
}
