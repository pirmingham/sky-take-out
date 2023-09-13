package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端-订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        //模拟交易成功，修改数据订单状态
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        log.info("模拟交易成功：{}", ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询用户订单列表
     *
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查询用户历史订单列表")
    public Result<PageResult> historyOrders(Integer page, Integer pageSize, Integer status) {
        log.info("查询用户历史订单列表：pageNum:{} pageSize:{} status: {}", page, pageSize, status);
        PageResult pageResult = orderService.pageQueryForUser(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 根据订单id获取订单详情
     *
     * @param orderId
     * @return
     */
    @GetMapping("/orderDetail/{orderId}")
    @ApiOperation("根据订单id获取订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long orderId) {
        log.info("根据订单id获取订单详情:{}", orderId);
        OrderVO orderVO = orderService.details(orderId);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     *
     * @param orderId
     * @return
     */
    @PutMapping("/cancel/{orderId}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long orderId) throws Exception {
        orderService.userCancelById(orderId);
        return Result.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{orderId}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long orderId) {
        orderService.repetition(orderId);
        return Result.success();
    }
}
