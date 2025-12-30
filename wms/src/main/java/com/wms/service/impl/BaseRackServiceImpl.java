package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.dto.BaseRackAddDTO;
import com.wms.dto.BaseRackUpdateDTO;
import com.wms.entity.BaseRack;
import com.wms.mapper.BaseRackMapper;
import com.wms.mapper.BaseLocationMapper;
import com.wms.service.BaseRackService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaseRackServiceImpl extends ServiceImpl<BaseRackMapper, BaseRack> implements BaseRackService {

    @Autowired
    private com.wms.service.BaseZoneService zoneService;

    @Autowired
    private BaseLocationMapper locationMapper;

    @Override
    public boolean add(BaseRackAddDTO addDTO) {
        // 校验库区是否存在
        com.wms.entity.BaseZone zone = zoneService.getById(addDTO.getZoneId());
        if (zone == null) {
            throw new RuntimeException("指定的库区不存在");
        }

        BaseRack rack = new BaseRack();
        BeanUtils.copyProperties(addDTO, rack);

        // 强制同步仓库ID
        rack.setWarehouseId(zone.getWarehouseId());

        return this.save(rack);
    }

    @Override
    public boolean update(BaseRackUpdateDTO updateDTO) {
        BaseRack rack = this.getById(updateDTO.getRackId());
        if (rack == null) {
            throw new RuntimeException("货架不存在");
        }

        // 如果修改了ZoneId，需要重新校验并同步WarehouseId
        if (updateDTO.getZoneId() != null && !updateDTO.getZoneId().equals(rack.getZoneId())) {
            com.wms.entity.BaseZone zone = zoneService.getById(updateDTO.getZoneId());
            if (zone == null) {
                throw new RuntimeException("指定的库区不存在");
            }
            rack.setWarehouseId(zone.getWarehouseId());
        }

        BeanUtils.copyProperties(updateDTO, rack);
        return this.updateById(rack);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        BaseRack rack = this.getById(id);
        if (rack == null) {
            throw new RuntimeException("货架不存在");
        }

        // 级联删除：标记所有相关库位为已删除（del_flag='1'）
        locationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.wms.entity.BaseLocation>()
                        .eq(com.wms.entity.BaseLocation::getRackId, id)
                        .set(com.wms.entity.BaseLocation::getDelFlag, "1"));

        // @TableLogic 会自动将这个操作转换为逻辑删除（设置 del_flag='1'）
        return this.removeById(id);
    }
}
