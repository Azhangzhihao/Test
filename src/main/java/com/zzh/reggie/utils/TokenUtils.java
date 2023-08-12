package com.zzh.reggie.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class TokenUtils {
    public static String getToken(String userId,String password){
        return JWT.create().withAudience(userId)//将userName保存token里，作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(),2))//两小时后失效
                .sign(Algorithm.HMAC256(password));//以password作为token密钥
    }
}