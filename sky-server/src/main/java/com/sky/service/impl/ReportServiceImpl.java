package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间区间端营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnOverStatistics(LocalDate begin, LocalDate end) {

        //当前集合用于存放从begin开始到end结束的范围内，每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        dateList.add(end);
        //计算出日期
        List<Double> turnoverList = new ArrayList<>();
        String dateListStr = StringUtils.join(dateList, ",");
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额是指：状态为"已完成"的订单金额合计；
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amount) from order where order_time > ? and order_time < ? and status = 5;
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnoverPerDday = orderMapper.sumByMap(map);
            //如果营业额为0时，这里会返回空
            turnoverPerDday = turnoverPerDday == null ? 0.0 : turnoverPerDday;
            turnoverList.add(turnoverPerDday);
        }
        String turnoverListStr = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO.builder().dateList(dateListStr).turnoverList(turnoverListStr).build();
    }

    /**
     * 统计用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin开始到end结束的范围内，每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        dateList.add(end);
        String dateListStr = StringUtils.join(dateList, ",");
        //统计每天的新增用户数量 select count(id) from user create_time<? and create_time>?
        List<Integer> newUserList = new ArrayList<>();
        //统计每天的总用户数量 select count(id) from user create_time<?
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate localDate : dateList) {
            //统计每天的总用户数量
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUserPerDay = userMapper.countByMap(map);
            totalUserList.add(totalUserPerDay);
            //统计每天新增的总用户数量
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            map.put("begin", beginTime);
            Integer newUserPerDay = userMapper.countByMap(map);
            newUserList.add(newUserPerDay);
        }
        return UserReportVO.builder()
                .dateList(dateListStr)
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 统计指定时间区间订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin开始到end结束的范围内，每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        dateList.add(end);
        String dateListStr = StringUtils.join(dateList, ",");
        //存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询每天的订单总数 select count(id) from orders where order_time >? and order_time<?
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer orderCountPerDay = orderMapper.countByMap(map);
            orderCountList.add(orderCountPerDay);
            //查询每天的有效订单总数 select count(id) from orders where order_time >? and order_time<? and status=5
            map.put("status", Orders.COMPLETED);
            Integer validOrderCountPerDay = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCountPerDay);
        }
        //计算时间区间的订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算时间区间的有效订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(dateListStr)
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCount(validOrderCount)
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

}
