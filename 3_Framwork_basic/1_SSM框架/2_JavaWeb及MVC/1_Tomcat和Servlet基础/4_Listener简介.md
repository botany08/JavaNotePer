## Listener简介

### 1.Servlet监听器体系

- 监听器的定义

  1.  监听器就是一个实现了特定接口EventListener的普通Java程序，这个程序专门用于监听另一个Java对象的方法调用或者属性改变。当被监听对象发生上述事件后，监听器某个方法将立即被执行。 

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/13_监听器工作原理.png)

- 监听器的分类

  1. 域对象监听器

  | 域对象         | 生命周期监听器         | 属性监听器                      |
| -------------- | ---------------------- | ------------------------------- |
  | ServletContext | ServletContextListener | ServletContextAttributeListener |
  | HttpSession    | HttpSessionListener    | HttpSessionAttributeListener    |
  | ServletRequest | ServletRequestListener | ServletRequestAttributeListener |
  
  2. Session的感知监听

     HttpSessionBindingListener类和HttpSessionActivationListener类

  3. 监听器作用

     生命周期监听器，就是监听三大域对象的创建和销毁，据此调用自身的特定方法。

     属性监听器，是专门监听三大域对象get/setAttribute() ，据此调用自身的特定方法。


- Session的创建时机
  1.  只有当在Servlet中调用request.getSession()，且根据JSESSIONID找不到对于的Session时，才会创建新的Session对象，才会被监听到。 
  2.  第二次请求，浏览器会带上JSESSIONID，此时虽然还是request.getSession()，但是会返回上次那个。 

### 2.Spring中的监听器类ContextLoaderListener

- 定义

  ContextLoaderListener是由Spring编写并提供的一个监听器。

  ```java
  /**
  * ContextLoaderListener定义
  **/
  public class ContextLoaderListener extends ContextLoader implements ServletContextListener {...}
  ```

  

- 作用

  1. Spring实现了Tomcat提供的ServletContextListener接口，写了一个监听器来监听项目启动。一旦项目启动，会触发ContextLoaderListener中的特定方法，初始化Spring的IOC容器。

  2. Tomcat的ServletContext创建时，会调用ContextLoaderListener的contextInitialized()，这个方法内部的initWebApplicationContext()就是用来初始化Spring的IOC容器的。 

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/14_Spring监听器.png)

- 监听器初始化容器的流程
  1. Tomcat容器初始化ServletContext
  2. ContextLoaderListener监听到ServletContext对象创建，获取到ServletContext实例
  3. 通过ServletContext实例获取web.xml配置文件中的Spring配置文件位置
  4. 解析Spring配置文件的信息
  5. 初始化IOC容器

- Spring容器和Tomcat容器的关系

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/15_Tomcat容器和Spring容器的关系.png)

  1. Spring的IOC容器初始化完成后，会放入Servlet容器中的ServletContext对象中。
  2. Tomcat的Servlet容器，也拥有类似于IOC容器的反射创建实例的能力。

  

  

- 工具类WebApplicationContextUtils

  ```java
  /**
  * 从ServletContext中取出IOC容器
  **/
  public static WebApplicationContext getWebApplicationContext(ServletContext sc) {
  		return getWebApplicationContext
              (sc,WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
  	}
  ```

  