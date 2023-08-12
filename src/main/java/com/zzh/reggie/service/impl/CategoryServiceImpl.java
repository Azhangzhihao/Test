package com.zzh.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzh.reggie.common.CustomException;
import com.zzh.reggie.entity.Category;
import com.zzh.reggie.entity.Dish;
import com.zzh.reggie.entity.Setmeal;
import com.zzh.reggie.mapper.CategoryMapper;
import com.zzh.reggie.service.CategoryService;
import com.zzh.reggie.service.DishService;
import com.zzh.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /*
 * @description:根据id删除分类，删除之前需要进行判断
 * @author: zzh
 * @date: 2022/6/29 16:16
 * @param: id
 * @return: null
 **/
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> qw=new LambdaQueryWrapper<>();
        qw.eq(Dish::getCategoryId,id);
        int count = dishService.count(qw);
        if(count>0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        // 已经关联，抛出一个业务异常
        }
        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> qw1=new LambdaQueryWrapper<>();
        qw1.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(qw1);
        if(count1>0){
            // 已经关联，抛出一个业务异常
        throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        //正常删除
        super.removeById(id);
    }
}
