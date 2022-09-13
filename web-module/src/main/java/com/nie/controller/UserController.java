package com.nie.controller;


import com.nie.entity.*;
import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.*;
import com.nie.utils.Constant;
import com.nie.utils.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserLoginFeign userLoginFeign;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private UserThreadLocal userThreadLocal;

    @Autowired
    private LikeServiceFeign likeServiceFeign;

    @Autowired
    private FollowServiceFeign followServiceFeign;

    @Autowired
    private DiscussPostFeign discussPostFeign;

    @Autowired
    private CommentServiceFeign commentServiceFeign;

    /**
     * 跳转到用户个人页面
     */
    @GetMapping("/profile/{userId}")
    public String toProfile(@PathVariable int userId ,@CookieValue("ticket") String ticket, Model model) {
        User user = userServiceFeign.selectById(userId).getData();
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        long likeCount = likeServiceFeign.selectUserLikeCount(userId).getData();
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followServiceFeign.selectFolloweeCount(Constant.ENTITY_TYPE_USER, userId).getData();
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followServiceFeign.selectFollowerCount(Constant.ENTITY_TYPE_USER, userId).getData();
        model.addAttribute("followerCount", followerCount);

        // 当前用户是否已关注当前页面上的用户
        boolean hasFollowed = false;
        LoginTicket login = userLoginFeign.isLogin(ticket).getData();
        User loginUser;
        if(login != null){
            loginUser = userServiceFeign.selectById(login.getUserId()).getData();
            model.addAttribute("loginUser",loginUser);
        }

        hasFollowed = followServiceFeign.hasFollowed(login.getUserId(), Constant.ENTITY_TYPE_USER, userId).getData();
        model.addAttribute("hasFollowed", hasFollowed);
        return "site/profile";
    }

    /**
     * 跳转到用户设置页面
     */
    @GetMapping("/setting")
    public String toSetting() {
        return "site/setting";
    }

    /**
     * 跳转到我的帖子页面
     */
    @GetMapping("/mypost")
    public String toMyPost(Model model, Page page ,@CookieValue("ticket") String ticket) {
        // 获取当前登录用户
        LoginTicket login = userLoginFeign.isLogin(ticket).getData();
        User curUser = userServiceFeign.selectById(login.getUserId()).getData();
        model.addAttribute("user", curUser);

        // 设置分页信息
        page.setLimit(5);
        page.setRows(discussPostFeign.selectDiscussPostRows(curUser.getId()).getData());
        page.setPath("/user/mypost");


        // 查询某用户发布的帖子
        List<DiscussPost> discussPosts = discussPostFeign.selectDiscussPosts(curUser.getId(), page.getOffset(), page.getLimit()).getData();
        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost post : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                // 点赞数量
                long likeCount = likeServiceFeign.selectEntityLikeCount(Constant.ENTITY_TYPE_POST, post.getId()).getData();
                map.put("likeCount", likeCount);

                list.add(map);
            }
        }
        // 帖子数量
        int postCount = discussPostFeign.selectDiscussPostRows(curUser.getId()).getData();
        model.addAttribute("postCount", postCount);
        model.addAttribute("discussPosts", list);

        return "site/my-post";
    }

    /**
     * 跳转到我的评论页面
     */
    @GetMapping("/mycomment")
    public String toMyReply(Model model, Page page) {
        // 获取当前登录用户
        User curUser = userThreadLocal.getUser();
        model.addAttribute("user", curUser);

        // 设置分页信息
        page.setLimit(5);
        page.setRows(commentServiceFeign.selectCountByUserId(curUser.getId()).getData());
        page.setPath("/user/mycomment");

        // 获取用户所有评论 (而不是回复,所以在 sql 里加一个条件 entity_type = 1)
        List<Comment> comments = commentServiceFeign.selectCommentByUserId(curUser.getId(), page.getOffset(), page.getLimit()).getData();
        List<Map<String, Object>> list = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);

                // 根据实体 id 查询对应的帖子标题
                String discussPostTitle = discussPostFeign.selectDiscussPostById(comment.getEntityId()).getData().getTitle();
                map.put("discussPostTitle", discussPostTitle);

                list.add(map);
            }
        }

        // 回复的数量
        int commentCount = commentServiceFeign.selectCountByUserId(curUser.getId()).getData();
        model.addAttribute("commentCount", commentCount);

        model.addAttribute("comments", list);
        return "site/my-comment";
    }


    /**
     * 密码修改
     */
    @PostMapping("/password")
    public String changePassword(Model model, String oldPassword, String newPassword, String confirmPassword, @CookieValue String ticket) {
        // 当前登录用户
        User curUser = userThreadLocal.getUser();
        ResponseApi<Map<String, Object>> mapResponseApi = userLoginFeign.changePassword(curUser.getId(), oldPassword, newPassword, confirmPassword);
        // map 为空则修改成功
        if (mapResponseApi.getCode().equals(ErrorCodeEnum.SUCCESS.getErrorCode())) {
            userLoginFeign.logout(ticket);
            return "redirect:/login";
        }
        Map<String, Object> map = mapResponseApi.getData();
        model.addAttribute("OldPasswordMessage", map.get("OldPasswordMessage"));
        model.addAttribute("NewPasswordMessage", map.get("NewPasswordMessage"));
        model.addAttribute("ConfirmPasswordMessage", map.get("ConfirmPasswordMessage"));
        return "site/setting";
    }

}
