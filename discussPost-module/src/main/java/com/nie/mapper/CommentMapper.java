package com.nie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nie.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据实体查询评论
     *
     * @param entityType 实体类型 0-帖子  1-评论
     * @param entityId   实体 id
     * @param offset     每页起始行行号
     * @param limit      一页显示多少条数据
     */
    List<Comment> selectCommentByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId,
                                        @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据实体查询评论数
     *
     * @param entityType 实体类型 0-帖子  1-评论
     * @param entityId   实体 id
     */
    int selectCountByEntity(int entityType, int entityId);

    /**
     * 增加评论
     */
    int insertComment(Comment comment);

    /**
     * 根据用户 id 查询评论数
     */
    int selectCountByUserId(int userId);

    /**
     * 根据用户 id 查询评论
     */
    List<Comment> selectCommentByUserId(int userId, int offset, int limit);

}
