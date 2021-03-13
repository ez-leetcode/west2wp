package com.west2wp.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  //get&set方法
@AllArgsConstructor  //有参构造函数
@NoArgsConstructor   //无参构造函数
public class User {
    private String username;      //用户名
    private String password;      //密码,在服务器用MD5存储
    private String email;         //用户注册时填的邮箱
    private String photo;         //用户头像的url
    private double space;         //用户网盘剩余空间,默认空间1024MB
}
