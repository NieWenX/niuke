package com.nie.controller;


import com.nie.entity.User;
import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.FollowService;
import com.nie.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    /**
     * 关注
     */
    @PostMapping("/follow")
    public ResponseApi<Map<String, Object>> follow(int entityType, int entityId, int userId) {
        try {
            followService.follow(userId, entityType, entityId);
        } catch (Exception e) {
            return new ResponseApi<>("关注失败", ErrorCodeEnum.FAIL.getErrorCode(), System.currentTimeMillis(),null);
        }
        return new ResponseApi<>("关注成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),null);
    }

    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    public ResponseApi<Map<String, Object>> unfollow(int entityType, int entityId,int userId) {
        try {
            followService.unfollow(userId, entityType, entityId);
        } catch (Exception e) {
            return new ResponseApi<>("取消关注失败", ErrorCodeEnum.FAIL.getErrorCode(), System.currentTimeMillis(),null);
        }
        return new ResponseApi<>("取消关注成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),null);
    }

    /**
     * 我关注了谁
     */
    @GetMapping("/followees")
    public ResponseApi<Map<String, Object>> getFollowees(int userId, int loginUserId, int pageOffset, int pageLimit) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> followeeList = followService.selectFolloweeList(userId, pageOffset, pageLimit);
        if (followeeList != null) {
            for (Map<String, Object> map : followeeList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", followService.hasFollowed(loginUserId,Constant.ENTITY_TYPE_USER,u.getId()));
            }
        }
        int rows = (int) followService.selectFolloweeCount(userId, Constant.ENTITY_TYPE_USER);

        result.put("followeeList",followeeList);
        result.put("rows",rows);
        return new ResponseApi<>("成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),result);
    }

    /**
     * 谁关注了我
     * @param userId
     * @param pageOffset
     * @param pageLimit
     * @return
     */
    @GetMapping("/followers")
    public ResponseApi<Map<String, Object>> getFollowers(int userId, int loginUserId, int pageOffset, int pageLimit) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> followerList = followService.selectFollowerList(userId, pageOffset, pageLimit);
        if (followerList != null) {
            for (Map<String, Object> map : followerList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", followService.hasFollowed(loginUserId,Constant.ENTITY_TYPE_USER, u.getId()));
            }
        }
        int rows = (int) followService.selectFollowerCount(Constant.ENTITY_TYPE_USER, userId);

        result.put("followerList",followerList);
        result.put("rows",rows);
        return new ResponseApi<>("成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),result);
    }

    @GetMapping("/followers/selectFollowerCount")
    public ResponseApi<Long>  selectFollowerCount(int entityType, int entityId){

        long rows = (long) followService.selectFollowerCount(entityType, entityId);
        return new ResponseApi<>("成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),rows);

    }

    @GetMapping("/followees/selectFolloweeCount")
    public ResponseApi<Long>  selectFolloweeCount(int entityType, int entityId){

        long rows = (long) followService.selectFolloweeCount(entityId,entityType);
        return new ResponseApi<>("成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),rows);
    }

    @GetMapping("/hasFollowed")
    public ResponseApi<Boolean>  hasFollowed(int userId, int entityType, int entityId){
        boolean followed = followService.hasFollowed(userId, entityType, entityId);
        return new ResponseApi<>("成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),followed);

    }
}
