package com.zzh.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzh.reggie.common.BaseContext;
import com.zzh.reggie.common.R;
import com.zzh.reggie.entity.ShoppingCart;
import com.zzh.reggie.service.ShoppingCartService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    /*
     * @description:添加购物车
     * @author: zzh
     * @date: 2022/7/1 16:45
     * @param: shoppingCart
     * @return: com.zzh.reggie.common.R<com.zzh.reggie.entity.ShoppingCart>
     **/
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> qw=new LambdaQueryWrapper<>();
        //设置用户id，指定当前是那个用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        qw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        if(dishId!=null){
            //添加到购物车的是菜品
            qw.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            qw.eq(ShoppingCart::getSetmealId,setmealId);
        }
        //SQL：select * from shopping_cart where user_id=? and dish_id=? /setmeal_id=?
        // 查询当前菜品或套餐是否在购物车中
        ShoppingCart one = shoppingCartService.getOne(qw);
        //如果已经存在，就在原来的数量基础上加一
        if(one!=null){
            Integer number = one.getNumber();
            one.setNumber(number+1);
            shoppingCartService.updateById(one);
        }else {
            //如果不存在，则添加到购物车，数量默认就为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one=shoppingCart;
        }
        return R.success(one);
    }
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> qw=new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        qw.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(qw);
        return R.success(list);
    }

    //清空购物车
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> qw=new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(qw);
        return R.success("清空成功");
    }
        //数量减一
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> qw=new LambdaQueryWrapper<>();

        qw.eq(shoppingCart.getDishId() != null,ShoppingCart::getDishId,shoppingCart.getDishId());
        qw.eq(shoppingCart.getSetmealId() != null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        qw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        ShoppingCart one = shoppingCartService.getOne(qw);
        Integer number = one.getNumber();
        if(number==1){
            shoppingCartService.removeById(one);
        }
            one.setNumber(number-1);
            shoppingCartService.updateById(one);
        return R.success(one);
    }
}
