package com.nie.service;

import com.nie.result.ResponseApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "gateway-server",path = "/like")
public interface LikeServiceFeign {
    @GetMapping("/selectEntityLikeCount")
    public ResponseApi<Long> selectEntityLikeCount(@RequestParam("entityType") Integer entityType, @RequestParam("entityId") Integer entityId);

    @GetMapping("/selectUserLikeCount")
    public ResponseApi<Long> selectUserLikeCount(@RequestParam("userId") Integer userId);

    @PostMapping("/like")
    public ResponseApi<Map<String, Object>> like(@RequestParam("entityType") Integer entityType, @RequestParam("entityId") Integer entityId,
                                                 @RequestParam("entityUserId") Integer entityUserId, @RequestParam("userId") Integer userId);


    @GetMapping("/selectEntityLikeStatus")
    public ResponseApi<Integer> selectEntityLikeStatus(@RequestParam("userId") int userId, @RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId);
}
