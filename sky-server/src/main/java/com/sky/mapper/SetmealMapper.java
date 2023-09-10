package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.CommonFieldsAutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 添加套餐
     *
     * @param setmeal
     */
    @CommonFieldsAutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据ID查询套餐
     *
     * @param id
     * @return
     */
    @Select("select * from setmeal where id =#{id}")
    Setmeal selectById(Long id);

    /**
     * 根据ID删除套餐
     *
     * @param id
     */
    @Delete("delete from setmeal where id=#{id}")
    void deleteById(Long id);

    /**
     * 更新套餐信息
     * @param setmeal
     */
    @CommonFieldsAutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);
}
