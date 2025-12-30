package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String username;

    @JsonIgnore // 不返回给前端
    private String password;

    private String realName;

    private String email;

    private String phone;

    private Integer status; // 1:正常, 0:停用

    @TableLogic(value = "0", delval = "1")
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    // 非数据库字段，用于前端显示角色名称
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String roleName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Long roleId;
}
