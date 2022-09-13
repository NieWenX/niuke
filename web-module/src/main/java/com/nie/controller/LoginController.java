package com.nie.controller;

import com.google.code.kaptcha.Producer;
import com.nie.entity.LoginTicket;
import com.nie.entity.User;
import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.UserLoginFeign;
import com.nie.service.UserServiceFeign;
import com.nie.utils.RedisKeyUtil;
import com.nie.utils.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
@Slf4j
public class LoginController {

    @Autowired
    private UserLoginFeign userLoginFeign;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserThreadLocal userThreadLocal;

    /**
     * 跳转到用户登录页面
     */
    @GetMapping("/login")
    public String toLogin() {
        return "site/login";
    }

    /**
     * 跳转到用户注册界面
     */
    @GetMapping("/register")
    public String toRegister() {
        return "site/register";
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public String login(@RequestParam(value = "username",required = false) String username,@RequestParam(value = "password",required = false) String password,
                        @RequestParam(value = "code",required = false) String code,
                        @RequestParam(value = "rememberMe",required = false) boolean rememberMe, @CookieValue(value = "kaptchaOwner",required = false) String kaptchaOwner,
                        HttpServletResponse response, Model model) {

        // 调用user模块的login
        ResponseApi responseApi = userLoginFeign.login(username, password,code,rememberMe,kaptchaOwner);
        // 对api返回结果和数据做处理
        Map<String, Object> result = (Map<String, Object>)responseApi.getData();

        if(responseApi.getCode().equals(HttpStatus.OK.toString())){
            Integer expired = (Integer)result.get("expired");
            Cookie cookie = new Cookie("ticket", result.get("ticket").toString());
            cookie.setPath("/");
            cookie.setMaxAge(expired);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("UsernameMessage", result.get("UsernameMessage"));
            model.addAttribute("PasswordMessage", result.get("PasswordMessage"));
            model.addAttribute("CodeMessage", result.get("CodeMessage"));
            return "site/login";
        }

    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public String register(Model model, User user) {

        ResponseApi  register = userLoginFeign.register(user);
        Map<String, Object> result = (Map<String, Object>)register.getData();

        if(register.getCode().equals(ErrorCodeEnum.SUCCESS.getErrorCode())){
            model.addAttribute("msg", "注册成功，请到邮箱激活该账号!");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        }else {
            model.addAttribute("UsernameMessage", result.get("UsernameMessage"));
            model.addAttribute("PasswordMessage", result.get("PasswordMessage"));
            return "site/register";
        }
    }

    /**
     * 获取验证码
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response) {
        ResponseApi kaptcha = userLoginFeign.getKaptcha();
        Map<String,Object> map = (Map)kaptcha.getData();
        String text =  (String) map.get("kaptchaText");
        BufferedImage image = kaptchaProducer.createImage(text);

        // 验证码的索引存入cookie中
        String kaptchaOwner = (String) map.get("kaptchaOwner");
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(2*60);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            // 该流不用关闭，Spring 会帮我们关
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户退出
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        //删除redis中的ticket
        ResponseApi responseApi = userLoginFeign.logout(ticket);
        //cookie中的ticket设置为过期

        return "redirect:/login";
    }


}
