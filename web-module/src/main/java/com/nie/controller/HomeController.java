package com.nie.controller;

import com.nie.entity.LoginTicket;
import com.nie.entity.Page;
import com.nie.entity.User;
import com.nie.result.ResponseApi;
import com.nie.service.DiscussPostFeign;
import com.nie.service.UserLoginFeign;
import com.nie.service.UserServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostFeign discussPostFeign;

    @Autowired
    private UserLoginFeign userLoginFeign;

    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * 社区首页，展示贴子列表
     * 方法调用前，SpringMVC 会自动实例化 Model 和 Page，并将 Page 注入 Model，
     * 所以，在 thymeleaf 中可以直接访问 Page 对象中的数据，不需要再 model.addAttribute() 方法。
     * -------
     * userId 为 0 表示： select * from user
     * userId 不为 0 表示：select * from user where user_id = #{userId}
     * 该做法是为了在用户个人主页上可以查询到某个用户发布的帖子
     */
    @GetMapping({"/index", "/"})
    public String index(Model model, Page page, @CookieValue(value = "ticket",required = false) String ticket) {
        // 判断用户是否登录
        User loginUser = null;
        LoginTicket loginTicket =  userLoginFeign.isLogin(ticket).getData();
        if(StringUtils.hasText(ticket) &&  loginTicket != null){
            loginUser = userServiceFeign.selectById(loginTicket.getUserId()).getData();
        }
        model.addAttribute("loginUser",loginUser);

        // 查询帖子
        ResponseApi<Map<String,Object>> index = discussPostFeign.index(page.getOffset(),page.getLimit());
        Map<String,Object> map = index.getData();
        int totalRows = (int) map.get("totalRows");
        page.setRows(totalRows);
        page.setPath("/index");
//        model.addAttribute("page",page);

        List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("list");
        model.addAttribute("discussPosts", list);
        return "index";
    }

    @GetMapping("/error")
    public String toErrorPage() {
        return "error/500";
    }
}
