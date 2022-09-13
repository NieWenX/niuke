package com.nie.service;

import com.nie.entity.Comment;
import com.nie.result.ResponseApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "gateway-server",path = "/comment")
public interface CommentServiceFeign {

    @GetMapping("/selectCountByUserId")
    public ResponseApi<Integer> selectCountByUserId(@RequestParam("userId") int userId);

    @GetMapping("/selectCommentByUserId")
    public ResponseApi<List<Comment>> selectCommentByUserId(@RequestParam("userId") int userId,@RequestParam("offset") int offset,
                                                            @RequestParam("limit") int limit);

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ResponseApi<Comment> addComment(@RequestParam("ticket") String ticket, @RequestBody Comment comment);

}
