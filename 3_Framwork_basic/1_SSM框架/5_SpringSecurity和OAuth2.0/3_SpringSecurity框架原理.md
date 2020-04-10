## SpringSecurity框架原理

### 1.初始化过滤器

#### 1.1创建过滤器

- 原理是通过注解`@EnableWebSecurity`，创建过滤器`springSecurityFilterChain`的实例，流程如下：
  
  1. `@EnableWebSecurity`引入了三个配置类，主要是配置类`WebSecurityConfiguration`。
  
     ```java
     @Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
     @Target(value = { java.lang.annotation.ElementType.TYPE })
     @Documented
     @Import({ WebSecurityConfiguration.class, ObjectPostProcessorConfiguration.class,
           SpringWebMvcImportSelector.class })
     @EnableGlobalAuthentication
     @Configuration
     public @interface EnableWebSecurity {
        boolean debug() default false;
     }
     ```
  
  2. `WebSecurityConfiguration`类中创建了过滤器的实例`Bean`。
  
     ```java
     /**
     * 作用是：创建了一个SpringBean,类型为Filter,名称为springSecurityFilterChain.
     **/
     @Configuration
     public class WebSecurityConfiguration implements ImportAware, BeanClassLoaderAware {
         // ......
         private WebSecurity webSecurity;
         private List<SecurityConfigurer<Filter, WebSecurity>> webSecurityConfigurers;
     
         // 创建一个名为springSecurityFilterChain的Filter
         @Bean(name = AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
         public Filter springSecurityFilterChain() throws Exception {
             boolean hasConfigurers = webSecurityConfigurers != null
                 && !webSecurityConfigurers.isEmpty();
             if (!hasConfigurers) {
                 WebSecurityConfigurerAdapter adapter = objectObjectPostProcessor
                     .postProcess(new WebSecurityConfigurerAdapter() {
                     });
                 webSecurity.apply(adapter);
             }
             return webSecurity.build();
         }
     
         // ......
     }
     ```
  
  3. 此过滤器的具体类型为`FilterChainProxy`，就是Security的入口。

####1.2注册过滤器

- 在`servlet3.0`规范中，提供了一个`javax.servlet.ServletContainerInitializer接口` ，用来动态注册`Servelt、Filter、Listener`等。

  ```java
  // 注册的方法就是onStartup
  public interface ServletContainerInitializer {
      public void onStartup(Set<Class<?>> c, ServletContext ctx)
          throws ServletException; 
  }
  ```

- **注册过滤的具体流程**

  1. `servlet`容器实现了接口`ServletContainerInitializer`，具体类为`SpringServletContainerInitializer`。 
  2. 在`servlet`容器的`onStartup()`方法中，调用了`SpringSecurity`的`AbstractSecurityWebApplicationInitializer` 类初始化方法。
  3. 在`Security`的初始化方法中，过滤器实例`springSecurityFilterChain`传入到了`servletContext`中，完成注册。
  4. SpringWeb再通过代理类`DelegatingFilterProxy`，访问入口过滤器`FilterChainProxy`。

#### 1.3添加业务过滤器

- 通过继承配置类`WebSecurityConfigurerAdapter`，可以控制过滤器的行为，从而达到具体权限控制方式。

  ```java
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
      protected void configure(HttpSecurity http) throws Exception {
  		logger.debug("Using default configure(HttpSecurity). 
  		If subclassed this will potentially override subclass configure(HttpSecurity).");
   
  		http
               // 1.要求访问应用的所有用户都要被验证        
  			.authorizeRequests().anyRequest().authenticated()
  	         	.and()
               // 2.允许所有用户可以通过表单进行验证
  			.formLogin()
               	.and()
               // 3.允许所有请求通过Http Basic验证        
  			.httpBasic();
  	}
  }
  ```

  1. 通过`HttpSecurity`调用方法来配置功能，本质上是将对应的业务过滤器，添加到`HttpSecurity`过滤链中。
     - `authorizeRequests()` ，对应的是`FilterSecurityInterceptor` 。
     - `formLogin()` ，对应的是`UsernamePasswordAuthenticationFilter`。 
     - `httpBasic()` ，对应的是`BasicAuthenticationFilter` 。
  2. 当请求进来时，就会在过滤链中流转，进行认证和鉴权。



