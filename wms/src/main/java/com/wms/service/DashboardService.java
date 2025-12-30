package com.wms.service;

import java.util.Map;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     * 根据当前用户权限返回不同的统计信息
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取库存信息
     */
    Map<String, Object> getInventoryInfo();

    /**
     * 获取最近操作动态
     */
    Map<String, Object> getRecentActivities();
}
