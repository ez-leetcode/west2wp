package com.west2wp.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;

//token工具类,redis出问题了,暂时只有createToken用得到
@Slf4j
@Service
public class JWTUtils {

    //加密secret
    private static final String jwtSecret = "!@#ycy*&^yyds!!";

    //过期时间
    private static final Integer TimeOutHours = 12;

    @Resource
    private RedisUtils redisUtils;
    /**
     * 生成token
     * @param username 用户名
     * @param password 密码
     * @return  token
     */
    public String createToken(String username,String password){
        Calendar calendar = Calendar.getInstance();
        //12小时后过期
        calendar.add(Calendar.HOUR,TimeOutHours);
        String token = JWT.create()
                .withClaim("username",username)
                .withClaim("password",password)
                .withExpiresAt(calendar.getTime())
                .sign(Algorithm.HMAC256(jwtSecret));
        log.info("---创建token:" + token + "---");
        log.info("-----正在保存token----");
        this.saveData(username,token);
        log.info("保存成功");
        return token;
    }

    //根据token解析出username
    public static String getUsername(String token){
        RedisUtils redisUtils = new RedisUtils();
        System.out.println(redisUtils.getToken("gaoxu"));
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        log.info("从token解析出的username:" + claims.get("username"));
        return (String)claims.get("username");
    }

    public void saveData(String username,String token){
        redisUtils.setTokenByTime(username,token);
    }

    /**
     * 验证token是否正确
     * @param token token字符串
     */
    public static void TokenVerify(String token){
        log.info("----正在验证token:" + token + "-----");
        JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
    }

    /**
     * 获取token信息
     * @param token token字符串
     * @return  DecodedJWT实例
     */
    public static DecodedJWT getToken(String token){
        return JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
    }
    public void delete(String key){
        redisUtils.expireToken(key);
    }
}