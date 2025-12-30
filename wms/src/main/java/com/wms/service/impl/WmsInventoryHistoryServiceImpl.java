package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WmsInventoryHistory;
import com.wms.mapper.WmsInventoryHistoryMapper;
import com.wms.service.WmsInventoryHistoryService;
import org.springframework.stereotype.Service;

@Service
public class WmsInventoryHistoryServiceImpl extends ServiceImpl<WmsInventoryHistoryMapper, WmsInventoryHistory>
        implements WmsInventoryHistoryService {
}
