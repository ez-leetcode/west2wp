package com.west2wp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

//redis出问题了,暂时不用这个类

@Slf4j
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate <String,String> redisTemplate;

    private final static long permitTime = 60 * 60 * 8;

    //存token
    public  void setToken(String key,String value){
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key,value);
        log.info("token已存入数据库,username:" + key + " token:" + value);
    }

    //存token(有时间)
    public void setTokenByTime(String key,String value){
        redisTemplate.opsForValue().set(key,value,permitTime);
        log.info("token已存入数据库,username:" + key + " token:" + value);
    }

    //注销时删除对应键值对
    public void expireToken(String key){
        redisTemplate.delete(key);
        log.info("token已销毁,username:" + key);
    }

    //根据用户名返回token
    public String getToken(String username){
        System.out.println(redisTemplate.opsForValue().get("gaoxu"));
        System.out.println(1);
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(username);
    }
   /*
    //获取key的过期时间
    public long getExpireTime(String key){
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
   */
}