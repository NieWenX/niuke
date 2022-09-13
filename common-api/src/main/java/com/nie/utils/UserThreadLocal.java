package com.nie.utils;


import com.nie.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户的信息，用于代替 Session 对象
 */
@Component
public class UserThreadLocal {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUsers(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }

}
