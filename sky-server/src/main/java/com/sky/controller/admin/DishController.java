package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Api(tags = "菜品管理")
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping()
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.saveWidthFlavor(dishDTO);
        String key = "dish_" + dishDTO.getCategoryId();
        clearRedisCache(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result page(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除:{}", ids);

        dishService.deleteBatch(ids);
        clearRedisCache("dish_*");
        return Result.success();
    }


    /**
     * 根据ID查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据ID查询菜品:{}", id);
        DishVO dishVO = dishService.getByIdWidthFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品:{}", dishDTO);
        dishService.updateWidthFlavor(dishDTO);
        clearRedisCache("dish_*");
        return Result.success();
    }

    /**
     * 根据分类Id获取菜品列表
     */
    @GetMapping("/list")
    @ApiOperation("根据分类Id获取菜品列表")
    public Result list(Long categoryId) {
        log.info("根据分类Id获取菜品列表：{}", categoryId);
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }

    @PostMapping("/status/{status}")
    public Result enableOrDisable(@PathVariable Integer status, Long id) {
        dishService.enableOrDisable(status, id);
        Long categoryId = dishService.getCategoryIdById(id);
        String key = "dish_" + categoryId;
        clearRedisCache(key);
        return Result.success();
    }

    private void clearRedisCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
