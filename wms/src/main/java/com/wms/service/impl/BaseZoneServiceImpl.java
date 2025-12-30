package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.dto.BaseZoneAddDTO;
import com.wms.dto.BaseZoneUpdateDTO;
import com.wms.entity.BaseZone;
import com.wms.mapper.BaseZoneMapper;
import com.wms.mapper.BaseRackMapper;
import com.wms.mapper.BaseLocationMapper;
import com.wms.service.BaseZoneService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaseZoneServiceImpl extends ServiceImpl<BaseZoneMapper, BaseZone> implements BaseZoneService {

    @Autowired
    private com.wms.service.BaseWarehouseService warehouseService;

    @Autowired
    private BaseRackMapper rackMapper;

    @Autowired
    private BaseLocationMapper locationMapper;

    @Override
    public boolean add(BaseZoneAddDTO addDTO) {
        // 校验仓库是否存在
        if (warehouseService.getById(addDTO.getWarehouseId()) == null) {
            throw new RuntimeException("指定的仓库是否存在");
        }

        BaseZone zone = new BaseZone();
        BeanUtils.copyProperties(addDTO, zone);
        // Ensure default status is 1 (Available) if not provided
        if (zone.getStatus() == null) {
            zone.setStatus(1);
        }
        return this.save(zone);
    }

    @Override
    public boolean update(BaseZoneUpdateDTO updateDTO) {
        BaseZone zone = this.getById(updateDTO.getZoneId());
        if (zone == null) {
            throw new RuntimeException("库区不存在");
        }

        // 如果修改了WarehouseId，需要校验新仓库
        if (updateDTO.getWarehouseId() != null) {
            if (warehouseService.getById(updateDTO.getWarehouseId()) == null) {
                throw new RuntimeException("指定的仓库不存在");
            }
        }

        BeanUtils.copyProperties(updateDTO, zone);
        return this.updateById(zone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        BaseZone zone = this.getById(id);
        if (zone == null) {
            throw new RuntimeException("库区不存在");
        }

        // 级联删除：标记所有相关货架为已删除（del_flag='1'）
        rackMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.wms.entity.BaseRack>()
                        .eq(com.wms.entity.BaseRack::getZoneId, id)
                        .set(com.wms.entity.BaseRack::getDelFlag, "1"));

        // 级联删除：标记所有相关库位为已删除（del_flag='1'）
        locationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.wms.entity.BaseLocation>()
                        .eq(com.wms.entity.BaseLocation::getZoneId, id)
                        .set(com.wms.entity.BaseLocation::getDelFlag, "1"));

        // 最后删除库区本身（设置 status=0）
        zone.setStatus(0);
        return this.updateById(zone);
    }
}
