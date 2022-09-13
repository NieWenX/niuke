package com.nie.controller;


import com.alibaba.fastjson.JSON;
import com.nie.entity.LoginTicket;
import com.nie.entity.Page;
import com.nie.entity.User;
import com.nie.result.ResponseApi;
import com.nie.service.FollowServiceFeign;
import com.nie.service.UserLoginFeign;
import com.nie.service.UserServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class FollowController {

    @Autowired
    private FollowServiceFeign followServiceFeign;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private UserLoginFeign userLoginFeign;

    /**
     * 关注
     */
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId,@CookieValue("ticket") String ticket) {
        LoginTicket login = userLoginFeign.isLogin(ticket).getData();
        ResponseApi<Map<String, Object>> result = null;
        if(login!=null){
            result = followServiceFeign.follow(entityType,entityId,login.getUserId());
        }
        return JSON.toJSONString(result);
    }

    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId,@CookieValue("ticket") String ticket) {
        LoginTicket login = userLoginFeign.isLogin(ticket).getData();
        ResponseApi<Map<String, Object>> result = null;
        if(login!=null){
            result = followServiceFeign.unfollow(entityType,entityId,login.getUserId());
        }
        return JSON.toJSONString(result);
    }


    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable int userId, Page page,
                               @CookieValue("ticket") String ticket, Model model) {
        User user = userServiceFeign.selectById(userId).getData();
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        LoginTicket login = userLoginFeign.isLogin(ticket).getData();
        User loginUser = null;
        if(login != null){
            loginUser = userServiceFeign.selectById(login.getUserId()).getData();
            model.addAttribute("loginUser",loginUser);
        }

        ResponseApi<Map<String, Object>> result;
        Map<String, Object> data = null;

        // 分页条件
        page.setLimit(5);
        page.setPath("/followees/" + userId);

        if(login!=null){
            result = followServiceFeign.getFollowees(userId,login.getUserId(), page.getOffset(),page.getLimit());
            data = result.getData();
        }

        page.setRows((int) data.get("rows"));
        model.addAttribute("users",  data.get("followeeList"));
        return "site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable int userId, Page page,
                               @CookieValue("ticket") String ticket, Model model) {
        User user = userServiceFeign.selectById(userId).getData();
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        LoginTicket login = userLoginFeign.isLogin(ticket).getData();
        User loginUser = null;
        if(login != null){
            loginUser = userServiceFeign.selectById(login.getUserId()).getData();
            model.addAttribute("loginUser",loginUser);
        }

        ResponseApi<Map<String, Object>> result = null;
        Map<String, Object> data = null;

        // 分页条件
        page.setLimit(5);
        page.setPath("/followers/" + userId);

        if(login!=null){
            result = followServiceFeign.getFollowers(userId,login.getUserId(), page.getOffset(),page.getLimit());
            data = result.getData();
        }

        page.setRows((int) data.get("rows"));
        model.addAttribute("users",  data.get("followerList"));
        return "site/follower";
    }

}
