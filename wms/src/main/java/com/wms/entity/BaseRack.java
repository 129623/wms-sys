package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("base_rack")
public class BaseRack implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "rack_id", type = IdType.AUTO)
    private Long rackId;

    private Long zoneId;

    private Long warehouseId;

    private String rackCode;

    private String rackType;

    private Long relatedRackId;

    @TableField("del_flag")
    @com.baomidou.mybatisplus.annotation.TableLogic(value = "0", delval = "1")
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