#### 1.4过滤器作用

1. **WebAsyncManagerIntegrationFilter**
   - 将`Security`上下文与`Spring Web`中用于处理异步请求映射的`WebAsyncManager`进行集成。

2. **SecurityContextPersistenceFilter**
   - 在每次请求处理之前将该请求相关的安全上下文信息加载到`SecurityContextHolder`中。
   - 然后在该次请求处理完成之后，将`SecurityContextHolder`中关于这次请求的信息存储到一个"仓储"中，然后将`SecurityContextHolder`中的信息清除。
   - 例如在`Session`中维护一个用户的安全信息就是这个过滤器处理的。

3. **HeaderWriterFilter**
   - 用于将头信息加入响应中。

4. **CsrfFilter**
   - 用于处理跨站请求伪造。

5. **LogoutFilter**
   - 用于处理退出登录。

6. **DefaultLoginPageGeneratingFilter**
   - 如果没有配置登录页面，那系统初始化时就会配置这个过滤器，并且用于在需要时生成一个登录表单页面。

7. **UsernamePasswordAuthenticationFilter**
   - 用于处理基于表单的登录请求，从表单中获取用户名和密码。默认情况下处理来自`"/login"`的请求。
   - 从表单中获取用户名和密码时，默认使用的表单`name`值为`"username"`和`"password"`，这两个值可以通过设置这个过滤器的`usernameParameter`和`passwordParameter`两个参数的值进行修改。

8. **BasicAuthenticationFilter**
   - 检测和处理`http basic`认证。

9. **RequestCacheAwareFilter**
   - 用来处理请求的缓存。

10. **SecurityContextHolderAwareRequestFilter**
    - 主要是包装请求对象`request`。

11. **AnonymousAuthenticationFilter**
    - 检测`SecurityContextHolder`中是否存在`Authentication`对象，如果不存在为其提供一个匿名`Authentication`。

12. **SessionManagementFilter**
    - 管理`session`的过滤器。

13. **ExceptionTranslationFilter**
    - 处理`AccessDeniedException`和`AuthenticationException`异常。

14. **FilterSecurityInterceptor**
    - 可以看做过滤器链的出口。

15. **RememberMeAuthenticationFilter**
    - 当用户没有登录而直接访问资源时, 从`cookie`里找出用户的信息,如果`Spring Security`能够识别出用户提供的`remember me cookie`,用户将不必填写用户名和密码,而是直接登录进入系统，该过滤器默认不开启。

#### 1.5过滤器流转过程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/6_SpringSecurity流程图.png)

### 2.表单的认证过程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_Security认证流程.png)

#### 2.1请求进入UsernamePasswordAuthenticationFilter

```java
/**
* 请求进入到父类的doFilter(),然后跳转到UsernamePasswordAuthenticationFilter.attemptAuthentication()
**/
public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
    if (postOnly && !request.getMethod().equals("POST")) {
        throw new AuthenticationServiceException(
            "Authentication method not supported: " + request.getMethod());
    }

    String username = obtainUsername(request);
    String password = obtainPassword(request);

    if (username == null) {
        username = "";
    }

    if (password == null) {
        password = "";
    }

    username = username.trim();

    /**
    * 1.根据用户输入的用户名、密码构建了UsernamePasswordAuthenticationToken,
    *   并将其交给AuthenticationManager来进行认证处理.此时的状态是未认证.
    **/
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
        username, password);

    setDetails(request, authRequest);

    /**
    * 2.将Token交给AuthenticationManager进行认证处理.
    * 3.AuthenticationManager是用来管理所有AuthenticationProvider,选择合适的Provider进行认证.
    **/
    return this.getAuthenticationManager().authenticate(authRequest);
}
```



#### 2.2AuthenticationManager处理用户Token

