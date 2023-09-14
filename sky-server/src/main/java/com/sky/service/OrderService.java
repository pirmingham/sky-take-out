package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 用户端历史订单查询
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQueryForUser(Integer pageNum, Integer pageSize, Integer status);


    /**
     * 根据订单ID查询订单详情
     *
     * @param orderId
     * @return
     */
    OrderVO details(Long orderId);

    /**
     * 用户取消订单
     *
     * @param orderId
     */
    void userCancelById(Long orderId) throws Exception;

    /**
     * 再来一单
     *
     * @param orderId
     */
    void repetition(Long orderId);

    /**
     * 管理端-订单条件查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 管理端-统计订单数据
     *
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);
}