package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WmsInboundOrder;
import com.wms.mapper.WmsInboundOrderMapper;
import com.wms.service.WmsInboundOrderService;
import org.springframework.stereotype.Service;

@Service
public class WmsInboundOrderServiceImpl extends ServiceImpl<WmsInboundOrderMapper, WmsInboundOrder>
                implements WmsInboundOrderService {

        @org.springframework.beans.factory.annotation.Autowired
        private com.wms.service.WmsInboundDetailService inboundDetailService;

        @org.springframework.beans.factory.annotation.Autowired
        private com.wms.service.WmsInventoryService inventoryService;

        @Override
        @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
        public boolean addInbound(com.wms.dto.WmsInboundAddDTO addDTO) {
                // 1. Create Order
                WmsInboundOrder order = new WmsInboundOrder();
                org.springframework.beans.BeanUtils.copyProperties(addDTO, order);

                // Generate OrderNo
                String orderNo = "IN" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                                .format(java.time.LocalDateTime.now()) + (int) (Math.random() * 1000);
                order.setOrderNo(orderNo);
                order.setCreateTime(new java.util.Date());
                order.setStatus(0); // 0: Created

                this.save(order);

                // 2. Create Details
                if (addDTO.getDetails() != null && !addDTO.getDetails().isEmpty()) {
                        java.util.List<com.wms.entity.WmsInboundDetail> details = addDTO.getDetails().stream()
                                        .map(d -> {
                                                com.wms.entity.WmsInboundDetail detail = new com.wms.entity.WmsInboundDetail();
                                                org.springframework.beans.BeanUtils.copyProperties(d, detail);
                                                detail.setInboundId(order.getInboundId());
                                                detail.setReceivedQty(0);
                                                return detail;
                                        }).collect(java.util.stream.Collectors.toList());

                        inboundDetailService.saveBatch(details);
                }
                return true;
        }

        @org.springframework.beans.factory.annotation.Autowired
        private com.wms.service.LocationCapacityService locationCapacityService;

        @Override
        @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
        public boolean executeReceipt(com.wms.dto.WmsInboundReceiptDTO receiptDTO, String operator) {
                // 1. Get Detail
                com.wms.entity.WmsInboundDetail detail = inboundDetailService.getById(receiptDTO.getDetailId());
                if (detail == null) {
                        throw new RuntimeException("入库明细不存在");
                }

                WmsInboundOrder order = this.getById(detail.getInboundId());
                if (order == null) {
                        throw new RuntimeException("入库单不存在");
                }
                if (order.getStatus() == 2) { // Assuming 2 is Finished
                        throw new RuntimeException("入库单已完成");
                }

                // Validate Qty
                if (detail.getReceivedQty() + receiptDTO.getQty() > detail.getPlanQty()) {
                        throw new RuntimeException("入库数量超出计划数量");
                }

                // CHECK CAPACITY BEFORE ADDING INVENTORY
                locationCapacityService.checkCapacity(receiptDTO.getLocationId(), detail.getProductId(),
                                receiptDTO.getQty());

                // 2. Add to Inventory
                com.wms.entity.WmsInventory inventory = new com.wms.entity.WmsInventory();
                inventory.setWarehouseId(order.getWarehouseId());
                inventory.setProductId(detail.getProductId());
                inventory.setLocationId(receiptDTO.getLocationId());
                inventory.setTotalQty(receiptDTO.getQty());
                inventory.setBatchNo(order.getOrderNo()); // Use OrderNo as BatchNo

                inventoryService.addInventory(inventory, order.getOrderNo(), operator);

                // 3. Update Detail
                detail.setReceivedQty(detail.getReceivedQty() + receiptDTO.getQty());
                inboundDetailService.updateById(detail);

                // 4. Update Order Status
                updateOrderStatus(order);

                return true;
        }

        private void updateOrderStatus(WmsInboundOrder order) {
                // Check if all details are fully received
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.wms.entity.WmsInboundDetail> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(com.wms.entity.WmsInboundDetail::getInboundId, order.getInboundId());
                java.util.List<com.wms.entity.WmsInboundDetail> details = inboundDetailService.list(wrapper);

                boolean allFinished = true;
                boolean anyReceived = false;

                for (com.wms.entity.WmsInboundDetail d : details) {
                        if (d.getReceivedQty() < d.getPlanQty()) {
                                allFinished = false;
                        }
                        if (d.getReceivedQty() > 0) {
                                anyReceived = true;
                        }
                }

                int newStatus = order.getStatus();
                if (allFinished) {
                        newStatus = 2; // Finished
                } else if (anyReceived) {
                        newStatus = 1; // Partial
                }

                if (newStatus != order.getStatus()) {
                        order.setStatus(newStatus);
                        this.updateById(order);
                }
        }
}