```java
/**
* ProviderManager是接口AuthenticationManager的实现类,用来管理AuthenticationProvider
* 1. 不同的登录逻辑的认证方式是不一样的,比如表单需要验证用户名密码,使用三方登录时就不需要验证密码.
* 2. SpringSecurity支持多种认证逻辑,每一种认证逻辑的认证方式其实就是一种AuthenticationProvider.
**/
public class ProviderManager implements AuthenticationManager,.... {
    public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
        Class<? extends Authentication> toTest = authentication.getClass();
        AuthenticationException lastException = null;
        Authentication result = null;
        boolean debug = logger.isDebugEnabled();

        // 1.getProviders()获取所有的AuthenticationProvider
        for (AuthenticationProvider provider : getProviders()) {
            if (!provider.supports(toTest)) {
                continue;
            }

            if (debug) {
                logger.debug("Authentication attempt using "
                             + provider.getClass().getName());
            }

            try {
                // 2.调用provider的方法authenticate()进行认证
                result = provider.authenticate(authentication);

                if (result != null) {
                    copyDetails(authentication, result);
                    break;
                }
            }
            // 省略....
        }
}
    
/**
* AuthenticationProvider：认证逻辑的抽象
**/
public interface AuthenticationProvider {
    // 具体的认证逻辑
    Authentication authenticate(Authentication authentication) throws AuthenticationException;
    // 判断provider是否支持当前的认证逻辑
    boolean supports(Class<?> authentication);
}
```



#### 2.3Provider进行具体的表单认证处理

```java
/**
* AbstractUserDetailsAuthenticationProvider抽象类负责表单认证
**/
public abstract class AbstractUserDetailsAuthenticationProvider implements AuthenticationProvider {
    // ......

    public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
        // ......
        
        // 1.通过方法retrieveUser()读取到用户信息,返回一个UserDetails对象
        user = retrieveUser(username,(UsernamePasswordAuthenticationToken) authentication);
        // ......
        
        try {
            // 2.第一次校验,前校验
            preAuthenticationChecks.check(user);
            // 3.第二次校验,附加认证校验
            additionalAuthenticationChecks(
                user,(UsernamePasswordAuthenticationToken) authentication);
        }
        
        // 4.第三次校验,后校验
        postAuthenticationChecks.check(user);
        
        // 5.返回认证信息,返回的是一个Authentication对象
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }
    
    // ......
}


/**
* 1.DaoAuthenticationProvider类,实现了方法retrieveUser(),是获取用户的具体逻辑
**/
public class DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    protected final UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        prepareTimingAttackProtection();
        try {
            // 1.使用UserDetailsService类进行验证用户名,可以自定义实现,重写方法loadUserByUsername()
            UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
        }
        // ...
	}
}

/**
* 2.DefaultPreAuthenticationChecks类,实现前校验
**/
public class DefaultPreAuthenticationChecks implements UserDetailsChecker {
    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {
        public void check(UserDetails user) {
            // 是否被锁
            if (!user.isAccountNonLocked()) {}
            // 是否可用
            if (!user.isEnabled()) {}
            // 是否过期
            if (!user.isAccountNonExpired()) {}
        }
    }
}

/**
* 3.DaoAuthenticationProvider类,实现附加校验
**/
public class DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    protected void additionalAuthenticationChecks(
        UserDetails userDetails,UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        // 校验密码是否为空
        if (authentication.getCredentials() == null) {...}
        
        String presentedPassword = authentication.getCredentials().toString();
        // 调用passwordEncoder编码器校验密码
        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {...}
    }
}

/**
* 4.DefaultPostAuthenticationChecks类,实现校验密码是否过期
**/
public class DefaultPostAuthenticationChecks implements UserDetailsChecker {
    public void check(UserDetails user) {
        // 校验密码是否过期
        if (!user.isCredentialsNonExpired()) {}
    }
}

/**
* 5.返回一个成功的认证信息，封装成对象Authentication
**/
public class AbstractUserDetailsAuthenticationProvider implements AuthenticationProvider,... {
    protected Authentication createSuccessAuthentication(
        Object principal,Authentication authentication, UserDetails user) {

        // 重新封装一个TOKEN,并将认证状态设置为已认证
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
            principal, authentication.getCredentials(),
            authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());

        return result;
    }
}
```



#### 2.4返回认证信息到UsernamePasswordAuthenticationFilter 

