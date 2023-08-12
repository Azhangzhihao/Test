package com.zzh.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzh.reggie.common.R;
import com.zzh.reggie.entity.Category;
import com.zzh.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody Category category){
    categoryService.save(category);
    return  R.success("新增分类成功");
}
    @GetMapping("/page")
    public R<Page<Category> > select(Integer page, Integer pageSize) {
        log.info("page={},pageSize={}",page,pageSize);
        Page<Category>  pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(Category::getSort);
        categoryService.page(pageInfo,qw);
        return R.success(pageInfo);
    }
@DeleteMapping
    public R<String> del(Long ids){
        categoryService.remove(ids);
        return R.success("删除成功");
}
@PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改成功");
}
@GetMapping("/list")
    public R<List<Category>> getList(Category category){
        LambdaQueryWrapper<Category> qw=new LambdaQueryWrapper<>();
        qw.eq(category.getType()!=null,Category::getType,category.getType());
        qw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(qw);
        return R.success(list);
}

}
