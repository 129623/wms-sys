package com.wms.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class WmsInboundReceiptDTO implements Serializable {
    private Long inboundId;
    private Long detailId;
    private Long locationId; // The location to store the goods
    private Integer qty; // Quantity received this time
    private String description; // Optional
}
