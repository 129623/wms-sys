package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WmsOutboundDetail;
import com.wms.mapper.WmsOutboundDetailMapper;
import com.wms.service.WmsOutboundDetailService;
import org.springframework.stereotype.Service;

@Service
public class WmsOutboundDetailServiceImpl extends ServiceImpl<WmsOutboundDetailMapper, WmsOutboundDetail>
        implements WmsOutboundDetailService {
}
