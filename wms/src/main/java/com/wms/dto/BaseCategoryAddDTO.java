package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseCategoryAddDTO implements Serializable {
    private Long parentId;
    private String categoryName;
    private Integer level;
}
