package com.nie.utils;

public class Constant {

    /**
     * 激活成功
     */
    public static int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    public static int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    public static int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超市时间（12小时）
     */
    public static int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态下的登录凭证的超时时间（3个月）
     */
    public static int REMEMBER_EXPIRED_SECONDS = 3600 * 12 * 100;

    /**
     * 实体类型：帖子
     */
    public static int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论
     */
    public static int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户
     */
    public static int ENTITY_TYPE_USER = 3;

}
