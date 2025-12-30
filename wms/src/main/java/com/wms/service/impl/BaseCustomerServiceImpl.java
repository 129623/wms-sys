package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BaseCustomer;
import com.wms.mapper.BaseCustomerMapper;
import com.wms.service.BaseCustomerService;
import org.springframework.stereotype.Service;

@Service
public class BaseCustomerServiceImpl extends ServiceImpl<BaseCustomerMapper, BaseCustomer>
        implements BaseCustomerService {
}
