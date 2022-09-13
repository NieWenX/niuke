package com.nie.service;

import com.nie.entity.User;
import com.nie.result.ResponseApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "gateway-server")
public interface UserServiceFeign {
    //通过用户id获得用户
    @GetMapping("/user/selectById")
    public ResponseApi<User> selectById(@RequestParam("userId") Integer userId);

}
