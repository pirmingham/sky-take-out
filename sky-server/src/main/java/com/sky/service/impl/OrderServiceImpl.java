package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1处理各种业务异常 1.地址簿为空，2购物车数据为空

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            //抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //2向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        //TODO:可能需要更改
        orders.setAddress(addressBook.getDetail());
        orderMapper.insert(orders);
        //3向订单明细表插入多条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //4清空当前用户端购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //5封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 用户端历史订单查询
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQueryForUser(Integer pageNum, Integer pageSize, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        for (Orders orders : page.getResult()) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailListByOrderId(orders.getId());
            orderVO.setOrderDetailList(orderDetailList);
            list.add(orderVO);
        }
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 根据订单ID查询订单详情
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderVO details(Long orderId) {
        OrderVO orderVO = new OrderVO();
        Orders orders = orderMapper.getById(orderId);
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailListByOrderId(orderId);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param orderId
     */
    @Override
    public void userCancelById(Long orderId) throws Exception {
        Orders orderInDB = orderMapper.getById(orderId);
        if (orderInDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer status = orderInDB.getStatus();
        //订单状态：
        //1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        //如果超过 待接单，就不能取消了
        if (status > Orders.REFUND) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(orderInDB.getId());
        //如果是待接单状态，还需要退款；
        if (status.equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    orders.getNumber(), //商户订单号
//                    orders.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
            //模拟调用微信支付退款接口
            //支付状态修改为 退款
            //支付状态 0未支付 1已支付 2退款
            orders.setPayStatus(Orders.REFUND);
        }

        //更新订单状态、取消原因
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     *
     * @param orderId
     */
    @Override
    public void repetition(Long orderId) {
        //根据orderId查询
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailListByOrderId(orderId);

        if (orderDetailList == null || orderDetailList.size() == 0) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //获取用户Id
        Long userId = BaseContext.getCurrentId();
        LocalDateTime localDateTime = LocalDateTime.now();
        //将订单详情转变为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((orderDetail -> {
            ShoppingCart cart = new ShoppingCart();
            //将园订单详情里端裁判信息重新复制到购物车对象中
            BeanUtils.copyProperties(orderDetail, cart, "id");
            cart.setUserId(userId);
            cart.setCreateTime(localDateTime);
            return cart;
        })).collect(Collectors.toList());
        //批量插入shoppingCart
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 管理端-订单条件查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> ordersList = page.getResult();
        List<OrderVO> orderVOList = new ArrayList<>();
        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        if (!Collections.isEmpty(ordersList)) {
            orderVOList = ordersList.stream().map((orders -> {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                String orderDishesStr = getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishesStr);
                return orderVO;
            })).collect(Collectors.toList());
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 根据orders的订单Id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中端菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailListByOrderId(orders.getId());
        List<String> orderDishList = orderDetailList.stream().map((orderDetail -> {
            String orderDish = orderDetail.getName() + "*" + orderDetail.getAmount() + ";";
            return orderDish;
        })).collect(Collectors.toList());
        //将该订单所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 管理端-统计订单数据
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();

        Integer toBeConfirmedCount = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmedCount = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgressCount = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmedCount);
        orderStatisticsVO.setConfirmed(confirmedCount);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgressCount);
        return orderStatisticsVO;
    }
}
