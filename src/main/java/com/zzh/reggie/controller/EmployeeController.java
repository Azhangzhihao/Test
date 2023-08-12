package com.zzh.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzh.reggie.common.BaseContext;
import com.zzh.reggie.common.R;
import com.zzh.reggie.entity.Employee;
import com.zzh.reggie.service.EmployeeService;
import com.zzh.reggie.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
//登录
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1、md5解密密码
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据用户名查询数据库
        LambdaQueryWrapper<Employee> QueryWrapper = new LambdaQueryWrapper<Employee>();
        QueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(QueryWrapper);
        if (emp == null) {
            return R.error("登录失败");
        }
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }
        //3登陆成功，将员工id存入session
        emp.setToken(TokenUtils.getToken(emp.getId().toString(),emp.getPassword()));//  request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
      //  request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
    //员工信息分页查询
    @GetMapping("/page")
    public R<Page> select(Integer page,Integer pageSize,String name) {
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        Page<Employee> pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        qw.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        qw.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo,qw);
        return R.success(pageInfo);
    }
    //添加员工信息
    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request) {
        log.info("新增员工，员工信息：{}",employee.toString());
        //123456默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //获取当前登录用户的id
      //  Long empId = (Long) request.getSession().getAttribute("employee");
       // employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }
    //启用禁用员工账户和修改员工信息
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request) {
        log.info(employee.toString());
    //    Long empId = (Long) request.getSession().getAttribute("employee");

        //employee.setUpdateUser(empId);
        //employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);
        return R.success("修改成功");
    }
    //修改员工信息
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查员工信息");
        Employee emp = employeeService.getById(id);
        if (emp!=null){
        return R.success(emp);
    }
        return R.error("未查询到对应员工信息");
}
}