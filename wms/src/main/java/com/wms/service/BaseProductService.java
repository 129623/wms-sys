package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.BaseProduct;

public interface BaseProductService extends IService<BaseProduct> {
    boolean add(com.wms.dto.BaseProductAddDTO addDTO);

    boolean update(com.wms.dto.BaseProductUpdateDTO updateDTO);
}
