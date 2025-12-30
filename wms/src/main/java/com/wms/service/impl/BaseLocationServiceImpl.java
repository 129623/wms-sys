package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseLocation;
import com.wms.mapper.BaseLocationMapper;
import com.wms.service.BaseLocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.wms.service.BaseRackService;
import com.wms.entity.BaseRack;

@Service
public class BaseLocationServiceImpl extends ServiceImpl<BaseLocationMapper, BaseLocation>
                implements BaseLocationService {

        @Autowired
        private BaseRackService rackService;

        @Override
        public boolean add(com.wms.dto.BaseLocationAddDTO addDTO) {
                // 验证货架是否存在
                com.wms.entity.BaseRack rack = rackService.getById(addDTO.getRackId());
                if (rack == null) {
                        throw new RuntimeException("指定的货架不存在");
                }

                BaseLocation location = new BaseLocation();
                org.springframework.beans.BeanUtils.copyProperties(addDTO, location);

                // 强制使用货架关联的仓库和库区，保证数据一致性
                location.setWarehouseId(rack.getWarehouseId());
                location.setZoneId(rack.getZoneId());

                return this.save(location);
        }

        @Override
        public boolean update(com.wms.dto.BaseLocationUpdateDTO updateDTO) {
                BaseLocation location = this.getById(updateDTO.getLocationId());
                if (location == null) {
                        throw new RuntimeException("库位不存在");
                }

                // 如果修改了货架ID，需要验证新货架
                if (updateDTO.getRackId() != null && !updateDTO.getRackId().equals(location.getRackId())) {
                        com.wms.entity.BaseRack rack = rackService.getById(updateDTO.getRackId());
                        if (rack == null) {
                                throw new RuntimeException("指定的货架不存在");
                        }
                        // 同步更新关联的仓库和库区
                        location.setWarehouseId(rack.getWarehouseId());
                        location.setZoneId(rack.getZoneId());
                }

                org.springframework.beans.BeanUtils.copyProperties(updateDTO, location);
                return this.updateById(location);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean batchAdd(com.wms.dto.BaseLocationBatchAddDTO batchAddDTO) {
                // 1. 检查货架是否存在
                com.wms.entity.BaseRack rack = rackService.getById(batchAddDTO.getRackId());
                if (rack == null) {
                        throw new RuntimeException("指定的货架不存在，ID: " + batchAddDTO.getRackId());
                }

                Integer rowNum = batchAddDTO.getRowNum();
                Integer layerNum = batchAddDTO.getLayerNum();

                for (int r = 1; r <= rowNum; r++) {
                        for (int l = 1; l <= layerNum; l++) {
                                BaseLocation location = new BaseLocation();
                                // 核心修改：直接使用 Rack 中的 WarehouseId 和 ZoneId
                                location.setWarehouseId(rack.getWarehouseId());
                                location.setZoneId(rack.getZoneId());
                                location.setRackId(rack.getRackId());
                                location.setRowNo(r);
                                location.setLayerNo(l);
                                location.setStatus(0); // 默认空闲

                                // 生成库位编码：RackCode-Rxx-Lxx
                                String code = String.format("%s-%02d-%02d", rack.getRackCode(), r, l);
                                location.setLocationCode(code);

                                // 幂等性处理：如果库位已存在，则跳过，避免报错
                                if (this.lambdaQuery().eq(BaseLocation::getLocationCode, code).count() > 0) {
                                        continue;
                                }

                                this.save(location);
                        }
                }
                return true;
        }
}
