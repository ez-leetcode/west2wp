package com.west2wp.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class MailServiceImpl implements MailService{

    @Resource
    private JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String email,String yzm){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1006021669@qq.com");
        message.setTo(email);
        message.setSubject("西二在线网盘验证码");
        message.setText("您的验证码:" + yzm);
        javaMailSender.send(message);
    }
}