package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和口味数据
     *
     * @param dishDTO
     */
    public void saveWidthFlavor(DishDTO dishDTO);

    /**
     * 分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除
     *
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据ID获得菜品与口味信息
     *
     * @param id
     * @return
     */
    DishVO getByIdWidthFlavor(Long id);


    /**
     * 修改菜品及口味信息
     *
     * @param dishDTO
     */
    void updateWidthFlavor(DishDTO dishDTO);

    /**
     * 根据分类Id查找到菜品列表
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 启用或停用某个菜品
     * @param status
     * @param id
     */
    void enableOrDisable(Integer status, Long id);


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
