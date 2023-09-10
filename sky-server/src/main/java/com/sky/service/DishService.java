package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
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
}
