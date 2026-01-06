package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.entity.WmsInboundOrder;
import com.wms.entity.WmsOutboundOrder;
import com.wms.entity.WmsInventory;
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

    @Autowired
    private com.wms.mapper.BaseZoneMapper zoneMapper;

    @Autowired
    private com.wms.mapper.BaseLocationMapper locationMapper;

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

        // 获取库存充足的产品（前5个），关联产品名称
        // 方法：直接查询inventory并在Java层关联product
        List<WmsInventory> inventories = inventoryMapper.selectList(
                new QueryWrapper<WmsInventory>()
                        .eq("del_flag", "0")
                        .orderByDesc("total_qty")
                        .last("LIMIT 5"));

        List<Map<String, Object>> topInventory = new ArrayList<>();
        for (WmsInventory inv : inventories) {
            Map<String, Object> item = new HashMap<>();
            item.put("inventoryId", inv.getInventoryId());
            item.put("productId", inv.getProductId());
            item.put("totalQty", inv.getTotalQty());
            item.put("availableQty", inv.getAvailableQty());

            // 查询产品信息
            if (inv.getProductId() != null) {
                com.wms.entity.BaseProduct product = productMapper.selectById(inv.getProductId());
                if (product != null) {
                    item.put("productName", product.getProductName());
                    item.put("skuCode", product.getSkuCode());
                } else {
                    item.put("productName", "未知产品");
                    item.put("skuCode", "");
                }
            } else {
                item.put("productName", "未知产品");
                item.put("skuCode", "");
            }

            topInventory.add(item);
        }

        result.put("topInventory", topInventory);

        // 库区容量统计（按库位数量 - 方案1）
        List<Map<String, Object>> zoneCapacity = new ArrayList<>();

        try {
            // 查询所有未删除的库区
            List<com.wms.entity.BaseZone> zones = zoneMapper.selectList(
                    new QueryWrapper<com.wms.entity.BaseZone>()
                            .eq("del_flag", "0")
                            .orderByAsc("zone_id"));

            // 遍历各库区，统计库位使用情况
            for (com.wms.entity.BaseZone zone : zones) {
                // 统计该库区的总库位数
                Long totalLocations = locationMapper.selectCount(
                        new QueryWrapper<com.wms.entity.BaseLocation>()
                                .eq("zone_id", zone.getZoneId())
                                .eq("del_flag", "0"));

                // 只统计有库位的库区
                if (totalLocations > 0) {
                    // 统计该库区已占用的库位数（有库存的库位，需去重）
                    // 修正：使用 selectObjs 查询去重后的 location_id 数量
                    QueryWrapper<WmsInventory> queryWrapper = new QueryWrapper<>();
                    queryWrapper.select("DISTINCT location_id");
                    queryWrapper.inSql("location_id",
                            "SELECT location_id FROM base_location WHERE zone_id = " + zone.getZoneId()
                                    + " AND del_flag = '0'");
                    queryWrapper.gt("total_qty", 0);

                    List<Object> usedLocationIds = inventoryMapper.selectObjs(queryWrapper);
                    Long usedLocations = (long) usedLocationIds.size();

                    System.out.println(
                            "Zone: " + zone.getZoneName() + ", Total: " + totalLocations + ", Used: " + usedLocations);

                    Map<String, Object> zoneData = new HashMap<>();
                    zoneData.put("zoneName", zone.getZoneName());
                    zoneData.put("current", usedLocations);
                    zoneData.put("total", totalLocations);
                    int percentage = totalLocations > 0 ? (int) ((usedLocations * 100) / totalLocations) : 0;
                    zoneData.put("percentage", percentage);
                    zoneCapacity.add(zoneData);
                }
            }

            System.out.println("Total zones with capacity: " + zoneCapacity.size());

        } catch (Exception e) {
            // 如果查询失败，返回默认数据
            System.err.println("Failed to calculate zone capacity: " + e.getMessage());
            e.printStackTrace();
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

    @Override
    public List<Map<String, Object>> getNotifications() {
        List<Map<String, Object>> notifications = new ArrayList<>();

        // 1. 库存预警 (聚合查询：按产品分组，计算所有仓库/批次的总可用量)
        QueryWrapper<WmsInventory> invWrapper = new QueryWrapper<>();
        invWrapper.select("product_id", "SUM(available_qty) as total_qty");
        invWrapper.groupBy("product_id");
        invWrapper.having("SUM(available_qty) < {0}", 10);
        invWrapper.last("LIMIT 10");

        // 使用 selectMaps 执行聚合查询
        List<Map<String, Object>> lowStockList = inventoryMapper.selectMaps(invWrapper);

        if (lowStockList != null && !lowStockList.isEmpty()) {
            Set<Long> productIds = new HashSet<>();
            for (Map<String, Object> map : lowStockList) {
                // map的key通常与select中的别名一致，或者数据库字段名一致
                Object pid = map.get("product_id");
                if (pid != null) {
                    productIds.add(Long.valueOf(pid.toString()));
                }
            }

            // 批量查询产品名称
            Map<Long, String> productNames = new HashMap<>();
            if (!productIds.isEmpty()) {
                productMapper.selectBatchIds(productIds).forEach(p -> {
                    productNames.put(p.getProductId(), p.getProductName());
                });
            }

            for (Map<String, Object> map : lowStockList) {
                Long productId = Long.valueOf(map.get("product_id").toString());

                // 处理 SUM 结果可能的数值类型 (Long, BigDecimal, Double 等)
                Object totalQtyObj = map.get("total_qty");
                long totalAvailable = 0;
                if (totalQtyObj != null) {
                    if (totalQtyObj instanceof Number) {
                        totalAvailable = ((Number) totalQtyObj).longValue();
                    } else {
                        // 尝试字符串解析
                        try {
                            totalAvailable = Long.parseLong(totalQtyObj.toString().split("\\.")[0]);
                        } catch (Exception e) {
                        }
                    }
                }

                Map<String, Object> notif = new HashMap<>();
                notif.put("id", "stock-alert-" + productId);
                notif.put("type", "warning");
                notif.put("title", "总库存告急");
                String name = productNames.getOrDefault(productId, "未知产品");
                notif.put("content", "产品 [" + name + "] 全库总可用量仅剩 " + totalAvailable + " 件，请及时补货。");
                notif.put("time", new Date());
                notifications.add(notif);
            }
        }

        return notifications;
    }
}
