package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_inventory_history")
public class WmsInventoryHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    private Long warehouseId;

    private Long productId;

    private String batchNo;

    private Long locationId;

    private Integer changeType; // 1:入库, 2:出库, 3:移库, 4:盘盈, 5:盘亏

    private Integer changeQty;

    private Integer afterQty;

    private String orderNo;

    private String operator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateTime;
}
