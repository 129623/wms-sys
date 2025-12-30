package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseUnit;
import com.wms.mapper.BaseUnitMapper;
import com.wms.service.BaseUnitService;
import org.springframework.stereotype.Service;

@Service
public class BaseUnitServiceImpl extends ServiceImpl<BaseUnitMapper, BaseUnit> implements BaseUnitService {
}
