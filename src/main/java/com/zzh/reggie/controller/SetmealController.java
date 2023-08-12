package com.zzh.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzh.reggie.common.R;
import com.zzh.reggie.dto.DishDto;
import com.zzh.reggie.dto.SetmealDto;
import com.zzh.reggie.entity.Category;
import com.zzh.reggie.entity.Dish;
import com.zzh.reggie.entity.Setmeal;
import com.zzh.reggie.entity.SetmealDish;
import com.zzh.reggie.service.CategoryService;
import com.zzh.reggie.service.SetmealDishService;
import com.zzh.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R<Page> selectAll(Integer page,Integer pageSize,String name){
        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage=new Page<>();

        LambdaQueryWrapper<Setmeal> qw=new LambdaQueryWrapper<>();
        //根据套餐名称搜索并排序
        qw.like(name!=null, Setmeal::getName,name);
        qw.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,qw);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            //将套餐实体的属性拷贝给Dto
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> insert(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("添加成功");
    }
    //批量删除
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> del(@RequestParam  List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }
    //批量修改套餐状态或单次修改
    @PostMapping("/status/{type}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> stop(@PathVariable Integer type,@RequestParam List<Long> ids){
        List<Setmeal> setmeals = setmealService.listByIds(ids);
        for(Setmeal s:setmeals){
            s.setStatus(type);
        }
        setmealService.updateBatchById(setmeals);
        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<SetmealDto> update(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.get(id);
        return R.success(setmealDto);
    }
    /*
     * @description:根据条件查询套餐数据
     * @author: zzh
     * @date: 2022/7/1 15:21
     * @param: setmeal
     * @return: com.zzh.reggie.common.R<java.util.List<com.zzh.reggie.entity.Setmeal>>
     **/

    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> qw=new LambdaQueryWrapper<>();
        qw.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        qw.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        qw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(qw);
        return R.success(list);
    }

    //前台点击套餐图片查看信息
    @GetMapping("/dish/{id}")
    public R<List<SetmealDish>> check(@PathVariable Long id){
        LambdaQueryWrapper<SetmealDish> qw=new LambdaQueryWrapper<>();
        qw.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(qw);
        return R.success(list);
    }
//TODO 修改SetmealDao数据
//    @PutMapping
//    public R<String> update(@RequestBody SetmealDto setmealDto) {
//        setmealService.updateById(setmealDto);
//        return R.success("修改成功");
//    }

}

