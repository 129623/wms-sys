package com.wms.controller;

import com.wms.common.Result;
import com.wms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     * 根据用户权限返回不同的数据
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return Result.success(stats);
    }

    /**
     * 获取最新库存信息
     */
    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('wms:inventory:list' or hasAuthority('ROLE_ADMIN'))")
    public Result<Map<String, Object>> getInventoryInfo() {
        Map<String, Object> inventory = dashboardService.getInventoryInfo();
        return Result.success(inventory);
    }

    /**
     * 获取最近操作动态
     */
    @GetMapping("/activities")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getRecentActivities() {
        Map<String, Object> activities = dashboardService.getRecentActivities();
        return Result.success(activities);
    }

    /**
     * 获取系统通知（库存预警等）
     */
    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public Result<java.util.List<Map<String, Object>>> getNotifications() {
        java.util.List<Map<String, Object>> notifications = dashboardService.getNotifications();
        return Result.success(notifications);
    }
}
