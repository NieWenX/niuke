package com.nie.service;

import com.nie.entity.LoginTicket;
import com.nie.entity.User;
import com.nie.result.ResponseApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@FeignClient(value = "gateway-server")
public interface UserLoginFeign {

    //用户登录
    @PostMapping("/user/login")
    public ResponseApi login(@RequestParam(value = "username",required = false) String username, @RequestParam(value = "password",required = false) String password,
                             @RequestParam(value = "code",required = false) String code,
                             @RequestParam(value = "rememberMe",required = false) boolean rememberMe,@RequestParam(value = "kaptchaOwner",required = false) String kaptchaOwner);

    //用户注册
    @PostMapping("/user/register")
    public ResponseApi register(@RequestBody User user);

    //登录时获得验证码
    @GetMapping("/user/kaptcha")
    public ResponseApi getKaptcha();

    @GetMapping("/user/logout")
    public ResponseApi logout(@RequestParam("ticket") String ticket);

    @GetMapping("/user/isLogin")
    public ResponseApi<LoginTicket> isLogin(@RequestParam(value = "ticket",required = false) String ticket);

    @GetMapping("/user/changePassword")
    public ResponseApi<Map<String, Object>> changePassword(@RequestParam("id") int id, @RequestParam("oldPassword") String oldPassword,
                                                           @RequestParam("newPassword") String newPassword, @RequestParam("confirmPassword") String confirmPassword);

    }
