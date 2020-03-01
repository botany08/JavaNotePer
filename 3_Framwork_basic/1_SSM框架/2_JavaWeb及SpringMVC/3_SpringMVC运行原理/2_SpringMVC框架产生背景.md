## SpringMVC的构成要素

- 基于框架所编写的应用程序的构成要素-搭建框架需要哪几个部分
  1. 入口程序(Tomcat加载) —— DispatcherServlet
  2. 核心配置(DI加载) —— [servlet-name]-servlet.xml
  3. 控制逻辑(客户端请求触发) —— UserController

### 1.指定SpringMVC的入口程序（在web.xml中）

```xml
<!-- 供Tomcat读取,加载DispatcherServlet类 -->
<servlet>  
    <servlet-name>dispatcher</servlet-name>  
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>  
    <load-on-startup>1</load-on-startup>  
</servlet>  
          
<servlet-mapping>  
    <servlet-name>dispatcher</servlet-name>  
    <url-pattern>/**</url-pattern>  
</servlet-mapping> 
```



### 2.编写SpringMVC的核心配置文件（在[servlet-name]-servlet.xml中）

```xml
<!-- 供DispatcherServlet类读取,加载SpringMVC中的配置,如指定默认Bean -->
<beans xmlns="http://www.springframework.org/schema/beans"  
       xmlns:mvc="http://www.springframework.org/schema/mvc"  
       xmlns:context="http://www.springframework.org/schema/context"  
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
       xsi:schemaLocation="  
            http://www.springframework.org/schema/beans  
            http://www.springframework.org/schema/beans/spring-beans-3.1.xsd  
            http://www.springframework.org/schema/context   
            http://www.springframework.org/schema/context/spring-context-3.1.xsd  
            http://www.springframework.org/schema/mvc  
            http://www.springframework.org/schema/mvc/spring-mvc-3.1.xsd"   
       default-autowire="byName">  
      
    <!-- Enables the Spring MVC @Controller programming model -->  
    <mvc:annotation-driven />  
      
    <context:component-scan base-package="com.demo2do" />  
      
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">  
        <property name="prefix" value="/" />  
        <property name="suffix" value=".jsp" />  
    </bean>  
      
</beans>  
```



### 3.编写控制(Controller)层的代码

```java
// 业务需求主要处理逻辑代码
@Controller  
@RequestMapping  
public class UserController {  
    @RequestMapping("/login")  
    public ModelAndView login(String name, String password) {  
       // write your logic here   
           return new ModelAndView("success");  
    }  
  
}  
```



## SpringMVC的发展历程

- 不同MVC框架的比较处理

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/6_MVC框架的设计.png)

### 1.核心Servlet的提炼-DispatcherServlet核心分发器

#### 1.1Servlet模型中的请求-响应实现

##### 1.1.1请求-响应的映射实现

- 配置Servlet及其映射关系（在web.xml中）

```xml
<!-- servlet-name则将servlet节点,和servlet-mapping节点联系在一起形成请求-响应的映射关系 -->
<servlet>  
    <servlet-name>registerServlet</servlet-name>
    <!-- 定义了具体进行响应的Servlet实现类 -->
    <servlet-class>com.demo2do.springmvc.web.RegisterServlet</servlet-class>  
    <load-on-startup>1</load-on-startup>  
</servlet>  

<servlet-mapping>  
    <servlet-name>registerServlet</servlet-name>
    <!-- 1.定义了整个请求-响应的映射载体,客户端请求匹配到url-pattern -->
    <url-pattern>/register</url-pattern>  
</servlet-mapping>  
```

##### 1.1.2响应的逻辑实现

- 在Servlet实现类中完成响应逻辑

```java
public class RegisterServlet extends  HttpServlet {  
     @Override  
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
         throws ServletException, IOException {  
      
         // 1.从请求对象中获取信息  
         String name = req.getParameter("name");  
         String birthdayString = req.getParameter("birthday");  
           
         // 2.进行业务逻辑处理
         ......
             
         // 3.设置返回数据  
         request.setAttribute("user", user);  
 
         // 4.返回成功页面  
         req.getRequestDispatcher("/success.jsp").forward(req, resp);  
     }  
}  
```

