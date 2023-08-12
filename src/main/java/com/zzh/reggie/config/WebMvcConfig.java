package com.zzh.reggie.config;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.zzh.reggie.common.BaseContext;
import com.zzh.reggie.common.CustomException;
import com.zzh.reggie.common.JacksonObjectMapper;
import com.zzh.reggie.entity.Employee;
import com.zzh.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        //访问/backend/**的所有请求都去classpath:/backend/下面进行匹配
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }
//扩展mvc框架的消息转换器
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用jackson将java对象转化为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0,messageConverter);
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String requestURI = request.getRequestURI();
                System.out.println("~~~~~~~~~~~~"+requestURI);
                String token = request.getHeader("token");
                String frontToken = request.getHeader("frontToken");
                System.out.println("~~~~~~token       " + token);
                System.out.println("~~~~~~frontToken         " + frontToken);
                //如果不是映射方法直接通过
                if (!(handler instanceof HandlerMethod)) {
                    return true;
                }
                if (StrUtil.isBlank(token) && StrUtil.isBlank(frontToken)) {
                    throw new CustomException("无token，请重新登陆");
                }
                String userid;
                try {
                    if(token!=null){
                        userid = JWT.decode(token).getAudience().get(0);
                    }else {
                        userid = JWT.decode(frontToken).getAudience().get(0);
                    }
                } catch (JWTDecodeException j) {
                    throw new CustomException("token验证失败");
                }
//                Employee user = employeeService.getById(userid);
//                if (user == null) {
//                    throw new CustomException("用户不存在，请重新登陆");
//                }
                //用户密码加签验证token
//                JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
//                try {
//                    jwtVerifier.verify(token);
//                } catch (JWTDecodeException j) {
//                    throw new CustomException("token验证失败，重新登录");
//                }
                //把用户id放入ThreadLocal
                BaseContext.setCurrentId(Long.valueOf(userid));
                return true;
            }
        }).excludePathPatterns(
            "/employee/login",
             "/employee/logout",
                "/backend/**",
                "/error",
                "/common/upload",
                "/common/download",
               "/user/loginout",
                "/user/login",
                "/front/**",
              "/user/sendMsg",
                "/shoppingCart/**"
        );

    }

}
