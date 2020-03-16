## SpringSecurity应用

### 1.SpringSecurity概览

1. `SpringSecurity`是`Spring`生态中关于安全方面的框架，能够为基于`Spring`的企业应用系统提供声明式的安全访问控制解决方案。
2. `SpringSecurity`是一个基于`SpringAOP`和`Servlet`过滤器的安全框架。提供全面的安全性解决方案，同时在`Web`请求级和方法调用级处理身份确认和授权。
3. `SpringSecurity`提供了一组可以在`Spring`应用上下文中配置的Bean，为应用系统提供声明式的安全访问控制功能。

### 2.搭建SpringSecurity环境

#### 2.1依赖配置

```xml
<!--Spring-Security框架-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

#### 2.2定义访问接口

```java
@RestController
@CrossOrigin
public class AppinfoController {
    
    @RequestMapping(path = "/appinfo",method = RequestMethod.GET)
    public String appinfo() {
        return "AppInfo!";
    }
}
```

- 默认情况下，访问应用中的接口要求输入用户名和密码。用户名默认为user，密码为随机(日志打印出来)。

#### 2.3自定义用户名和密码

```yml
spring:
  security:
    user:
      name: user
      password: 123456
```



### 3.高级应用

####3.1自定义用户角色

- **配置类**

  1. 注解`@EnableWebSecurity`， 开启`SpringSecurity`的功能。

  2. 注解`@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)`

     用来开启security的注解，可以在方法上使用注解`@PreAuthorize`，`@PreFilter`等

  3. 默认情况下，对所有接口都会进行权限验证。

  ```java
  package com.tcl.joker.productboot.config;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
  import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
  import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
  import org.springframework.security.crypto.password.PasswordEncoder;
  
  /**
   * 类 <code>{类名称}</code>{此类功能描述}
   *
   * @author zangbao.lin
   * @version 2020/3/13
   * @since JDK 1.8
   */
  @Configuration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
      /**
       * 配置默认登录页面
       * @param http
       * @throws Exception
       */
      @Override
      protected void configure(HttpSecurity http) throws Exception {
          super.configure(http);
      }
  
  
      /**
       * 配置用户名称密码角色
       * @param auth
       * @throws Exception
       */
      @Override
      protected void configure(AuthenticationManagerBuilder auth) throws Exception {
          auth.inMemoryAuthentication().passwordEncoder(getPasswordEncoder())
                  .withUser("zangbao.lin").password("123456").roles("ADMIN")
                  .and().withUser("jiangdongping").password("123456").roles("USER");
      }
  
  
      /**
       * 密码编码器
       * @return
       */
      @Bean
      public PasswordEncoder getPasswordEncoder() {
          return new PasswordEncoder() {
              @Override
              public String encode(CharSequence charSequence) {
                  return charSequence.toString();
              }
  
              @Override
              public boolean matches(CharSequence charSequence, String s) {
                  return s.equals(charSequence);
              }
          };
      }
  }
  
  ```

  

#### 3.2角色权限控制

- 注解@PreAuthorize，控制访问方法的角色权限

  1. 只有角色为`ADMIN`的账号`zangbao.lin`可以访问，其他角色都会报错。

  ```java
  /**
  * 只允许角色为ADMIN的权限,访问该方法
  **/
  @RestController
  @CrossOrigin
  public class AppinfoController {
      // Spring Security默认的角色前缀是”ROLE_”,使用hasRole方法时已经默认加上了
      @PreAuthorize("hasRole('ADMIN')")
      @RequestMapping(path = "/appinfo",method = RequestMethod.GET)
      public String appinfo() {
          return "AppInfo!";
      }
  }
  ```

  