package com.sky.mapper;

import com.sky.entity.Orders;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     *
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);


    /**
     * 插入数据
     * @param user
     */
    void insert(User user);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Select("select * from user where id=#{userId}")
    User getById(Long userId);

    Integer countByMap(Map map);
}
