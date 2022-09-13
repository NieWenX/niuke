package com.nie.controller;

import com.nie.entity.DiscussPost;
import com.nie.entity.LoginTicket;
import com.nie.entity.Page;
import com.nie.entity.User;
import com.nie.result.ResponseApi;
import com.nie.service.DiscussPostFeign;
import com.nie.service.UserLoginFeign;
import com.nie.service.UserServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/post")
public class DiscussPostController {

    @Autowired
    private DiscussPostFeign discussPostFeign;

    @Autowired
    private UserLoginFeign userLoginFeign;

    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * 发布帖子
     *
     * @param title   标题
     * @param content 正文
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseApi addDiscussPost(@RequestParam String title,@RequestParam String content,@CookieValue("ticket") String ticket) {

        return discussPostFeign.addDiscussPost(title,content,ticket);
    }

    /**
     * 查询帖子详情
     * 帖子的回复分为评论：1.对帖子进行评论（comment） 2.对评论进行评论（reply）
     * 首先展示所有的 comment，再针对每个 comment 展示 reply
     */
    @GetMapping("/detail/{id}")
    public String getDiscussPost(@PathVariable("id") int id, Page page, @CookieValue(value = "ticket",required = false) String ticket, Model model){
        // 判断用户是否登录,从而显示不同的页面情况
        LoginTicket loginTicket = userLoginFeign.isLogin(ticket).getData();
        User loginUser = null;
        if(loginTicket != null){
            loginUser = userServiceFeign.selectById(loginTicket.getUserId()).getData();
            model.addAttribute("loginUser",loginUser);
        }

        ResponseApi<Map<String,Object>> discussPost = discussPostFeign.getDiscussPost(id, page.getOffset(),page.getLimit(),ticket);
        Map<String,Object> map = discussPost.getData();

        // 评论（分页）
        int totalCommentRows = (int) map.get("totalCommentRows");
        page.setLimit(5);
        page.setPath("/post/detail/" + id);
        page.setRows(totalCommentRows);

        Map<String,Object> post = (Map<String, Object>) map.get("post");
        model.addAttribute("post", post);

        Map<String,Object> user = (Map<String,Object>) map.get("user");
        model.addAttribute("user", user);

        Integer likeCount = (Integer) map.get("likeCount");
        model.addAttribute("likeCount", likeCount);

        Integer likeStatus = (Integer) map.get("likeStatus");
        model.addAttribute("likeStatus", likeStatus);

        List<Map<String, Object>> commitVoList = (List<Map<String, Object>>) map.get("commitVoList");
        model.addAttribute("comments", commitVoList);
        return "site/discuss-detail";
    }

}
