package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.dto.BaseZoneAddDTO;
import com.wms.dto.BaseZoneUpdateDTO;
import com.wms.entity.BaseZone;

public interface BaseZoneService extends IService<BaseZone> {
    boolean add(BaseZoneAddDTO addDTO);

    boolean update(BaseZoneUpdateDTO updateDTO);

    boolean delete(Long id);
}
