package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseStorageType;
import com.wms.mapper.BaseStorageTypeMapper;
import com.wms.service.BaseStorageTypeService;
import org.springframework.stereotype.Service;

@Service
public class BaseStorageTypeServiceImpl extends ServiceImpl<BaseStorageTypeMapper, BaseStorageType>
        implements BaseStorageTypeService {
}
