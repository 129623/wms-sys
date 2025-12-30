package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("base_zone")
public class BaseZone implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "zone_id", type = IdType.AUTO)
    private Long zoneId;

    private Long warehouseId;

    private String zoneCode;

    private String zoneName;

    private String zoneType;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
