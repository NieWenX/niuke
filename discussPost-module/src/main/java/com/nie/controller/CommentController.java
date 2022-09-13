package com.nie.controller;


import com.nie.entity.Comment;
import com.nie.entity.LoginTicket;
import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.CommentService;
import com.nie.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/comment/add",method = RequestMethod.POST)
    public ResponseApi<Comment> addComment(@RequestParam String ticket,@RequestBody Comment comment) {


        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);

        comment.setUserId(loginTicket.getUserId());
        comment.setStatus(0);
        if(comment.getTargetId() == null){
            comment.setTargetId(0);
        }
        comment.setCreateTime(new Date());

        Boolean isBoolean = commentService.insertComment(comment);

         if(isBoolean){
             return new ResponseApi<Comment>("评论成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(), comment);
         }else {
             return new ResponseApi<Comment>("评论出错", ErrorCodeEnum.FAIL.getErrorCode(), System.currentTimeMillis(), comment);
         }
    }


    @GetMapping("/comment/selectCountByUserId")
   public ResponseApi<Integer> selectCountByUserId(int userId){
        int count = commentService.selectCountByUserId(userId);
        return new ResponseApi<>("success", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(), count);
    }

    @GetMapping("/comment/selectCommentByUserId")
    public ResponseApi<List<Comment>> selectCommentByUserId(int userId, int offset, int limit){
        List<Comment> comments = commentService.selectCommentByUserId(userId, offset, limit);
        return new ResponseApi<>("success", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(), comments);

    }
}
