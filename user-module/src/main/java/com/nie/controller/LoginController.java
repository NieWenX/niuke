package com.nie.controller;


import com.google.code.kaptcha.Producer;
import com.nie.entity.LoginTicket;
import com.nie.entity.User;
import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.UserService;
import com.nie.utils.CommonUtil;
import com.nie.utils.Constant;
import com.nie.utils.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Producer kaptchaProducer;


    /**
     * 用户登录
     */
    @PostMapping("/user/login")
    public ResponseApi login(@RequestParam(value = "username",required = false) String username, @RequestParam(value = "password",required = false) String password,
                             @RequestParam(value = "code",required = false) String code,
                             @RequestParam(value = "rememberMe",required = false) boolean rememberMe,@RequestParam(value = "kaptchaOwner") String kaptchaOwner) {

        log.info("跳转到用户模块");
        // 检查验证码，账号和密码
        int expired = rememberMe ? Constant.REMEMBER_EXPIRED_SECONDS : Constant.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> result = userService.login(kaptchaOwner,username, password, code, expired);
        if (result.containsKey("ticket")) {
            return new ResponseApi<Map<String, Object>>("登陆成功",HttpStatus.OK.toString(),System.currentTimeMillis(),result);
        } else {
            return new ResponseApi<Map<String, Object>>("登陆失败",HttpStatus.UNAUTHORIZED.toString(), System.currentTimeMillis(),result);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/user/register")
    public ResponseApi register(@RequestBody User user){
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            return new ResponseApi<Map<String, Object>>("注册成功",ErrorCodeEnum.SUCCESS.getErrorCode(),System.currentTimeMillis(),map);
        } else {
            return new ResponseApi<Map<String, Object>>("注册失败", ErrorCodeEnum.AUTHFAIL.getErrorCode(),System.currentTimeMillis(),map);
        }
    }

    /**
     * 获取验证码
     */
    @GetMapping("/user/kaptcha")
    public ResponseApi getKaptcha() {
        Map<String,Object> map = new HashMap<>();

        // 生成验证码
        String text = kaptchaProducer.createText();
        map.put("kaptchaText",text);

        // 验证码的归属者(随机字符串)
        String kaptchaOwner = CommonUtil.generateUUID();
        map.put("kaptchaOwner",kaptchaOwner);

        // 将验证码存入 redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 2*60, TimeUnit.SECONDS);

        return new ResponseApi<Map<String, Object>>("生成验证码", ErrorCodeEnum.SUCCESS.getErrorCode(),System.currentTimeMillis(),map);
    }

    /**
     * 判断用户是否登录
     */
    @GetMapping("/user/isLogin")
    public ResponseApi<LoginTicket> isLogin(@RequestParam(value = "ticket",required = false) String ticket){
        if(StringUtils.hasText(ticket)){
            String ticketKey = RedisKeyUtil.getTicketKey(ticket);
            LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
            return new ResponseApi<LoginTicket>("用户已登录",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),loginTicket);
        }else {
            return new ResponseApi<LoginTicket>("用户未登录",ErrorCodeEnum.FAIL.getErrorCode(), System.currentTimeMillis(),null);
        }
    }

    @GetMapping("/user/logout")
    public ResponseApi logout(@RequestParam("ticket") String ticket){
        userService.logout(ticket);
        return new ResponseApi("用户退出",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),null);

    }

    @GetMapping("/user/changePassword")
    public ResponseApi<Map<String, Object>> changePassword(int id, String oldPassword, String newPassword, String confirmPassword){
        Map<String, Object> map = userService.changePassword(id, oldPassword, newPassword, confirmPassword);
        // map 为空则修改成功
        if (map.isEmpty()) {
            return new ResponseApi<>("用户修改密码成功",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),map);

        }
        return new ResponseApi<>("用户密码失败",ErrorCodeEnum.FAIL.getErrorCode(), System.currentTimeMillis(),map);
    }
}
