package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("wms_outbound_detail")
public class WmsOutboundDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "detail_id", type = IdType.AUTO)
    private Long detailId;

    private Long outboundId;

    private Long productId;

    private Integer planQty;

    private Integer pickedQty;

    /**
     * 删除标志 (0:存在, 1:删除)
     */
    @com.baomidou.mybatisplus.annotation.TableLogic
    private String delFlag;
}
