package cn.v5cn.ss.demospringsecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @GetMapping("/")
    public Map<String, String> hello(HttpServletRequest request) {
        // 返回map会变成JSON key value方式
        Map<String,String> map=new HashMap<String,String>();
        map.put("content", "hello freewolf~ " + request.getSession().getId());
        return map;
    }
}
