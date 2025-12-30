package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.BaseProduct;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BaseProductMapper extends BaseMapper<BaseProduct> {
}
