package com.nie.controller;

import com.alibaba.fastjson.JSON;
import com.nie.entity.LoginTicket;
import com.nie.result.ResponseApi;
import com.nie.service.LikeServiceFeign;
import com.nie.service.UserLoginFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private UserLoginFeign userLoginFeign;

    @Autowired
    private LikeServiceFeign likeServiceFeign;

    /**
     * 点赞(异步请求),这个需要登录验证，在网关里完成
     *
     * @param entityType   实体类型(用户/帖子/回复)
     * @param entityId     实体 id
     * @param entityUserId 实体用户 id
     */
    @PostMapping("/like")
    @ResponseBody
    public ResponseApi<Map<String, Object>> like(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId,
                       @RequestParam("entityUserId") int entityUserId, @CookieValue("ticket") String ticket) {

        LoginTicket login = userLoginFeign.isLogin(ticket).getData();
        ResponseApi<Map<String, Object>> result = null;
        if(login!=null){
            result = likeServiceFeign.like(entityType, entityId, entityUserId,login.getUserId());
        }

        return result;
    }

}
