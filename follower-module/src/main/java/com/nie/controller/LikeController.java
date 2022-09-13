package com.nie.controller;


import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/like")
public class LikeController {

    @Autowired
    private LikeService likeService;


    /**
     * 点赞(异步请求)
     *
     * @param entityType   实体类型(用户/帖子/回复)
     * @param entityId     实体 id
     * @param entityUserId 实体用户 id
     */
    @PostMapping("/like")
    public ResponseApi<Map<String, Object>> like(Integer entityType, Integer entityId, Integer entityUserId, Integer userId) {

        // 点赞
        likeService.like(userId, entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeService.selectEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.selectEntityLikeStatus(userId, entityType, entityId);
        // 返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return new ResponseApi<Map<String, Object>>("点赞成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),map);

    }

    @GetMapping("/selectEntityLikeCount")
    public ResponseApi<Long> selectEntityLikeCount(@RequestParam("entityType") Integer entityType, @RequestParam("entityId") Integer entityId){
        // 数量
        long likeCount = likeService.selectEntityLikeCount(entityType, entityId);
        return new ResponseApi<>("查询当前点赞数",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),likeCount);
    }

    @GetMapping("/selectEntityLikeStatus")
    public ResponseApi<Integer> selectEntityLikeStatus(@RequestParam("userId") int userId, @RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId){
        // 状态
        int likeStatus = likeService.selectEntityLikeStatus(userId, entityType, entityId);
        return new ResponseApi<>("查询当前点赞状态",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),likeStatus);

    }

    @GetMapping("/selectUserLikeCount")
    public ResponseApi<Long> selectUserLikeCount(@RequestParam("userId") int userId){
        // 状态
        long likeCount = likeService.selectUserLikeCount(userId);
        return new ResponseApi<>("查询当前点赞状态",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),likeCount);

    }
}
