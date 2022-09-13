package com.nie.controller;

import com.nie.entity.*;
import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.*;
import com.nie.utils.Constant;
import com.nie.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private LikeServiceFeign likeServiceFeign;

    /**
     * 发布帖子
     *
     * @param title   标题
     * @param content 正文
     */
    @PostMapping("/discussPost/add")
    public ResponseApi addDiscussPost(String title, String content, String ticket) {
        // 判断用户是否登录,可以通过网关进行拦截
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);

        // 发布帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(loginTicket.getUserId());
        post.setTitle(title);
        post.setStatus(0);
        post.setType(0);
        post.setScore(0.0);
        post.setCommentCount(0);
        post.setContent(content);
        post.setCreateTime(new Date());

        int row = discussPostService.insertDiscussPost(post);
        if(row>0){
            String totalRowsKey = RedisKeyUtil.getDiscussPostTotalRowsKey();
            RedisAtomicLong entityIdCounter = new RedisAtomicLong(totalRowsKey, redisTemplate.getConnectionFactory());
            entityIdCounter.getAndIncrement();
            return new ResponseApi("发布成功", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(), post);
        }else {
            return new ResponseApi("发布失败", ErrorCodeEnum.FAIL.getErrorCode(), System.currentTimeMillis(), post);
        }
    }

    /**
     * 查询帖子详情
     * 帖子的回复分为评论：1.对帖子进行评论（comment） 2.对评论进行评论（reply）
     * 首先展示所有的 comment，再针对每个 comment 展示 reply
     */
    @GetMapping("/discussPost/detail")
    public ResponseApi<Map<String,Object>> getDiscussPost(@RequestParam int id, Integer pageOffset,Integer pageLimit, String ticket) {
        Map<String,Object> map = new HashMap<>();

        // 帖子
        DiscussPost post = discussPostService.selectDiscussPostById(id);
        map.put("post",post);

        //当前评论的数目
        map.put("totalCommentRows",post.getCommentCount());

        // 用户
        User user = userServiceFeign.selectById(post.getUserId()).getData();
        map.put("user",user);

        // 点赞数量
        long likeCount = likeServiceFeign.selectEntityLikeCount(Constant.ENTITY_TYPE_POST, id).getData();
        map.put("likeCount",likeCount);

        // 点赞状态,这个功能需要先登录
        LoginTicket loginTicket = null;
        if(StringUtils.hasText(ticket)){
            String ticketKey = RedisKeyUtil.getTicketKey(ticket);
            loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        }
        int likeStatus = loginTicket == null ? 0 : likeServiceFeign.selectEntityLikeStatus(loginTicket.getUserId(), Constant.ENTITY_TYPE_POST, id).getData();
        map.put("likeStatus",likeStatus);

        // 评论列表
        List<Comment> commentList = commentService.selectCommentByEntity(Constant.ENTITY_TYPE_POST, id, pageOffset, pageLimit);

        // 评论Vo列表
        List<Map<String, Object>> commitVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论Vo
                Map<String, Object> commentVo = new HashMap<>();

                // 评论
                commentVo.put("comment", comment);

                // 用户
                User commentUser = userServiceFeign.selectById(comment.getUserId()).getData();
                commentVo.put("user", commentUser);

                // 点赞数量
                likeCount = likeServiceFeign.selectEntityLikeCount(Constant.ENTITY_TYPE_COMMENT, comment.getId()).getData();
                commentVo.put("likeCount", likeCount);

                // 点赞状态
                likeStatus = loginTicket == null ? 0 : likeServiceFeign.selectEntityLikeStatus(loginTicket.getUserId(), Constant.ENTITY_TYPE_COMMENT, comment.getId()).getData();
                commentVo.put("likeStatus", likeStatus);

                // 回复列表（不作分页）
                List<Comment> replyList = commentService.selectCommentByEntity(Constant.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                // 回复Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // 回复Vo
                        Map<String, Object> replyVo = new HashMap<>();

                        // 回复
                        replyVo.put("reply", reply);

                        // 用户
                        User replayUser = userServiceFeign.selectById(reply.getUserId()).getData();
                        replyVo.put("user", replayUser);

                        // 回复的目标
                        User targetUser = reply.getTargetId() == 0 ? null : userServiceFeign.selectById(reply.getTargetId()).getData();
                        replyVo.put("target", targetUser);

                        // 点赞数量
                        likeCount = likeServiceFeign.selectEntityLikeCount(Constant.ENTITY_TYPE_COMMENT, reply.getId()).getData();
                        replyVo.put("likeCount", likeCount);

                        // 点赞状态
                        likeStatus = loginTicket == null ? 0 : likeServiceFeign.selectEntityLikeStatus(loginTicket.getUserId(), Constant.ENTITY_TYPE_COMMENT, reply.getId()).getData();
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys", replyVoList);

                // 回复数量
                Long replyCount = commentService.selectCount(Constant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commitVoList.add(commentVo);
            }
        }
        map.put("commitVoList",commitVoList);
        return new ResponseApi("查询帖子详情",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),map);
    }

    @GetMapping("/discussPost/selectDiscussPostRows")
    public ResponseApi<Integer> selectDiscussPostRows(int userId){
        int rows = discussPostService.selectDiscussPostRows(userId);
        return new ResponseApi("success",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),rows);

    }

    @GetMapping("/discussPost/selectDiscussPosts")
    public ResponseApi<List<DiscussPost>> selectDiscussPosts(int userId, int offset, int limit){
        List<DiscussPost> discussPosts = discussPostService.selectDiscussPosts(userId, offset, limit);
        return new ResponseApi("success",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),discussPosts);
    }

    @GetMapping("/discussPost/selectDiscussPostById")
    public ResponseApi<DiscussPost> selectDiscussPostById(int id){
        DiscussPost discussPosts = discussPostService.selectDiscussPostById(id);
        return new ResponseApi("success",ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),discussPosts);
    }
}
