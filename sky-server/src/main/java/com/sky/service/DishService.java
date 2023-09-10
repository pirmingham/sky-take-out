package com.sky.service;


import com.sky.dto.DishDTO;

public interface DishService {
    /**
     * 新增菜品和口味数据
     * @param dishDTO
     */
    public void saveWidthFlavor(DishDTO dishDTO);
}
