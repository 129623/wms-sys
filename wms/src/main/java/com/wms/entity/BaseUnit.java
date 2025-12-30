package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("base_unit")
public class BaseUnit implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "unit_id", type = IdType.AUTO)
    private Long unitId;

    private String unitCode;

    private String unitName;

    private Date createTime;
}
