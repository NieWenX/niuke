package com.nie.service;

import com.nie.result.ResponseApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "gateway-server",path = "/follow")
public interface FollowServiceFeign {

    @PostMapping("/follow")
    public ResponseApi<Map<String, Object>> follow(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId,
                                                   @RequestParam("userId") int userId);

    @PostMapping("/unfollow")
    public ResponseApi<Map<String, Object>> unfollow(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId,
                                                     @RequestParam("userId") int userId);

    @GetMapping("/followees")
    public ResponseApi<Map<String, Object>> getFollowees(@RequestParam("userId") int userId,  @RequestParam("loginUserId") int loginUserId, @RequestParam("pageOffset") int pageOffset,
                                                         @RequestParam("pageLimit") int pageLimit);

    @GetMapping("/followers")
    public ResponseApi<Map<String, Object>> getFollowers(@RequestParam("userId") int userId,  @RequestParam("loginUserId") int loginUserId, @RequestParam("pageOffset") int pageOffset,
                                                         @RequestParam("pageLimit") int pageLimit);

    @GetMapping("/followers/selectFollowerCount")
    public ResponseApi<Integer>  selectFollowerCount(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId);

    @GetMapping("/followees/selectFolloweeCount")
    public ResponseApi<Integer>  selectFolloweeCount(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId);

    @GetMapping("/hasFollowed")
    public ResponseApi<Boolean>  hasFollowed(@RequestParam("userId") int userId, @RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId);

}
