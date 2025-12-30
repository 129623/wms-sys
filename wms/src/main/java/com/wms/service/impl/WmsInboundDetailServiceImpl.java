package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WmsInboundDetail;
import com.wms.mapper.WmsInboundDetailMapper;
import com.wms.service.WmsInboundDetailService;
import org.springframework.stereotype.Service;

@Service
public class WmsInboundDetailServiceImpl extends ServiceImpl<WmsInboundDetailMapper, WmsInboundDetail>
        implements WmsInboundDetailService {
}
