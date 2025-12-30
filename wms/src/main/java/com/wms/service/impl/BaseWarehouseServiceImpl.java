package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseWarehouse;
import com.wms.entity.BaseZone;
import com.wms.entity.BaseRack;
import com.wms.entity.BaseLocation;
import com.wms.mapper.BaseWarehouseMapper;
import com.wms.mapper.BaseZoneMapper;
import com.wms.mapper.BaseRackMapper;
import com.wms.mapper.BaseLocationMapper;
import com.wms.service.BaseWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaseWarehouseServiceImpl extends ServiceImpl<BaseWarehouseMapper, BaseWarehouse>
        implements BaseWarehouseService {

    @Autowired
    private BaseZoneMapper zoneMapper;

    @Autowired
    private BaseRackMapper rackMapper;

    @Autowired
    private BaseLocationMapper locationMapper;

    @Override
    public boolean add(com.wms.dto.BaseWarehouseAddDTO addDTO) {
        BaseWarehouse warehouse = new BaseWarehouse();
        org.springframework.beans.BeanUtils.copyProperties(addDTO, warehouse);
        // 设置默认状态为启用
        if (warehouse.getStatus() == null) {
            warehouse.setStatus(1);
        }
        return this.save(warehouse);
    }

    @Override
    public boolean update(com.wms.dto.BaseWarehouseUpdateDTO updateDTO) {
        BaseWarehouse warehouse = this.getById(updateDTO.getWarehouseId());
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }
        org.springframework.beans.BeanUtils.copyProperties(updateDTO, warehouse);
        return this.updateById(warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        BaseWarehouse warehouse = this.getById(id);
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }

        // 级联删除：使用Mapper批量更新，避免循环依赖
        // 标记所有相关库位为已删除（del_flag='1'）
        locationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.wms.entity.BaseLocation>()
                        .eq(com.wms.entity.BaseLocation::getWarehouseId, id)
                        .set(com.wms.entity.BaseLocation::getDelFlag, "1"));

        // 标记所有相关货架为已删除（del_flag='1'，因为该表没有status字段）
        rackMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.wms.entity.BaseRack>()
                        .eq(com.wms.entity.BaseRack::getWarehouseId, id)
                        .set(com.wms.entity.BaseRack::getDelFlag, "1"));

        // 标记所有相关库区为已删除（status=0）
        zoneMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.wms.entity.BaseZone>()
                        .eq(com.wms.entity.BaseZone::getWarehouseId, id)
                        .set(com.wms.entity.BaseZone::getStatus, 0));

        // 最后删除仓库本身
        warehouse.setStatus(0);
        return this.updateById(warehouse);
    }
}
