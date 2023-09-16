package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

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
        String dateListStr = getDateListStr(begin, end, dateList);
        //计算出日期
        List<Double> turnoverList = new ArrayList<>();
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
        String dateListStr = getDateListStr(begin, end, dateList);
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
        String dateListStr = getDateListStr(begin, end, dateList);
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

    /**
     * 获取日期字符串
     *
     * @param begin
     * @param end
     * @param dateList
     * @return
     */
    private String getDateListStr(LocalDate begin, LocalDate end, List<LocalDate> dateList) {
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        dateList.add(end);
        return StringUtils.join(dateList, ",");
    }

    /**
     * 统计指定时间区间销量排名Top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> nameList = goodsSalesDTOList.stream().map((goodsSalesDTO -> {
            String name = goodsSalesDTO.getName();
            return name;
        })).collect(Collectors.toList());

        List<Integer> numberList = goodsSalesDTOList.stream().map(goodsSalesDTO -> {
            return goodsSalesDTO.getNumber();
        }).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出运营数据报表数据
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {

        //1. 查询数据库，获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);

        //2. 通过POI将数据写入excel文件中
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于模版文件创建一个新的excel文件
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //获取表格标签页
            XSSFSheet sheet = excel.getSheetAt(0);
            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);
            //获取第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            //或得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //获取某一天的数据
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getTurnover());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }


            //3. 通过输出流将excel文件下载到浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
