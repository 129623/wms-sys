package com.wms.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_inventory")
public class WmsInventory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "inventory_id", type = IdType.AUTO)
    private Long inventoryId;

    private Long warehouseId;

    private Long zoneId;

    private Long locationId;

    private Long productId;

    private String batchNo;

    private Integer totalQty;

    private Integer frozenQty;

    // availableQty is a generated column in the database (total_qty - frozen_qty)
    // It should not be updated via ORM, only read
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer availableQty;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
