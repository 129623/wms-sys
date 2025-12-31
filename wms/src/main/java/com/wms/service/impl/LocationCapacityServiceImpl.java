package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.entity.BaseLocation;
import com.wms.entity.BaseProduct;
import com.wms.entity.WmsInventory;
import com.wms.mapper.BaseLocationMapper;
import com.wms.mapper.BaseProductMapper;
import com.wms.mapper.WmsInventoryMapper;
import com.wms.service.LocationCapacityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LocationCapacityServiceImpl implements LocationCapacityService {

    @Autowired
    private BaseLocationMapper locationMapper;

    @Autowired
    private BaseProductMapper productMapper;

    @Autowired
    private WmsInventoryMapper inventoryMapper;

    @Override
    public void checkCapacity(Long locationId, Long productId, Integer quantity) {
        if (locationId == null || productId == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("参数无效：库位、产品、数量不能为空且数量必须大于0");
        }

        // 1. 获取目标库位信息
        BaseLocation location = locationMapper.selectById(locationId);
        if (location == null) {
            throw new RuntimeException("库位不存在: " + locationId);
        }

        // 检查库位是否设置了限制，如果没有限制（为0或null），则默认不校验或者认为无限
        // 这里假设如果 maxWeight 或 maxVolume 为 0/null，则通过校验（或根据业务需求调整）
        // 但通常 0 表示未设置，建议视为无限容量，或者视为不能存放。这里暂且视为不校验。
        boolean checkWeight = location.getMaxWeight() != null && location.getMaxWeight().doubleValue() > 0;
        boolean checkVolume = location.getMaxVolume() != null && location.getMaxVolume().doubleValue() > 0;

        if (!checkWeight && !checkVolume) {
            return; // 无限制，直接通过
        }

        // 2. 获取入库产品信息
        BaseProduct newProduct = productMapper.selectById(productId);
        if (newProduct == null) {
            throw new RuntimeException("产品不存在: " + productId);
        }

        double singleWeight = newProduct.getWeight() != null ? newProduct.getWeight().doubleValue() : 0.0;
        // 体积计算：cm -> m³
        // L*W*H / 1,000,000
        double len = newProduct.getLength() != null ? newProduct.getLength().doubleValue() : 0.0;
        double wid = newProduct.getWidth() != null ? newProduct.getWidth().doubleValue() : 0.0;
        double hei = newProduct.getHeight() != null ? newProduct.getHeight().doubleValue() : 0.0;
        double singleVolume = (len * wid * hei) / 1000000.0;

        double incomingWeight = singleWeight * quantity;
        double incomingVolume = singleVolume * quantity;

        // 3. 计算当前已用量
        double currentTotalWeight = 0.0;
        double currentTotalVolume = 0.0;

        // 查询该库位下所有库存（包括其他批次、其他产品）
        // 注意：由于实体类可能缺失delFlag字段，使用硬编码列名过滤
        List<WmsInventory> inventories = inventoryMapper.selectList(
                new LambdaQueryWrapper<WmsInventory>()
                        .eq(WmsInventory::getLocationId, locationId)
                        .apply("del_flag = '0'") // 使用apply手动添加SQL片段，避免编译错误
                        .gt(WmsInventory::getTotalQty, 0));

        for (WmsInventory inv : inventories) {
            // 如果是同一个产品的同一批次（尚未入库），这部分逻辑通常由调用方控制是否包含
            // 假设这里计算的是 "数据库中已存在的" + "本次要入的"

            // 为了准确，需要查询每个库存对应的产品信息
            BaseProduct p = productMapper.selectById(inv.getProductId());
            if (p != null) {
                double w = p.getWeight() != null ? p.getWeight().doubleValue() : 0.0;
                double l = p.getLength() != null ? p.getLength().doubleValue() : 0.0;
                double wd = p.getWidth() != null ? p.getWidth().doubleValue() : 0.0;
                double h = p.getHeight() != null ? p.getHeight().doubleValue() : 0.0;
                double v = (l * wd * h) / 1000000.0;

                currentTotalWeight += w * inv.getTotalQty();
                currentTotalVolume += v * inv.getTotalQty();
            }
        }

        // 4. 校验
        if (checkWeight) {
            double finalWeight = currentTotalWeight + incomingWeight;
            if (finalWeight > location.getMaxWeight().doubleValue()) {
                throw new RuntimeException(String.format("库位[%s]超重！上限:%.2fkg, 当前:%.2fkg, 拟入:%.2fkg, 预计总重:%.2fkg",
                        location.getLocationCode(), location.getMaxWeight(), currentTotalWeight, incomingWeight,
                        finalWeight));
            }
        }

        if (checkVolume) {
            double finalVolume = currentTotalVolume + incomingVolume;
            if (finalVolume > location.getMaxVolume().doubleValue()) {
                throw new RuntimeException(String.format("库位[%s]体积不足！上限:%.4fm³, 当前:%.4fm³, 拟入:%.4fm³, 预计总体积:%.4fm³",
                        location.getLocationCode(), location.getMaxVolume(), currentTotalVolume, incomingVolume,
                        finalVolume));
            }
        }
    }

    @Override
    public String getLoadDetails(Long locationId) {
        // 简化的查询逻辑，用于调试
        return "Capacity details for location " + locationId;
    }
}
