package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

public interface ReportService {

    /**
     * 统计指定时间区间端营业额数据
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnOverStatistics(LocalDate begin, LocalDate end);
}
