package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.WmsInventory;
import com.wms.vo.WmsInventoryVO;

public interface WmsInventoryService extends IService<WmsInventory> {

    IPage<WmsInventoryVO> pageVO(IPage<WmsInventory> page, Wrapper<WmsInventory> queryWrapper);

    /**
     * 入库增加库存
     * 
     * @param inventory 库存对象（包含 productId, locationId, batchNo, totalQty）
     * @param orderNo   关联单号
     * @param operator  操作人
     */
    void addInventory(WmsInventory inventory, String orderNo, String operator);

    /**
     * 锁定库存 (分配)
     * 
     * @param inventoryId 库存ID
     * @param qty         锁定数量
     */
    void lockInventory(Long inventoryId, Integer qty);

    /**
     * 扣减库存 (出库)
     * 
     * @param inventoryId 库存ID
     * @param qty         扣减数量
     * @param orderNo     单号
     * @param operator    操作人
     */
    void deductInventory(Long inventoryId, Integer qty, String orderNo, String operator);
}
