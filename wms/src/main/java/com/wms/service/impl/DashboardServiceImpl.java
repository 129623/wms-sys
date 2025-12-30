package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.entity.WmsInboundOrder;
import com.wms.entity.WmsOutboundOrder;
import com.wms.entity.WmsInventory;
import com.wms.entity.BaseProduct;
import com.wms.mapper.WmsInboundOrderMapper;
import com.wms.mapper.WmsOutboundOrderMapper;
import com.wms.mapper.WmsInventoryMapper;
import com.wms.mapper.BaseProductMapper;
import com.wms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private WmsInboundOrderMapper inboundOrderMapper;

    @Autowired
    private WmsOutboundOrderMapper outboundOrderMapper;

    @Autowired
    private WmsInventoryMapper inventoryMapper;

    @Autowired
    private BaseProductMapper productMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取当前登录用户的权限
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> permissions = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 基础统计（所有用户可见）
        Map<String, Object> basicStats = new HashMap<>();

        // 待入库任务数
        if (hasPermission(permissions, "wms:inbound:list")) {
            Long pendingInbound = inboundOrderMapper.selectCount(
                    new QueryWrapper<WmsInboundOrder>().eq("status", 0));
            basicStats.put("pendingInbound", pendingInbound != null ? pendingInbound : 0);
        } else {
            basicStats.put("pendingInbound", 0);
        }

        // 待出库任务数
        if (hasPermission(permissions, "wms:outbound:list")) {
            Long pendingOutbound = outboundOrderMapper.selectCount(
                    new QueryWrapper<WmsOutboundOrder>().eq("status", 0));
            basicStats.put("pendingOutbound", pendingOutbound != null ? pendingOutbound : 0);
        } else {
            basicStats.put("pendingOutbound", 0);
        }

        stats.put("basicStats", basicStats);

        // 管理员额外统计
        if (hasPermission(permissions, "ROLE_ADMIN")) {
            Map<String, Object> adminStats = new HashMap<>();

            // 总库存数量
            Long totalInventory = inventoryMapper.selectCount(null);
            adminStats.put("totalInventory", totalInventory != null ? totalInventory : 0);

            // 总产品种类
            Long totalProducts = productMapper.selectCount(null);
            adminStats.put("totalProducts", totalProducts != null ? totalProducts : 0);

            // 今日入库单数
            Long todayInbound = inboundOrderMapper.selectCount(
                    new QueryWrapper<WmsInboundOrder>()
                            .ge("create_time", getTodayStart()));
            adminStats.put("todayInbound", todayInbound != null ? todayInbound : 0);

            // 今日出库单数
            Long todayOutbound = outboundOrderMapper.selectCount(
                    new QueryWrapper<WmsOutboundOrder>()
                            .ge("create_time", getTodayStart()));
            adminStats.put("todayOutbound", todayOutbound != null ? todayOutbound : 0);

            stats.put("adminStats", adminStats);
        }

        stats.put("permissions", permissions);
        return stats;
    }

    @Override
    public Map<String, Object> getInventoryInfo() {
        Map<String, Object> result = new HashMap<>();

        // 获取库存充足的产品（前5个）
        List<Map<String, Object>> topInventory = inventoryMapper.selectMaps(
                new QueryWrapper<WmsInventory>()
                        .orderByDesc("total_qty")
                        .last("LIMIT 5"));

        result.put("topInventory", topInventory);

        // 真实库区容量统计（按体积）
        List<Map<String, Object>> zoneCapacity = new ArrayList<>();

        // 查询所有库区及其库位容量统计
        String sql = "SELECT " +
                "z.zone_id, z.zone_name, " +
                "COALESCE(SUM(l.max_volume), 0) as total_volume, " +
                "COALESCE(SUM(CASE WHEN i.inventory_id IS NOT NULL THEN l.max_volume ELSE 0 END), 0) as used_volume " +
                "FROM base_zone z " +
                "LEFT JOIN base_location l ON z.zone_id = l.zone_id " +
                "LEFT JOIN wms_inventory i ON l.location_id = i.location_id " +
                "WHERE z.del_flag = '0' " +
                "GROUP BY z.zone_id, z.zone_name " +
                "ORDER BY z.zone_id " +
                "LIMIT 5";

        try {
            // 使用原生SQL查询
            List<Map<String, Object>> zones = inventoryMapper.selectMaps(
                    new QueryWrapper<WmsInventory>().apply(sql));

            for (Map<String, Object> zone : zones) {
                Map<String, Object> zoneData = new HashMap<>();
                String zoneName = (String) zone.get("zone_name");

                // 将体积转换为整数（四舍五入）
                Double totalVolumeDouble = getDoubleValue(zone.get("total_volume"));
                Double usedVolumeDouble = getDoubleValue(zone.get("used_volume"));

                Long totalVolume = Math.round(totalVolumeDouble);
                Long usedVolume = Math.round(usedVolumeDouble);

                int percentage = totalVolume > 0 ? (int) ((usedVolume * 100) / totalVolume) : 0;

                zoneData.put("zoneName", zoneName);
                zoneData.put("current", usedVolume);
                zoneData.put("total", totalVolume);
                zoneData.put("percentage", percentage);

                if (totalVolume > 0) { // 只显示有容量的库区
                    zoneCapacity.add(zoneData);
                }
            }
        } catch (Exception e) {
            // 如果查询失败，使用默认数据
            Map<String, Object> defaultZone = new HashMap<>();
            defaultZone.put("zoneName", "暂无数据");
            defaultZone.put("current", 0);
            defaultZone.put("total", 0);
            defaultZone.put("percentage", 0);
            zoneCapacity.add(defaultZone);
        }

        result.put("zoneCapacity", zoneCapacity);

        return result;
    }

    @Override
    public Map<String, Object> getRecentActivities() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> activities = new ArrayList<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> permissions = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 最近入库单
        if (hasPermission(permissions, "wms:inbound:list")) {
            List<WmsInboundOrder> recentInbound = inboundOrderMapper.selectList(
                    new QueryWrapper<WmsInboundOrder>()
                            .orderByDesc("create_time")
                            .last("LIMIT 3"));

            for (WmsInboundOrder order : recentInbound) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "inbound");
                activity.put("title", "入库单 #" + order.getOrderNo());
                activity.put("description", "待处理");
                activity.put("time", order.getCreateTime());
                activities.add(activity);
            }
        }

        // 最近出库单
        if (hasPermission(permissions, "wms:outbound:list")) {
            List<WmsOutboundOrder> recentOutbound = outboundOrderMapper.selectList(
                    new QueryWrapper<WmsOutboundOrder>()
                            .orderByDesc("create_time")
                            .last("LIMIT 3"));

            for (WmsOutboundOrder order : recentOutbound) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "outbound");
                activity.put("title", "出库单 #" + order.getOrderNo());
                activity.put("description", "待处理");
                activity.put("time", order.getCreateTime());
                activities.add(activity);
            }
        }

        // 按时间排序
        activities.sort((a, b) -> {
            Date timeA = (Date) a.get("time");
            Date timeB = (Date) b.get("time");
            return timeB.compareTo(timeA);
        });

        // 只保留最近5条
        if (activities.size() > 5) {
            activities = activities.subList(0, 5);
        }

        result.put("activities", activities);
        return result;
    }

    /**
     * 检查是否有指定权限
     */
    private boolean hasPermission(Set<String> permissions, String permission) {
        return permissions.contains(permission);
    }

    /**
     * 获取今天的开始时间
     */
    private String getTodayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
    }

    /**
     * 安全地将Object转换为Long类型
     */
    private Long getLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    /**
     * 安全地将Object转换为Double类型
     */
    private Double getDoubleValue(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
