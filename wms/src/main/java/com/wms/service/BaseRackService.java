package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.dto.BaseRackAddDTO;
import com.wms.dto.BaseRackUpdateDTO;
import com.wms.entity.BaseRack;

public interface BaseRackService extends IService<BaseRack> {
    boolean add(BaseRackAddDTO addDTO);

    boolean update(BaseRackUpdateDTO updateDTO);

    boolean delete(Long id); // Note: Rack usually doesn't have status field in table definition provided, so
                             // maybe physical delete or check if table has status?
    // Checking schema: base_rack table does NOT have 'status' column. Using
    // physical delete for now, or user might want to add status column.
    // User request: "necessary to modify database". I should probably add status
    // column to BaseRack to be consistent with logical delete requirement.
    // Let's first check if I should modify DB. The requirements image says
    // "Delete... set status to unavailable" for Warehouse and Location/Zone?
    // For Rack: "Create: ... Double rack requires setting position and related
    // rack".
    // Let's look at schema again. base_rack has no status.
    // I will add status column to base_rack to support logical delete consistent
    // with others.
}
