package com.zzh.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzh.reggie.common.R;
import com.zzh.reggie.entity.Orders;
import com.zzh.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    //用户下单
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }
    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize,String number){
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> qw=new LambdaQueryWrapper<>();
        qw.like(number!=null,Orders::getNumber,number);
        qw.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage,qw);
        return R.success(ordersPage);
    }
    @GetMapping("/userPage")
    public R<Page> userPage(Integer page,Integer pageSize){
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> qw=new LambdaQueryWrapper<>();
        qw.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage,qw);
        return R.success(ordersPage);
    }

    @PutMapping
    public R<String> put(@RequestBody Map map){
        String status = map.get("status").toString();
        String id =map.get("id").toString();
        Orders byId = orderService.getById(id);
        byId.setStatus(Integer.valueOf(status));
        orderService.updateById(byId);
        return R.success("派送服务");
    }
    //再来一单
    @PostMapping("/again")
    public R again(){
        return R.success("返回首页");
    }

}
