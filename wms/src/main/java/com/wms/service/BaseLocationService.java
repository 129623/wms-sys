package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.BaseLocation;

public interface BaseLocationService extends IService<BaseLocation> {
    boolean add(com.wms.dto.BaseLocationAddDTO addDTO);

    boolean update(com.wms.dto.BaseLocationUpdateDTO updateDTO);

    boolean batchAdd(com.wms.dto.BaseLocationBatchAddDTO batchAddDTO);
}
