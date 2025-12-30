package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WmsOutboundOrder;
import com.wms.mapper.WmsOutboundOrderMapper;
import com.wms.service.WmsOutboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WmsOutboundOrderServiceImpl extends ServiceImpl<WmsOutboundOrderMapper, WmsOutboundOrder>
                implements WmsOutboundOrderService {

        @Autowired
        private com.wms.service.WmsOutboundDetailService outboundDetailService;

        @Autowired
        private com.wms.service.WmsInventoryService inventoryService;

        @Override
        @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
        public boolean addOutbound(com.wms.dto.WmsOutboundAddDTO addDTO) {
                // 1. Create Order
                WmsOutboundOrder order = new WmsOutboundOrder();
                org.springframework.beans.BeanUtils.copyProperties(addDTO, order);

                // Generate OrderNo
                String orderNo = "OUT" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                                .format(java.time.LocalDateTime.now()) + (int) (Math.random() * 1000);
                order.setOrderNo(orderNo);
                order.setCreateTime(new java.util.Date());
                order.setStatus(0); // 0: Created

                this.save(order);

                // 2. Create Details
                if (addDTO.getDetails() != null && !addDTO.getDetails().isEmpty()) {
                        java.util.List<com.wms.entity.WmsOutboundDetail> details = addDTO.getDetails().stream()
                                        .map(d -> {
                                                com.wms.entity.WmsOutboundDetail detail = new com.wms.entity.WmsOutboundDetail();
                                                org.springframework.beans.BeanUtils.copyProperties(d, detail);
                                                detail.setOutboundId(order.getOutboundId());
                                                detail.setPickedQty(0);
                                                return detail;
                                        }).collect(java.util.stream.Collectors.toList());

                        outboundDetailService.saveBatch(details);
                }
                return true;
        }

        @Override
        @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
        public boolean executePick(com.wms.dto.WmsOutboundPickDTO pickDTO, String operator) {
                // 1. Get Detail
                com.wms.entity.WmsOutboundDetail detail = outboundDetailService.getById(pickDTO.getDetailId());
                if (detail == null) {
                        throw new RuntimeException("出库明细不存在");
                }

                WmsOutboundOrder order = this.getById(detail.getOutboundId());
                if (order == null) {
                        throw new RuntimeException("出库单不存在");
                }
                if (order.getStatus() == 2) {
                        throw new RuntimeException("出库单已完成");
                }

                // Validate Qty
                if (detail.getPickedQty() + pickDTO.getQty() > detail.getPlanQty()) {
                        throw new RuntimeException("拣货数量超出计划数量");
                }

                // Safety check: Ensure inventory matches product
                com.wms.entity.WmsInventory inv = inventoryService.getById(pickDTO.getInventoryId());
                if (inv == null) {
                        throw new RuntimeException("库存不存在");
                }
                if (!inv.getProductId().equals(detail.getProductId())) {
                        throw new RuntimeException("库存商品与出库明细不符");
                }

                // 2. Lock Inventory (if not already locked)
                // Check available quantity
                int availableQty = inv.getTotalQty() - inv.getFrozenQty();
                if (availableQty < pickDTO.getQty()) {
                        throw new RuntimeException("可用库存不足，当前可用: " + availableQty);
                }

                // Lock the inventory before deducting
                inventoryService.lockInventory(pickDTO.getInventoryId(), pickDTO.getQty());

                // 3. Deduct Inventory
                inventoryService.deductInventory(pickDTO.getInventoryId(), pickDTO.getQty(), order.getOrderNo(),
                                operator);

                // 3. Update Detail
                detail.setPickedQty(detail.getPickedQty() + pickDTO.getQty());
                outboundDetailService.updateById(detail);

                // 4. Update Order Status
                updateOrderStatus(order);

                return true;
        }

        private void updateOrderStatus(WmsOutboundOrder order) {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.wms.entity.WmsOutboundDetail> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(com.wms.entity.WmsOutboundDetail::getOutboundId, order.getOutboundId());
                java.util.List<com.wms.entity.WmsOutboundDetail> details = outboundDetailService.list(wrapper);

                boolean allFinished = true;
                boolean anyPicked = false;

                for (com.wms.entity.WmsOutboundDetail d : details) {
                        if (d.getPickedQty() < d.getPlanQty()) {
                                allFinished = false;
                        }
                        if (d.getPickedQty() > 0) {
                                anyPicked = true;
                        }
                }

                int newStatus = order.getStatus();
                if (allFinished) {
                        newStatus = 2; // Finished
                } else if (anyPicked) {
                        newStatus = 1; // Partial
                }

                if (newStatus != order.getStatus()) {
                        order.setStatus(newStatus);
                        this.updateById(order);
                }
        }
}
