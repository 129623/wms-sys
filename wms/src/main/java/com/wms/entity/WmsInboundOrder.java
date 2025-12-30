package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_inbound_order")
public class WmsInboundOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "inbound_id", type = IdType.AUTO)
    private Long inboundId;

    private String orderNo;

    private Long warehouseId;

    private Long customerId;

    private Integer orderType; // 1:采购入库...

    private Integer status; // 0:已创建...

    private String remark;

    /**
     * 删除标志 (0:存在, 1:删除)
     */
    @com.baomidou.mybatisplus.annotation.TableLogic
    private String delFlag;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
