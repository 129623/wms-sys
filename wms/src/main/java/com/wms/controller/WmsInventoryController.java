package com.wms.controller;

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
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<com.wms.vo.WmsInventoryVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName) {
        Page<WmsInventory> page = new Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WmsInventory> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (productId != null) {
            queryWrapper.eq("i.product_id", productId);
        }
        // 支持按产品名称模糊查询 (虽然当前前端主要传ID，但扩展一下)
        // 注意：由于是VO查询，产品名称实际上是在XML的join里处理的，wrapper这里的条件如果是针对WmsInventory表的字段才有效
        // 如果要支持产品名称搜索，最好是在XML里写动态SQL，或者传参给Mapper。
        // 这里暂时保持原样，只针对 inventory 表的字段过滤. 如果需要按产品名搜, 需要改 XML 里的 <if>

        return Result.success(inventoryService.pageVO(page, queryWrapper));
    }
}
