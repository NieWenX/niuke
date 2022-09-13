package com.nie.controller;

import com.nie.entity.User;
import com.nie.result.ErrorCodeEnum;
import com.nie.result.ResponseApi;
import com.nie.service.UserService;
import com.nie.service.UserServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServicesController implements UserServiceFeign {

    @Autowired
    private UserService userService;

    @Override
    public ResponseApi<User> selectById(Integer userId) {
        User user = userService.selectById(userId);
        return new ResponseApi("通过userId获得用户", ErrorCodeEnum.SUCCESS.getErrorCode(), System.currentTimeMillis(),user);
    }


}
