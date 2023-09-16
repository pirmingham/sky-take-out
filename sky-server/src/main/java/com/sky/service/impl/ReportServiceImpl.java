package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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
}
