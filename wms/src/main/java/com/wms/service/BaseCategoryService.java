package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.dto.BaseCategoryAddDTO;
import com.wms.dto.BaseCategoryUpdateDTO;
import com.wms.entity.BaseCategory;

public interface BaseCategoryService extends IService<BaseCategory> {
    boolean add(BaseCategoryAddDTO addDTO);

    boolean update(BaseCategoryUpdateDTO updateDTO);

    boolean delete(Long id);
}
