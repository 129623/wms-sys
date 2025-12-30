package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("base_location")
public class BaseLocation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "location_id", type = IdType.AUTO)
    private Long locationId;

    private Long rackId;
    private Long warehouseId;
    private Long zoneId;
    private String locationCode;
    private Integer rowNo;
    private Integer layerNo;
    private Integer status;
    private java.math.BigDecimal maxWeight;
    private java.math.BigDecimal maxVolume;

    /**
     * 删除标志 (0:存在, 1:删除)
     */
    @com.baomidou.mybatisplus.annotation.TableLogic
    private String delFlag;

    private Date createTime;
}
