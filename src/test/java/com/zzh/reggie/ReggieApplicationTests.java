package com.zzh.reggie;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzh.reggie.entity.Dish;
import com.zzh.reggie.mapper.DishMapper;
import com.zzh.reggie.service.DishService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ReggieApplicationTests {
@Autowired
    DishMapper dishMapper;
@Autowired
    DishService dishService;
    @Test
    void contextLoad1() {
        QueryWrapper<Dish> qw = new QueryWrapper<>();
        //Dish dish = dishMapper.selectOne(qw.eq("id",1397849739276890114L));
        qw.inSql("price","select price from dish where price<1000");
        //子查询  SELECT id,name,category_id,price,code,image,description,status,sort,create_time,update_time,create_user,update_user FROM dish WHERE (price IN (select price from dish where price<1000))
       // List<Dish> dishes = dishService.list(qw);
        List<Dish> dishes = dishMapper.selectList(qw);
        dishes.forEach(System.out::println);
        //System.out.println(dishes);
    }
    @Test
    void contextLoad2() {
        QueryWrapper<Dish> qw = new QueryWrapper<>();
     //   List<Dish> dish= dishService.listByIds(Arrays.asList(1397849739276890114L));
        Dish dish = dishService.getById(1397849739276890114L);
        System.out.println(dish);
    }

}
