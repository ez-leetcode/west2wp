package com.west2wp.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public JWTInterceptor getJWTInterceptor(){
        return new JWTInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getJWTInterceptor())
                //拦截设置:除了登录,注册,注销,发送验证码,修改密码接口以外所有接口
                //.addPathPatterns("/abc/**")
                .addPathPatterns("/**")
                .excludePathPatterns("/login")
                .excludePathPatterns("/register")
                .excludePathPatterns("/sendEmail")
<<<<<<< HEAD
                .excludePathPatterns("/changePassword");
=======
                .excludePathPatterns("/upload")
                .excludePathPatterns("/photoUpload")
                .excludePathPatterns("/getPhoto")
                .excludePathPatterns("/findPassword");
>>>>>>> 2017e29 (west2wp)
    }
}