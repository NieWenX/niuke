package com.nie.service;

import com.nie.entity.Comment;
import com.nie.entity.DiscussPost;
import com.nie.result.ResponseApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "gateway-server",path = "/discussPost")
public interface DiscussPostFeign {

    @GetMapping("/index")
    public ResponseApi<Map<String,Object>> index(@RequestParam("pageOffset") Integer pageOffset, @RequestParam("pageLimit") Integer pageLimit);

    @PostMapping("/add")
    public ResponseApi addDiscussPost(@RequestParam("title") String title, @RequestParam("content") String content, @RequestParam("ticket") String ticket);

    @GetMapping("/detail")
    public ResponseApi<Map<String,Object>> getDiscussPost(@RequestParam("id") int id, @RequestParam("pageOffset") Integer pageOffset,
                                                          @RequestParam("pageLimit") Integer pageLimit, @RequestParam("ticket") String ticket);


    @GetMapping("/selectDiscussPostRows")
    public ResponseApi<Integer> selectDiscussPostRows(@RequestParam("userId") int userId);

    @GetMapping("/selectDiscussPosts")
    public ResponseApi<List<DiscussPost>> selectDiscussPosts(@RequestParam("userId") int userId,@RequestParam("offset") int offset,
                                                             @RequestParam("limit") int limit);

    @GetMapping("/selectDiscussPostById")
    public ResponseApi<DiscussPost> selectDiscussPostById(@RequestParam("id") int id);
}
