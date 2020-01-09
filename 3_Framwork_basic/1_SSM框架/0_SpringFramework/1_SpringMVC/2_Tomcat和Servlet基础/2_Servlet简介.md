## Servlet简介

### 1.Tomcat作为容器

- Web服务器

  用来存放静态资源(如html文件)，并且将某个主机上的资源映射为一个URL供外界访问。 

- Servlet容器

  1. 用来存放servlet对象，管理servlet的生命周期。
  2. 主要作用是，接收请求、处理请求和响应请求。

- Tomcat的调用机制

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_调用机制.png)

###2.自定义实现Servlet

- Servlet接口

  ```java
  public interface Servlet {
      // Tomcat反射创建Servlet之后，调用init()方法时会传入ServletConfig对象
      public void init(ServletConfig config) throws ServletException;
      
      // 获取ServletConfig对象
      public ServletConfig getServletConfig();
      
      // Tomcat封装了HTTP请求，分别为ServletRequest请求对象和ServletResponse响应对象
      public void service(ServletRequest req, ServletResponse res)
          throws ServletException, IOException;
  
     	// 获取ServletInfo相关信息
      public String getServletInfo();
  
      // Servlet销毁后回调的方法
      public void destroy();
  }
  ```

#### 2.1 ServletConfig对象

Tomcat解析配置文件web.xml，并且封装成ServletConfig对象。

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_ServletConfig对象.png)

#### 2.2Request/Response

- 定义
  1. Tomcat解析Htpp请求，把各个请求头(Header)，请求地址(URL)，请求参数(QueryString)都封装到ServletRequest对象中。 
  2.  Tomcat将一个空的ServletResponse对象，传给Servlet对象。Servlet逻辑处理后得到结果，最终通过response.write()方法，将结果写入response内部的缓冲区。Tomcat会在servlet处理结束后，拿到response，遍历里面的信息，组装成HTTP响应发给客户端。 

#### 2.3Servlet接口层次接口



![](https://javanote.oss-cn-shenzhen.aliyuncs.com/10_Servlet接口体系.png)

- 抽象类GenericServlet的作用

  1. 提升了init方法中原本是形参的servletConfig对象的作用域(成员变量)，方便其他方法使用 。

  2. 封装了一个自定义初始化方法init()。

  3. 通过持有的ServletConfig对象实例，可以获取ServletContext对象。

- 抽象类HttpServlet的作用

  1. 详细的实现了Servlet.service()方法，将ServletRequest转换为HttpServletRequest对象，处理了Http请求的不同类型，比如GET请求和POST请求.
  2. HttpServletRequest接口继承于ServletRequest接口，增加了HTTP协议的相关方法。
  3. HttpServlet类没有抽象方法，但是声明成抽象类，是因为不希望被直接实例，需要自定义Servlet继承重写处理逻辑。
  4. HttpServlet采用了模板方法模式。

#### 2.4自定义Servlet的流程

​	处理HTTP请求的Servlet，只需要继承HttpServlet并重写doGet()/doPost() 方法。



### 3.ServletContext对象

- 基本定义
  1. ServletContext简称为Servlet上下文，本质上是一个数据容器，数据结构是Map。
  2. 服务器会为每个应用创建一个ServletContext对象。
- 作用
  1. ServletContext对象的作用是在整个Web应用的动态资源(Servlet/JSP)之间共享数据。
  2. 在JavaWeb中， 用来装载共享数据的对象称为“域对象”，总共有4个
     - ServletContext域（Servlet间共享数据）
     - Session域（一次会话间共享数据，也可以理解为多次请求间共享数据） 
     - Request域（同一次请求共享数据） 
     - Page域（JSP页面内共享数据） 
- 生命周期
  1. ServletContext对象的创建是在服务器启动时完成的。
  2. ServletContext对象的销毁是在服务器关闭时完成的。

- 获取ServletContext对象的5种方法
  1. ServletConfig#getServletContext()
  2. GenericServlet#getServletContext()
  3. HttpSession#getServletContext()
  4. HttpServletRequest#getServletContext()
  5. ServletContextEvent#getServletContext()

### 4.Servlet映射器

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/12_Servlet映射器.png)

- 定义

  Servlet映射器的作用就是，将HTTP请求映射到对应处理的Servlet上。在Tomcat中实现为Mapper类。

- Tomcat中默认的匹配规则

  1. 对于静态资源，Tomcat最后会交由一个叫做DefaultServlet的类来处理。
  2. 对于Servlet ，Tomcat最后会交由一个叫做 InvokerServlet的类来处理。
  3. 对于JSP，Tomcat最后会交由一个叫做JspServlet的类来处理。