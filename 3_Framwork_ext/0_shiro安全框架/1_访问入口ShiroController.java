package com.lin.shiro.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by baozang Cotter on 2018/11/28.
 * function:登录页面
 */
@RestController
public class ShiroController {
    @GetMapping("/login")
    public String login(){
        return "need login";
    }

    //登录
    @GetMapping("/doLogin")
    public String doLogin(String uid, String pwd){
        //添加用户认证信息
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(uid,pwd);
        try{
            //进行验证，这里可以捕获异常，然后返回对应信息
            subject.login(token);
        }
        catch(Exception e){
            return "login failed";
        }

        return "login success";
    }

    @RequestMapping(value = "/index")
    public String index(){
        return "index";
    }

    //登出
    @RequestMapping(value = "/logout")
    public String logout(){
        return "logout";
    }

    //错误页面展示
    @GetMapping("/error")
    public String error(){
        return "error ok!";
    }

    @RequiresRoles("admin")//限制可以访问的角色
    @RequiresPermissions("create")//限制可以访问的权限
    @RequestMapping(value = "/create")
    public String create(){
        return "Create success!";
    }

    @RequiresRoles("admin")
    @RequiresPermissions("detail")
    @RequestMapping(value = "/detail")
    public String detail(){
        return "uid";
    }
}
