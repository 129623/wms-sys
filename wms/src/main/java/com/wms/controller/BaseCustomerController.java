package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.dto.BaseCustomerAddDTO;
import com.wms.dto.BaseCustomerQueryDTO;
import com.wms.dto.BaseCustomerUpdateDTO;
import com.wms.entity.BaseCustomer;
import com.wms.service.BaseCustomerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/base/customer")
public class BaseCustomerController {

    @Autowired
    private BaseCustomerService baseCustomerService;

    @PostMapping("/list")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:customer:list') or hasAuthority('ROLE_ADMIN')")
    public Result list(@RequestBody BaseCustomerQueryDTO queryDTO) {
        Page<BaseCustomer> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<BaseCustomer> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getCustomerName())) {
            queryWrapper.like(BaseCustomer::getCustomerName, queryDTO.getCustomerName());
        }
        if (StringUtils.hasText(queryDTO.getCustomerCode())) {
            queryWrapper.like(BaseCustomer::getCustomerCode, queryDTO.getCustomerCode());
        }
        if (queryDTO.getCustomerType() != null) {
            queryWrapper.eq(BaseCustomer::getCustomerType, queryDTO.getCustomerType());
        }
        
        queryWrapper.orderByDesc(BaseCustomer::getCreateTime);
        
        Page<BaseCustomer> result = baseCustomerService.page(page, queryWrapper);
        return Result.success(result);
    }

    @PostMapping("/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:customer:add') or hasAuthority('ROLE_ADMIN')")
    public Result add(@RequestBody BaseCustomerAddDTO addDTO) {
        BaseCustomer customer = new BaseCustomer();
        BeanUtils.copyProperties(addDTO, customer);
        baseCustomerService.save(customer);
        return Result.success();
    }

    @PostMapping("/update")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:customer:edit') or hasAuthority('ROLE_ADMIN')")
    public Result update(@RequestBody BaseCustomerUpdateDTO updateDTO) {
        BaseCustomer customer = new BaseCustomer();
        BeanUtils.copyProperties(updateDTO, customer);
        baseCustomerService.updateById(customer);
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('base:customer:delete') or hasAuthority('ROLE_ADMIN')")
    public Result delete(@PathVariable Long id) {
        baseCustomerService.removeById(id);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        BaseCustomer customer = baseCustomerService.getById(id);
        return Result.success(customer);
    }
}
