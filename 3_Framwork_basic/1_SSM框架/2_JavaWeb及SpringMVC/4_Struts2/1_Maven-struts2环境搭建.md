## Maven-struts2环境搭建

### 1.创建Maven-Web项目

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/0_搭建web项目步骤.png)

### 2.Pom.xml文件配置

```xml
<!-- struts2核心包 -->
<dependency>
    <groupId>org.apache.struts</groupId>
    <artifactId>struts2-core</artifactId>
    <version>2.5.22</version>
</dependency>
```

### 3.web.xml文件配置struts2过滤器，Tomcat读取

```xml
<!DOCTYPE web-app PUBLIC
 "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
 "http://java.sun.com/dtd/web-app_2_3.dtd" >
<web-app>
  <display-name>Archetype Created Web Application</display-name>
  <!--引入核心过滤器-->
  <filter>
    <filter-name>struts2</filter-name>
    <filter-class>org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter</filter-class>
      	<!-- 修改配置文件位置 -->
     	<init-param> 
          <param-name>config</param-name> 
          <param-value>struts-default.xml,struts-plugin.xml,struts/struts.xml</param-value>
    	</init-param>
  </filter>
    
  <filter-mapping>
    <filter-name>struts2</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
    
  <welcome-file-list>
    <welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
</web-app>
```



### 4.开发Action-类似于Controller

- Action类 - LoginAction

```java
package com.huan.struts.action;

public class LoginAction {
    public String success(){
        System.out.println("成功访问action，请求正在处理中");
        System.out.println("调用service");
        return "success";
    }
}
```

- 跳转页面 - success.jsp 

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>success成功跳转到该页面</title>
</head>
<body>
</body>
</html>
```



### 5.struts.xml文件配置，Struts2读取

- IDEA配置Strut2模块

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_IDEA配置Strut2模块.png)

- 配置文件位置
  1. 默认位置，/src/main/resource/struts.xml
  2. 可选位置,在web.xml文件中配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE struts PUBLIC
        "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"
        "http://struts.apache.org/dtds/struts-2.0.dtd">
<struts>
    
    <package name="helloworld" extends="struts-default">
        <action name="helloworld" class="com.joker.action.LoginAction"
                method="success">
            <result name="success">/WEB-INF/jsp/success.jsp</result>
        </action>

    </package>

</struts>
```

