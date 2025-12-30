package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.BaseWarehouse;

public interface BaseWarehouseService extends IService<BaseWarehouse> {
    boolean add(com.wms.dto.BaseWarehouseAddDTO addDTO);

    boolean update(com.wms.dto.BaseWarehouseUpdateDTO updateDTO);

    boolean delete(Long id);
}
