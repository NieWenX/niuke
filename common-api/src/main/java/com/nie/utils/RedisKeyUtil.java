package com.nie.utils;

public class RedisKeyUtil {

    // 分隔符
    private static final String SPLIT = ":";
    // 点赞实体
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 点赞用户
    private static final String PREFIX_USER_LIKE = "like:user";
    // 关注的目标
    private static final String PREFIX_FOLLOWEE = "followee";
    // 被关注目标的粉丝
    private static final String PREFIX_FOLLOWER = "follower";
    // 登录验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";
    // 用户
    private static final String PREFIX_USER = "user";
    // 总的帖子数
    private static final String PREFIX_TOTALROWS = "DiscussPostTotalRows";
    // 当前帖子的评论数
    private static final String PREFIX_CommentRows = "DiscussPostCommentRows";

    /**
     * 某个实体的赞
     * like:entity:entityType:entityId -> set(userId)
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户的赞
     * like:user:userId -> int
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体(某个用户关注了某个实体),存在有序列表 zset 中,以当前时间排序
     * followee:userId:entityType -> zset(entityId, now)
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个用户拥有的粉丝
     * follower:entityType:entityId -> zset(userId, now)
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 登录验证码
     *
     * @param owner 验证码的临时凭证(随机字符串)
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登录凭证
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 用户
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     *
     * @return 返回一个当前所有帖子的数目
     */
    public static String getDiscussPostTotalRowsKey(){return  PREFIX_TOTALROWS;}

    /**
     *
     * @return 返回一个当前帖子的评论数
     */
    public static String getDiscussPostCommentRowsKey(int postId){return  PREFIX_CommentRows + SPLIT + postId;}
}
