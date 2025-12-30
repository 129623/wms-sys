package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_picking_task")
public class WmsPickingTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    private Long outboundId;

    private Long detailId;

    private Long productId;

    private Long locationId;

    private String batchNo;

    private Integer qty;

    private Integer status; // 0:待拣货, 1:已拣货

    private String operator;

    /**
     * 删除标志 (0:存在, 1:删除)
     */
    @com.baomidou.mybatisplus.annotation.TableLogic
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateTime;
}
