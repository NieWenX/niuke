package com.nie.controller;


import com.nie.entity.Comment;
import com.nie.result.ResponseApi;
import com.nie.service.CommentServiceFeign;
import com.nie.service.DiscussPostFeign;
import com.nie.service.UserLoginFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentServiceFeign commentServiceFeign;


    /**
     * 增加评论/回复
     *
     * @param discussPostId 帖子 id
     * @param comment       评论/回复
     */
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable int discussPostId,  Comment comment, @CookieValue("ticket") String ticket) {


        Comment data = commentServiceFeign.addComment(ticket, comment).getData();
        return "redirect:/post/detail/" + discussPostId;

    }

}