```java
/**
* AbstractAuthenticationProcessingFilter过滤器,对认证返回的Authentication对象进行后置处理
**/
public class AbstractAuthenticationProcessingFilter implements ... {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
        // .....

        Authentication authResult;
        try {
            // 1.获取认证信息,Authentication对象
            authResult = attemptAuthentication(request, response);
        }
        catch (InternalAuthenticationServiceException failed) {
            // 2.认证失败执行的处理器,默认为SimpleUrlAuthenticationFailureHandler
            unsuccessfulAuthentication(request, response, failed);
            return;
        }
        // ...

        // 3.认证成功执行的处理器,默认为SavedRequestAwareAuthenticationSuccessHandler
        successfulAuthentication(request, response, chain, authResult);
    }
}
```



#### 2.5将认证成功的信息放入Session

```java
/** 
* AbstractAuthenticationProcessingFilter过滤器,对认证成功信息的后置处理
**/
public class AbstractAuthenticationProcessingFilter implements ... {
    protected void successfulAuthentication(
        HttpServletRequest request,HttpServletResponse response, 
        FilterChain chain, Authentication authResult) throws IOException, ServletException {

        // 1.将Authentication放入SecurityContext,SecurityContext是保存在session的,进而放到Session中.
        SecurityContextHolder.getContext().setAuthentication(authResult);

        rememberMeServices.loginSuccess(request, response, authResult);

        // Fire event
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(
                authResult, this.getClass()));
        }

        // 2.SavedRequestAwareAuthenticationSuccessHandler处理器,实现重定向跳转
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }
}
```

- **多个请求共享认证信息**

  1. 认证信息`Authentication`对象，存放在`SecurityContext`中，进而放入到`Session`中。

  2. 一个HTTP请求和响应都是在一个线程中执行，因此在整个处理的任何一个方法中都可以通过 `SecurityContextHolder.getContext()`来取得存放进去的认证信息。

  3. 当多个请求时，通过`SecurityContextPersistenceFilter`过滤器来处理。

     - 当请求时，检查`Session`中是否存在`SecurityContext`，如果有将其放入到线程中。
     - 当响应时，检查线程中是否存在`SecurityContext`，如果有将其放入到`Session`中。

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_Security过滤器链.png)



### 3.授权过程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_授权流程.png)

#### 3.1授权的具体流程

1. 请求进入过滤器之后，如果是登录地址则会跳转到表单认证。认证成功之后，将`Authentication`对象放入`Session`中，然后跳转到登录成功的页面。
2. 如果是其他请求进入`Web`应用后，`SecurityContextPersistenceFilter`过滤器会检查`session`中是否含有认证信息。
3. 如果有成功认证的`Authentication`对象，则会进入`ExceptionTranslationFilter`和`FilterSecurityInterceptor`过滤器，进入鉴权流程。
4. 鉴权的主要处理，是在`FilterSecurityInterceptor`过滤器。
5. 调用`AccessDecisionManager`进行具体的鉴权逻辑，其又委托给`AccessDecisionVoter`进行处理。
6. `AccessDecisionVoter`用来判断，定义保护的资源是否和`UserDetail`中用户权限是否一致。
7. 当权限验证一致的时候，`AccessDecisionVoter`就会允许用户访问用权限保护资源。
8. 在`AccessDecisionManager`中，有三个执行策略，默认为`AffirmativeBased`。
   - `AffirmativeBased`-任意1个通过授权成功
   - `ConsensusBased`-部分通过授权成功
   - `UnanimousBased`-全部通过授权成功

#### 3.2授权的过滤器

- **异常过滤器`ExceptionTranslationFilter`**

  ```java
  /**
  * ExceptionTranslationFilter作用是,用来捕获下一个Filter抛出的异常
  **/ 
  public class ExceptionTranslationFilter extends GenericFilterBean {
      public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
          throws IOException, ServletException {
          HttpServletRequest request = (HttpServletRequest) req;
          HttpServletResponse response = (HttpServletResponse) res;
  
          try {
              // 捕获下一个Filter抛出的异常,FilterSecurityInterceptor
              chain.doFilter(request, response);
  
              logger.debug("Chain processed normally");
          }
          catch (IOException ex) {......}
          catch (Exception ex) {......}
          }
      }
  }
  ```

  

