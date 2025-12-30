package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("base_product")
public class BaseProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "product_id", type = IdType.AUTO)
    private Long productId;

    private Long categoryId;

    private String skuCode;

    private String productName;

    private String spec;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String unit;

    private Long unitId;

    private Long storageTypeId;

    private Long labelId;

    private BigDecimal weight;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;

    private Integer palletCapacity;

    private String imageUrl;

    private String origin;

    /**
     * 删除标志 (0:存在, 1:删除)
     */
    @com.baomidou.mybatisplus.annotation.TableLogic
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
