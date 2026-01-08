package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.WmsOutboundOrder;
import com.wms.service.WmsOutboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/outbound")
public class WmsOutboundOrderController {

    @Autowired
    private WmsOutboundOrderService outboundOrderService;

    @Autowired
    private com.wms.service.WmsOutboundDetailService outboundDetailService;

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:outbound:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<WmsOutboundOrder>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo) {
        Page<WmsOutboundOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsOutboundOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            queryWrapper.like(WmsOutboundOrder::getOrderNo, orderNo);
        }
        queryWrapper.orderByAsc(WmsOutboundOrder::getStatus).orderByDesc(WmsOutboundOrder::getCreateTime);
        return Result.success(outboundOrderService.page(page, queryWrapper));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:outbound:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.WmsOutboundAddDTO addDTO) {
        return Result.success(outboundOrderService.addOutbound(addDTO));
    }

    @PostMapping("/pick")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:outbound:pick') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> pick(@RequestBody com.wms.dto.WmsOutboundPickDTO pickDTO) {
        String operator = "Admin"; // TODO: get from context
        return Result.success(outboundOrderService.executePick(pickDTO, operator));
    }

    @GetMapping("/details/{outboundId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:outbound:list') or hasAuthority('ROLE_ADMIN')")
    public Result<java.util.List<com.wms.entity.WmsOutboundDetail>> listDetails(@PathVariable Long outboundId) {
        return Result.success(outboundDetailService.list(new LambdaQueryWrapper<com.wms.entity.WmsOutboundDetail>()
                .eq(com.wms.entity.WmsOutboundDetail::getOutboundId, outboundId)));
    }

    // Deprecated or keep for simple updates
    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:outbound:list') or hasAuthority('ROLE_ADMIN')")
    public Result<WmsOutboundOrder> getById(@PathVariable Long id) {
        return Result.success(outboundOrderService.getById(id));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:outbound:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(outboundOrderService.removeById(id));
    }

    @PostMapping("/save")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:outbound:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> save(@RequestBody WmsOutboundOrder order) {
        return Result.success(outboundOrderService.saveOrUpdate(order));
    }
}