- **授权过滤器`FilterSecurityInterceptor`** 

  ```java
  /**
  * FilterSecurityInterceptor负责将过滤产生的认证信息从当前请求上下文中取出来,对请求的资源做权限判断,
  * 如果无权访问相应的资源,则抛出spring-security异常,由ExceptionTranslationFilter进行处理.
  **/
  public class FilterSecurityInterceptor extends AbstractSecurityInterceptor 
      implements Filter {
      // 过滤方法,将请求委托给FilterInvocation执行
      public void doFilter(ServletRequest request, ServletResponse response,
                           FilterChain chain) throws IOException, ServletException {
          FilterInvocation fi = new FilterInvocation(request, response, chain);
          invoke(fi);
      }
  
      // 权限判断的方法
      public void invoke(FilterInvocation fi) throws IOException, ServletException {
          if ((fi.getRequest() != null){.....}
             
          else {
              
              if (fi.getRequest() != null && observeOncePerRequest) {
                  fi.getRequest().setAttribute(FILTER_APPLIED, Boolean.TRUE);
              }
  
              // 1.进行鉴权逻辑,对实际请求进行调用前的处理
              InterceptorStatusToken token = super.beforeInvocation(fi);
  
              try {
                  // 过滤链继续执行
                  fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
              }
              finally {
                  // 2.AbstractSecurityInterceptor进行清理工作
                  super.finallyInvocation(token);
              }
  
              // 3.完成清理工作后的回调方法
              super.afterInvocation(token, null);
          }
      }
  }
  ```

- **鉴权逻辑`AbstractSecurityInterceptor`**

  ```java
  public class AbstractSecurityInterceptor implements ... {
      protected InterceptorStatusToken beforeInvocation(Object object) {
          // 1.获取Request的相关属性配置信息
          Collection<ConfigAttribute> attributes = this.obtainSecurityMetadataSource()
              .getAttributes(object);
  
          // 校验属性配置是否为空.
          if (attributes == null || attributes.isEmpty()) {
              
              // 2.发布事件
              publishEvent(new PublicInvocationEvent(object));
              return null; 
          }
  
  
          // 3.从SecurityContext中获取Authentication认证信息,并且进行空校验
          if (SecurityContextHolder.getContext().getAuthentication() == null) {
              credentialsNotFound(messages.getMessage(
                  "AbstractSecurityInterceptor.authenticationNotFound",
                  "An Authentication object was not found in the SecurityContext"),
                                  object, attributes);
          }
  
          // 4.校验Authentication是否需要重新认证.
          Authentication authenticated = authenticateIfRequired();
  
          try {
              // 5.委托AccessDecisionManager进行鉴权
              this.accessDecisionManager.decide(authenticated, object, attributes);
          }
          catch (AccessDeniedException accessDeniedException) {
              publishEvent(new AuthorizationFailureEvent(object, attributes, authenticated,
                                                         accessDeniedException));
  
              throw accessDeniedException;
          }
          
          // ... 后置处理逻辑
  
      }
  }
  ```

#### 3.3决策管理器`AccessDecisionManager`

- **工作原理**

  1. `AccessDecisionManager`接口中，`decide()`方法作用是调用`AccessDecisionVoter`进行权限判定(投票)。
  2. 如果权限判断通过，则表示鉴权成功，可以继续访问。否则失败返回。

- **`AccessDecisionManager`的实现类**

  1. **AffirmativeBased一票通过**

     当前只要存在任何一个投了赞同票的，`AccessDecisionVoter`便会最终给予相关授权。

  2. **UnanimousBased一票否决**

     只要任意一个`AccessDecisionVoter`投出了反对票，则无论有多少个赞同票都无法授权访问权限。

  3. **ConsensusBased少数服从多数**

     通过或拒绝，以投票数多的为准。

- **`AccessDecisionVoter`投票器类**

  1. 作用是进行具体的权限判定，可以自己进行扩展。
  
  2. 例如，从`Authentication`对象中取出允许访问的接口`URL`，和请求中的接口`URL`进行对比。
  
     

