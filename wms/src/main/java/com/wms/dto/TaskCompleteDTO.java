package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class TaskCompleteDTO implements Serializable {
    private Long taskId;
    private Long locationId; // Confirm actual location
    private String operator;
    private String remark;
}
