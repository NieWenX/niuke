package com.nie.service.impl;


import com.nie.entity.LoginTicket;
import com.nie.entity.User;
import com.nie.mapper.UserMapper;
import com.nie.service.UserService;
import com.nie.utils.CommonUtil;
import com.nie.utils.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;


    @Autowired
    private RedisTemplate redisTemplate;


    // 1. 先从缓存中查询
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2. 取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3. 数据变更时清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    /**
     * 根据用户 id 查询用户
     */
    @Override
    public User selectById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    @Override
    public User selectByUsername(String username) {
        return null;
    }

    @Override
    public User selectByEmail(String email) {
        return null;
    }

    @Override
    public int insertUser(User user) {
        return 0;
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值判断
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("UsernameMessage", "账号不能为空!");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("PasswordMessage", "密码不能为空!");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("EmailMessage", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (dbUser != null) {
            map.put("UsernameMessage", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        dbUser = userMapper.selectByEmail(user.getEmail());
        if (dbUser != null) {
            map.put("EmailMessage", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(CommonUtil.generateUUID().substring(0, 5));
        user.setPassword(CommonUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommonUtil.generateUUID());
//        user.setHeaderUrl("https://weizujie.oss-cn-shenzhen.aliyuncs.com/img/avatar.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件
/*        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/activation/153/ajdaejfsiufhsfbsef
        String url = domain + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);*/

        return map;
    }

    @Override
    public int updateStatus(int userId, int status) {
        return 0;
    }

    @Override
    public int activation(int userId, String code) {
        return 0;
    }

    @Override
    public Map<String, Object> login(String kaptchaOwner,String username, String password, String code, int expired) {
        Map<String, Object> map = new HashMap<>();

        // 判断验证码
        String kaptcha = null;
        if (StringUtils.isNoneBlank(kaptchaOwner)) {
            // 从 redis 取验证码
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            map.put("KaptchaMessage", "验证码错误!");
            return map;
        }

        // 空值判断
        if (StringUtils.isBlank(username)) {
            map.put("UsernameMessage", "账号不能为空!");

        }

        if (StringUtils.isBlank(password)) {
            map.put("PasswordMessage", "密码不能为空!");

        }

        // 账号验证
        User dbUser = userMapper.selectByUsername(username);
        if (dbUser == null) {
            map.put("UsernameMessage", "该账号不存在!");
            return map;
        }
/*        if (dbUser.getStatus() == 0) {
            map.put("UsernameMessage", "该账号未激活!");
            return map;
        }*/
        // 密码验证
        password = CommonUtil.md5(password + dbUser.getSalt());
        if (!dbUser.getPassword().equals(password)) {
            map.put("PasswordMessage", "密码错误!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(dbUser.getId());
        loginTicket.setTicket(CommonUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expired * 1000L));

        // 将登录凭证存入 redis
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        map.put("expired",expired);

        return map;
    }

    @Override
    public void logout(String ticket) {
        // return loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    @Override
    public LoginTicket selectByTicket(String ticket) {
        return null;
    }

    @Override
    public int updateHeaderUrl(int userId, String avatarUrl) {
        return 0;
    }

    @Override
    public Map<String, Object> changePassword(int id, String oldPassword, String newPassword, String confirmPassword) {
        return null;
    }
}
