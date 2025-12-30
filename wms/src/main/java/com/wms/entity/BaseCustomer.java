package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户/供应商表
 */
@Data
@TableName("base_customer")
public class BaseCustomer {

    @TableId(value = "customer_id", type = IdType.AUTO)
    private Long customerId;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 类型 (1:客户/收货方, 2:供应商/发货方, 3:两者皆是)
     */
    private Integer customerType;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 城市
     */
    private String city;

    /**
     * 状态 (1:可用, 0:不可用)
     */
    private Integer status;

    /**
     * 删除标志 (0:存在, 1:删除)
     */
    @TableLogic
    private String delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
