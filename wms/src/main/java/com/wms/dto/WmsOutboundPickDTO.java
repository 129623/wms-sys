package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class WmsOutboundPickDTO implements Serializable {
    private Long outboundId;
    private Long detailId;
    private Long inventoryId; // Inventory to deduct from
    private Integer qty; // Quantity picked
}
