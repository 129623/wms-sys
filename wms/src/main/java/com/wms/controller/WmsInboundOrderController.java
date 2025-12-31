package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.WmsInboundOrder;
import com.wms.service.WmsInboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inbound")
public class WmsInboundOrderController {

    @Autowired
    private WmsInboundOrderService inboundOrderService;

    @Autowired
    private com.wms.service.WmsInboundDetailService inboundDetailService;

    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:inbound:list') or hasAuthority('ROLE_ADMIN')")
    public Result<Page<WmsInboundOrder>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo) {
        Page<WmsInboundOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WmsInboundOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            queryWrapper.like(WmsInboundOrder::getOrderNo, orderNo);
        }
        queryWrapper.orderByAsc(WmsInboundOrder::getStatus).orderByDesc(WmsInboundOrder::getCreateTime);
        return Result.success(inboundOrderService.page(page, queryWrapper));
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:inbound:add') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody com.wms.dto.WmsInboundAddDTO addDTO) {
        return Result.success(inboundOrderService.addInbound(addDTO));
    }

    @PostMapping("/receipt")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:inbound:receipt') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> receipt(@RequestBody com.wms.dto.WmsInboundReceiptDTO receiptDTO) {
        // Build operator from context if possible, or pass "Admin" for now
        // In a real app, use
        // SecurityContextHolder.getContext().getAuthentication().getName()
        String operator = "Admin";
        return Result.success(inboundOrderService.executeReceipt(receiptDTO, operator));
    }

    @GetMapping("/details/{inboundId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:inbound:list') or hasAuthority('ROLE_ADMIN')")
    public Result<java.util.List<com.wms.entity.WmsInboundDetail>> listDetails(@PathVariable Long inboundId) {
        return Result.success(inboundDetailService.list(new LambdaQueryWrapper<com.wms.entity.WmsInboundDetail>()
                .eq(com.wms.entity.WmsInboundDetail::getInboundId, inboundId)));
    }

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:inbound:list') or hasAuthority('ROLE_ADMIN')")
    public Result<WmsInboundOrder> getById(@PathVariable Long id) {
        return Result.success(inboundOrderService.getById(id));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('wms:inbound:delete') or hasAuthority('ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(inboundOrderService.removeById(id));
    }
}
