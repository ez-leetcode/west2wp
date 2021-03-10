package com.west2wp.config;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.west2wp.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

//JWT拦截器
@Slf4j     //日志注解
public class JWTInterceptor implements HandlerInterceptor {

    /*
    @Autowired
    private RedisTemplate redisTemplate;
     */

    //redis一直出bug,改用线程安全的ConcurrentHashMap暂时代替一下
    public static ConcurrentHashMap<String,String> tokenCodeMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //设置中文
        response.setContentType("application/json;charset=UTF-8");
        JSONObject jsonObject = new JSONObject();
        //获取请求的用户名
        String username = request.getParameter("username");
        if(username == null){
            jsonObject.put("message","UsernameWrong");
            log.warn("-----用户名不存在,已被拦截-----");
            response.getWriter().print(jsonObject);
            return false;
        }
        //获取token
        String token = request.getHeader("token");
        try{
            //验证token是否合法
            JWTUtils.TokenVerify(token);
        }catch(SignatureVerificationException e){
            //签名无效
            log.warn("-----认证签名无效------");
            jsonObject.put("message","SignWrong");
            e.printStackTrace();
        }catch (TokenExpiredException e){
            //token过期
            log.warn("------token过期-----");
            jsonObject.put("message","DateWrong");
            e.printStackTrace();
        }catch (AlgorithmMismatchException e){
            //算法出错
            log.warn("----token算法错误----");
            jsonObject.put("message","AlgorithmWrong");
            e.printStackTrace();
        }catch (Exception e){
            //token无效
            log.warn("-----token无效------");
            jsonObject.put("message","TokenWrong");
            e.printStackTrace();
        }
        log.info("-----token有效-------");
        log.info(token);
        //根据hashmap获取
        String realToken = tokenCodeMap.get(username);
        if(token.equals(realToken)){
            return true;
        }
        //token合法时,解析token和redis数据库中数据比较判断是否可以通过登录(可有效防止用他人token登录)
        /*
        String redisToken = (String) redisTemplate.opsForValue().get(JWTUtils.getUsername(token));
        if(redisToken != null && redisToken.equals(token)){
            log.info("-----用户认证成功!,username:" + JWTUtils.getUsername(token));
            jsonObject.put("message","success");
            jsonObject.put("state",true);
            response.getWriter().print(jsonObject);
            return true;
        }
         */
        response.getWriter().print(jsonObject);
        log.info("-----用户认证失败!需要重新登录------");
        return false;
    }
}