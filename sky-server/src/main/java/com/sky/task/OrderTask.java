package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    /**
     * 处理超时订单方法
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    public void processTimeoutOrders() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        //select * from orders where status=待付款 and order_time<(当前时间-15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLessThan(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理一直处于派送中状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天早上凌晨处理昨天派送中未完成的订单
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().minusHours(1);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLessThan(Orders.DELIVERY_IN_PROGRESS, time);
        for (Orders orders : ordersList) {
            orders.setStatus(Orders.COMPLETED);
            orderMapper.update(orders);
        }
    }
}
