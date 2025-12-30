package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_outbound_order")
public class WmsOutboundOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "outbound_id", type = IdType.AUTO)
    private Long outboundId;

    private String orderNo;

    private Long warehouseId;

    private Integer orderType;

    private Long customerId;

    private Integer status;

    /**
     * 删除标志 (0:存在, 1:删除)
     */
    @com.baomidou.mybatisplus.annotation.TableLogic
    private String delFlag;

    private String receiverName;

    private String receiverPhone;

    private String address;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
