package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.WmsInventory;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.wms.vo.WmsInventoryVO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WmsInventoryMapper extends BaseMapper<WmsInventory> {

    IPage<WmsInventoryVO> selectInventoryPage(IPage<WmsInventory> page,
            @Param(Constants.WRAPPER) com.baomidou.mybatisplus.core.conditions.Wrapper<WmsInventory> queryWrapper);
}
