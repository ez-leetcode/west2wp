package com.west2wp.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                  //类的setter和getter方法
@AllArgsConstructor    //类的有参构造函数
@NoArgsConstructor     //类的无参构造函数
//为实现文件夹体系,在上传文件时除了url额外记录用户的父级文件,对于非资源文件(文件夹)特别设置type
public class FileData {
    private String url;          //文件url
    private String username;     //用户名
    private String filename;     //文件名
    private String parent;       //父级文件
    private String type;         //代表文件类型,为了区分文件夹,给文件夹取名wjj
    private Double sizes;        //文件大小
    private String date;         //上传日期
}