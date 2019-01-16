package com.lin.shiro.config;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Created by baozang Cotter on 2018/11/27.
 * function:自定义的权限验证
 */
public class MyShiroRealm extends AuthorizingRealm {

    //配置角色和权限（为登录的用户配置响应的角色和权限）
    //角色和权限会在 访问的url进行控制
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        System.out.println("--------权限配置-------");
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        String name = (String) principals.getPrimaryPrincipal();
        try {
            //注入角色（该用户所属的角色）
            authorizationInfo.addRole("admin");
            //注入权限（该用户的权限）
            authorizationInfo.addStringPermission("create");
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return authorizationInfo;
    }

    //用户认证(验证账号密码是否正确)
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken utoken = (UsernamePasswordToken)token;

        //获取用户名
        String name = utoken.getUsername();
        if(name == null) {
            return null;
        }
        //这里需要一个步骤
        //验证该用户的用户名对应的密码是否正确
        //输入的password根据token来获取
        //正确的password根据数据库来获取

        //验证authenticationToken和simpleAuthenticationInfo的信息
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(name,"123456",getName());
        return simpleAuthenticationInfo;
    }

}
