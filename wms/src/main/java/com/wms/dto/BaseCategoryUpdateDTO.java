package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BaseCategoryUpdateDTO implements Serializable {
    private Long categoryId;
    private Long parentId;
    private String categoryName;
    private Integer level;
}
