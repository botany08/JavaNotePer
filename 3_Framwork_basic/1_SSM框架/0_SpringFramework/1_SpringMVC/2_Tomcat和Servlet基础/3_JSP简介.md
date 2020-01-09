## JSP简介

### 1.JSP的定义

JSP全称Java Server Page，就是“运行在服务器端的页面”。JSP = HTML + Java片段（各种标签本质上还是Java片段） 

- 请求JSP过程的资源转换

  当请求JSP时，服务器内部会经历一次动态资源(JSP)到静态资源(HTML)的转化，服务器会自动帮我们把JSP中的HTML片段和数据拼接成静态资源响应给浏览器。 

  

### 2.Servlet的定义

一个Java类，运行在Servlet容器中(Tomcat)。 负责接收请求，调用Service处理数据，负责响应数据 。

- Http请求的处理流程

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/5_Http请求的处理流程.png)

### 3.JSP的本质

 JSP本质就是一个Servlet。 

- 处理流程

  1. WEB容器接收到以.jsp为扩展名的URL的访问请求时，将把该请求交给JSP引擎去处理。Tomcat中的JSP引擎就是一个Servlet程序，它负责解释和执行JSP页面。 
  2. 每个JSP 页面在第一次被访问时，JSP引擎将它翻译成一个Servlet源程序，接着再把这个Servlet源程序编译成Servlet的class类文件 。
  3. 再由Web容器像调用普通Servlet程序一样的方式来装载和解释执行这个由JSP页面翻译成的Servlet程序。
  4. 最后处理完成后，会往响应报文中写入HTML页面，这个页面由JSP转化而来。 

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/6_JSP执行流程.png)

- 存放位置

  1.  Tomcat把为JSP页面创建的Servlet源文件和class类文件放置在“<TOMCAT_HOME>\work\Catalina\<主机名>\<应用程序名>\”目录中 
  2.  JSP页面翻译成的Servlet的包名为， org.apache.jsp.<JSP页面在WEB应用程序内的目录名> 
  
### 4.JSP与AJAX+HTML

- 访问JSP的HTTP请求

  1. 每次访问JSP，响应报文中都会返回整个HTML的内容。

  2. 大部分静态页面是没有改动的，只有少部分的变量内容会改动，相应对于网络的压力会比较大。

- 直接访问静态资源

  1. 直接访问静态资源，指的是那些不会变动的HTML，访问完之后就可以将静态资源缓存在服务器上。
  2. 对于静态HTML中的变量值，可以通过AJAX技术进行动态更新。所以响应报文中，只有变量数据内容。

### 5.与JSP相关的Servlet类

Tomcat的默认配置文件web.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.1" xmlns="http://xmlns.jcp.org/xml/ns/javaee" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                             http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">

	<!-- 声明默认的Servlet -->
	<servlet>
        <servlet-name>default</servlet-name>
        <servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>
        <init-param>
            <param-name>debug</param-name>
            <param-value>0</param-value>
        </init-param>
        <init-param>
            <param-name>listings</param-name>
            <param-value>false</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <!-- 声明默认处理JSP的Servlet -->
    <servlet>
        <servlet-name>jsp</servlet-name>
        <servlet-class>org.apache.jasper.servlet.JspServlet</servlet-class>
        <init-param>
            <param-name>fork</param-name>
            <param-value>false</param-value>
        </init-param>
        <init-param>
            <param-name>xpoweredBy</param-name>
            <param-value>false</param-value>
        </init-param>
        <load-on-startup>3</load-on-startup>
    </servlet>

    <!-- 设置Servlet匹配规则 -->
    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

    <servlet-mapping>
        <servlet-name>jsp</servlet-name>
        <url-pattern>*.jsp</url-pattern>
        <url-pattern>*.jspx</url-pattern>
    </servlet-mapping>

    <!-- 设置session超时时间 -->
    <session-config>
        <session-timeout>30</session-timeout>
    </session-config>

    <!-- 设置默认的首页页面 -->
    <welcome-file-list>
	    <welcome-file>index.html</welcome-file>
	    <welcome-file>index.htm</welcome-file>
	    <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>
</web-app>
```

#### 5.1 DefaultServlet 

- 定义

  1. DefaultServlet是默认的Servlet，匹配路径是 /。
  2. 越精确的路径，匹配优先级越高，所以 / 的匹配优先级是最低的。

- 作用

  当浏览器的请求路径找不到对应的Servlet处理时，都会被匹配到DefaultServlet处理。

  1. 如果存在静态资源，则读取静态资源并响应。
  2. 如果找不到静态资源，则返回404页面。

####5.2 JspServlet 

- 定义

  Tomcat中用来处理JSP的默认Servlet，匹配路径是 *.jsp 和 *.jspx 

- 作用

  1. 当jsp请求被接收进来后，通过servlet找到对应的jsp文件。
  2. 将jsp文件转换成静态资源，并通过响应报文返回给处理器。

#### 5.3Tomcat中处理请求的三种模式

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_处理请求的三种模式.png)

  