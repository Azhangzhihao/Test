package com.zzh.reggie.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzh.reggie.common.BaseContext;
import com.zzh.reggie.common.R;
import com.zzh.reggie.dto.DishDto;
import com.zzh.reggie.dto.SetmealDto;
import com.zzh.reggie.entity.Category;
import com.zzh.reggie.entity.Dish;
import com.zzh.reggie.entity.DishFlavor;
import com.zzh.reggie.service.CategoryService;
import com.zzh.reggie.service.DishFlavorService;
import com.zzh.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
    log.info(dishDto.toString());
    dishService.saveWithFlavor(dishDto);
    String key="dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
    redisTemplate.delete(key);
    return R.success("添加成功");
    }
    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize,String name){
        Page<Dish> p=new Page<>(page,pageSize);
        Page<DishDto> dtoPage=new Page<>();
        LambdaQueryWrapper<Dish> qw=new LambdaQueryWrapper<>();
        qw.like(name!=null,Dish::getName,name);
        qw.orderByDesc(Dish::getUpdateTime);
        dishService.page(p,qw);
        //对象拷贝
        BeanUtils.copyProperties(p,dtoPage,"records");
        List<Dish> records = p.getRecords();
        //添加菜品分类
         List<DishDto> list= records.stream().map((item)->{
            DishDto dto=new DishDto();
            BeanUtils.copyProperties(item,dto);
            Long categoryId = item.getCategoryId();
            Category byId = categoryService.getById(categoryId);
            if(byId!=null){
                String categoryName = byId.getName();
                dto.setCategoryName(categoryName);
            }
            return dto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }
    /**
     *根据id查询菜品信息和对应口味信息
     * @author: zzh
     * @date: 2022/6/30 15:51
     **/
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return  R.success(byIdWithFlavor);
    }

    @PutMapping
    public R<String> insert(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        String key="dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("修改成功");
    }
@PostMapping("/status/{type}")
    public R<String> isStop(@PathVariable Integer type,@RequestParam List<Long> ids){
    List<Dish> dishes = dishService.listByIds(ids);
    List<Dish> collect = dishes.stream().map(dish -> {
        dish.setStatus(type);
        return dish;
    }).collect(Collectors.toList());

    // BaseContext.setCurrentId();
        dishService.updateBatchById(collect);
//        String key="dish_"+d.getCategoryId()+"_1";
//        redisTemplate.delete(key);
        return R.success("修改成功");
}
    @DeleteMapping()
    public R<String> del(@RequestParam Long ids ){
        Dish dish = dishService.getById(ids);
       dishService.removeById(ids);
        String key="dish_"+dish.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("删除成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> getDish(Dish dish){
        List<DishDto> dishDtoList=null;

        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //先从redis中获取缓存数据
        String strDishDto = redisTemplate.opsForValue().get(key);
        if (strDishDto!=null){
            String Json = JSONUtil.toJsonStr(strDishDto);
            dishDtoList= JSON.parseArray(Json, DishDto.class);
        }
        //如果存在，直接返回，无需查询数据库
        if(dishDtoList!=null){
            return R.success(dishDtoList);
        }

       // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        LambdaQueryWrapper<Dish> qw=new LambdaQueryWrapper<>();
        qw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        qw.orderByAsc(Dish::getPrice).orderByDesc(Dish::getUpdateTime);
        qw.eq(Dish::getStatus,1);
        List<Dish> list = dishService.list(qw);
        dishDtoList= list.stream().map((item)->{
            DishDto dto=new DishDto();
            BeanUtils.copyProperties(item,dto);
            //分类id
           Long categoryId = item.getCategoryId();
           //根据id查询分类对象
           Category category = categoryService.getById(categoryId);
           if(category!=null){
               String name = category.getName();
               dto.setCategoryName(name);
           }
           Long id = item.getId();
           LambdaQueryWrapper<DishFlavor> df=new LambdaQueryWrapper<>();
           df.eq(DishFlavor::getDishId,id);
           //口味集合
           List<DishFlavor> listFlavor = dishFlavorService.list(df);
           dto.setFlavors(listFlavor);
           return  dto;
        }).collect(Collectors.toList());
        //如果不存在，将数据库查到的数据缓存
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(dishDtoList),60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }


}
