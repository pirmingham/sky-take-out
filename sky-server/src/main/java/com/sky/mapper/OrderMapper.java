package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id=#{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{order_id}")
    Orders getById(Long orderId);

    @Select("select count(id) from orders where status=#{status}")
    Integer countByStatus(Integer status);

    @Select("select * from orders where status=#{status} and order_time< #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLessThan(Integer status, LocalDateTime orderTime);

//    @Select("select sum(amount) from orders where status=#{status} and order_time>#{begin} and order_tim<#{end}")
    Double sumByMap(Map map);

    Integer countByMap(Map map);

    /**
     * 统计指定时间内的Top10数据
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin,LocalDateTime end);
}