#### 1.2Servlet规范中请求-响应实现的总结

##### 1.2.1控制流和数据流

- 控制流
  1. 请求-响应的映射关系的定义 -- 在web.xml配置文件中对Http请求进行处理
  2. 控制程序对于Http响应的处理
- 数据流
  1. 控制程序对于请求数据的处理 -- 在doGet或doPost方法中处理
  2. 控制程序对于响应数据的处理 -- 在doGet或doPost方法中处理

##### 1.2.2SpringMVC中的解决方法

- 提炼一个核心的Servlet覆盖对所有Http请求的处理，减小对web.xml文件的依赖。
- 将`url-servlet`一对一的关系，转化为`url-method`一对一。
- 定义了对所有Http请求进行规范化处理的流程。

### 2.组件的引入-SpringMVC规范化的处理流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_SpirngMVC的设计.png)

#### 2.1处理流程规范化

- 将处理流程划分为若干个步骤（任务），并使用一条明确的逻辑主线将所有的步骤串联起来。
  1. 对`Http`请求进行初步处理，查找与之对应的`Controller`处理类（方法）
  2. 调用相应的`Controller`处理类（方法）完成业务逻辑
  3. 对`Controller`处理类（方法）调用时可能发生的异常进行处理
  4. 根据`Controller`处理类（方法）的调用结果，进行Http响应处理

####2.2处理流程组件化

- 将处理流程中的每一个步骤（任务）都定义为接口，并为每个接口赋予不同的实现模式。
  1. HandlerMapping -- 匹配后端处理器
  2. HandlerAdapter -- 执行后端处理器
  3. HandlerExceptionResolver -- 异常处理
  4. ViewResolver -- 视图渲染

#### 2.3组件管理 -- 实例化或调用接口

- SpringMVC对于已定义组件的管理，借用了Spring自身已经提供的容器功能。

- SpringMVC在进行组件管理时，会单独为SpringMVC相关的组件构建一个容器环境，这一容器环境可以独立于应用程序自身所创建的Spring容器。

- SpringMVC对这些组件的管理载体，通过核心配置文件来完成的，也可以通过注解来完成。

  

### 3.行为模式的扩展

####3.1扩展的方式

-  通过接口的不同实现类，来进行不同功能的实现。
  1. 一个接口的每一个不同的实现分支，代表了相同操作规范的不同行为模式。
  2. SpringMVC各种不同的组件实现体系成为了SpringMVC行为模式扩展的有效途径。

##SpringMVC的设计原则

### 1.拒绝修改，鼓励扩展

#### 1.1使用final关键字来限定核心组件中的核心方法,禁止重写

- `HandlerAdapter`实现类`RequestMappingHandlerAdapter`中，核心方法`handleInternal`就被定义为`final`

#### 1.2大量地在核心组件中使用private方法,防止子类继承调用

- 默认的`HandlerAdapter`实现`RequestMappingHandlerAdapter`，几乎所有的核心处理方法全部被定义成了带有红色标记的`private`方法。

#### 1.3限定某些类对外部程序不可见

- 不允许外部程序对这些系统配置类进行访问，从而杜绝外部程序对`SpringMVC`默认行为的任何修改。 

####1.4提供自定义扩展接口，却不提供完整覆盖默认行为的方式

- 类似于`HttpServlet`中提供了`doGet()`方法扩展，但`Servlet`接口中的`Service()`方法执行不受扩展影响。

  `SpringMVC`提供的扩展切入点无法改变框架默认的行为方式。

### 2.执行主线固定，行为模式多样

- 在SpringMVC版本更改过程中，整个DispatcherServlet的核心处理主线没有变化，默认实现类发生了改变。

### 3.简化配置

- 利用注解代替XML配置

