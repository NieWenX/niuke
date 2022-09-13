package com.nie.controller;

import com.nie.entity.DiscussPost;
import com.nie.entity.Page;
import com.nie.entity.User;
import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.DiscussPostService;
import com.nie.service.LikeServiceFeign;
import com.nie.service.UserLoginFeign;
import com.nie.service.UserServiceFeign;
import com.nie.utils.Constant;
import com.nie.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private LikeServiceFeign likeService;

    @Autowired
    private RedisTemplate redisTemplate;



    @GetMapping("/discussPost/index")
    public ResponseApi<Map<String,Object>> index(Integer pageOffset,Integer pageLimit){
        Map<String,Object> result = new HashMap<>();

        // 设置总的数据行数
        Integer totalRows = (Integer) redisTemplate.opsForValue().get(RedisKeyUtil.getDiscussPostTotalRowsKey());
        if(totalRows==null){
            totalRows = discussPostService.selectDiscussPostRows(0);
            redisTemplate.opsForValue().set(RedisKeyUtil.getDiscussPostTotalRowsKey(),totalRows);
        }
        result.put("totalRows",totalRows);

        //format():将给定的 Date 格式化为日期/时间字符串。即：date--->String
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//定义新的日期格式

        // 查询当前页帖子
        List<DiscussPost> discussPosts = discussPostService.selectDiscussPosts(0, pageOffset, pageLimit);
        List<Map<String, Object>> list = new ArrayList<>();
        for (DiscussPost post : discussPosts) {
            Map<String, Object> map = new HashMap<>();
            map.put("post", post);
            map.put("postTime",formatter.format(post.getCreateTime()));
            User user = userServiceFeign.selectById(post.getUserId()).getData();
            map.put("user", user);

            // 点赞数量
            long likeCount = likeService.selectEntityLikeCount(Constant.ENTITY_TYPE_POST, post.getId()).getData();
            map.put("likeCount", likeCount);
            list.add(map);
        }
        result.put("list",list);
        return new ResponseApi("查询当前页帖子", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),result);
    }
}
