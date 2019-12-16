## Spring概述

**Spring基本框架结构**

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Spring框架结构爱.png)

- Spring Core：提供IOC容器对象的创建和处理依赖对象关系。

- Spring DAO：Spring提供了对JDBC的操作支持,JdbcTemplate模板工具类。

- Spring ORM：Spring可以与ORM框架整合。

  例如Spring整合Hibernate框架,其中Spring还提供HibernateDaoSupport工具类,简化了Hibernate的操作。

- Spring WEB：Spring提供了对Struts、Springmvc的支持,支持WEB开发。

  与此同时Spring自身也提供了基于MVC的解决方案。

- Spring AOP：Spring提供面向切面的编程,可以给某一层提供事务管理,例如在Service层添加事物控制。

- Spring JEE：J2EE开发规范的支持,例如EJB。

###1.简化Java开发

- Spring简化Java开发的四条准则
  1. 基于POJO的**轻量级和最小侵入性编程**。POJO指的是简单老式Java对象(Plain Old Java object，POJO)
  2. 通过依赖注入和面向接口实现**松耦合**.
  3. 基于切面和惯例进行**声明式编程**.
  4. 通过切面和模板**减少样板式代码**.

####1.1JavaBean-轻量级和最小侵入性编程

- 具体实现
  1. JavaBean中，不用实现Spring规范的接口或继承Spring规范的类。
  2. 在基于Spring构建的应用中，最坏的场景就是应用了Spring的注解，但不是必须的。
- 作用
  1. Spring的非侵入编程模型意味着，这个类在Spring应用和非Spring应用中都可以发挥同样的作用。
  2. 通过DI依赖注入来装配JavaBean

####1.2依赖注入-松耦合

- 出现背景
  1. 在应用程序中，有多个类相互之间进行协作来完成特定的业务逻辑，每个对象负责管理与自己相互协作的对象(即它所依赖的对象)的引用。传统的方式会造成高耦合的情况。
  2. 耦合，一方面紧密耦合的代码难以测试以及复用。另一方面，应用通过耦合把多个类合成一体。耦合是必须的，但是必须最大程度减少耦合。

- 具体实现
  1. 依赖注入，对象的依赖关系将由系统中负责协调各对象的第三方组件在创建对象的时候进行设定，对象无需自行创建或管理它们的依赖关系，依赖关系将被自动注入到目标对象。
  2. 装配指，创建应用组件之间协作的行为。装配Bean则是Spring实现依赖注入的主要方式。
  3. Spring装配Bean分为，通过XML配置文件装配，通过Java代码装配。
  4. Spring应用上下文全权负责对象的创建和组装，Spring自带了多种应用上下文的实现。
- 作用
  1. 依赖注入DI，让相互协作的软件组件保持松散耦合。

####1.3应用切面-声明式编程

- 出现背景
  1. 系统由许多不同的组件组成，每一个组件各负责一块特定功能。但一些特别组件，比如日志、事务管理和安全这样的系统服务，需要加入到系统各个部分中。
  2. 系统服务通常被称为横切关注点，因为会跨越系统的多个组件。第一，实现系统关注点功能的代码将会重复出现在多个组件中。第二，组件会因为那些与自身核心业务无关的代码而变得混乱。
- 具体实现
  1. 通过XML声明的方式，定义一个切面(系统服务类)，一个切点(需要调用系统服务的逻辑代码JavaBean)
  2. 实现的效果就是，在调用切点方法的时候，系统会自动调用切面的系统服务方法，不用在逻辑代码显示调用。
- 作用
  1. 面向切面编程，把遍布应用各处的功能分离出来形成可重用的组件。　
  2. AOP能够使系统服务模块化，并以声明的方式应用到所需的组件中去。将安全、事务和日志关注点与核心业务逻辑相分离。

####1.4使用模板-减少样板式代码

- 出现背景
  1. 样板式的代码指的是，需要不断重复书写的代码。比如JDBC的前置后置代码。
- 具体实现
1. Spring旨在通过模板封装来消除样板式代码。
  2. Spring实现了一些模板类，可以直接调用以便减少样板式代码。比如JDBC调用的JdbcTemplate类。   　

###2.Spring容器-JavaBean

Spring容器分为两类，BeanFactory接口用于提供基本的DI支持。ApplicationContext基于BeanFactory构建，并提供应用框架级别的服务，例如从属性文件解析文本信息以及发布应用事件。

- BeanFactory接口

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/3_实例工厂BeanFactory.png" style="zoom: 50%;" />

- ApplicationContext接口

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/2_上下文类ApplicationContext继承图.png" style="zoom:50%;" />

  1. ClassPathXmlApplicationContext：需要xml配置文件的默认路径，maven中是resource。
  2. FileSystemXmlApplicationContext：需要xml配置文件在系统中的路径。
  3. AnnotationConfigApplicationContext:采用配置类和注解来配置。

####2.1Spring容器的初始化

- IOC的加载流程
  1. 加载配置文件，初始化spring容器。
  2. Spring容器实例化bean对象。
  3. 实例化对象调用方法。

####2.2bean的生命周期

　<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/4_Bean生命周期1.png"  />

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_Bean生命周期2.png)

###3.Spring框架的整体结构
####3.1Spring模块

- Spring的Jar包

  在Spring 4.0中，Spring框架的发布版本包括了20个不同的模块，每个模块会有3个JAR文件。分别是二进制类库、源码的JAR文件以及JavaDoc的JAR文件。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/6_Spring框架jar包.png)　

- 主要6个模块组成

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/5_Spring4的模块.png"  />

- Spring核心容器

  1. 依赖的Jar包

     spring-core：依赖注入IoC与DI的最基本实现。spring-core依赖了commons-logging，相当于整个框架都依赖。如  					  果有自定义logging实现如 log4j，则不用引入该包，否则需要引入。

     spring-beans：Bean工厂与bean的装配。

     spring-context：spring的context上下文即IoC容器。

     spring-expression：spring表达式语言。

     spring-context-support：spring额外支持包，比如邮件服务、视图解析等。依赖spring-core、spring-beans、                   		                                spring-context

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_CoreJar包依赖.png)

  2. 作用

     提供bean工厂和应用上下文，以及最基本的DI支持。也提供了许多企业服务，例如E-mail、JNDI访问、EJB集成和调度。

- Spring的AOP模块

  1. 依赖的Jar包

     spring-aop：面向切面编程。

     spring-aspects：集成AspectJ。

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_AOPJar包依赖.png)

  2. 作用 

    Spring应用系统中开发切面的基础，对面向切面编程提供了丰富的支持。

- 数据访问与集成

  1. 依赖的Jar包

     spring-jdbc：jdbc的支持
     spring-tx：事务控制

     spring-orm：对象关系映射，集成orm框架

     spring-oxm：对象xml映射

     spring-jms：java消息服务

     spring-messaging：用于构建基于消息的应用程序。 依赖spring-core、spring-beans、spring-context

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_数据访问Jar包依赖.png)

  2. 作用

     - Spring的JDBC和DAO(Data Access Object)模块抽象了有关数据库连接的样板式代码。

     - Spring的ORM模块建立在对DAO的支持之上，集成了多个ORM框架。包括Hibernate、Java Persisternce API、Java Data Object和iBATISSQL Maps。

     - 包含了在JMS(Java Message Service)之上构建的Spring抽象层，使用消息以异步的方式与其他应用集成。
     - 包含对象到XML映射的特性
     - 使用Spring AOP模块为Spring应用中的对象提供事务管理服务.

- Web与远程调用
  1. 依赖的Jar包
  
     spring-web：基础web功能，如文件上传。
  
     spring-webmvc：mvc实现。
  
     spring-webmvc-portlet：基于portlet的mvc实现。
  
     spring-websocket：为web应用提供的高效通信工具。依赖spring-core、spring-web、spring-context
  
     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/10_WebJar包依赖.png)
  
  2. 作用
  
     - 自带MVC框架：SpringMVC。
     - 提供了多种构建与其他应用交互的远程调用方案，集成了RMI（Remote Method Invocation）、Hessian、Burlap、JAX-WS。
     - 自带远程调用框架：HTTP invoker。
     - 提供了暴露和使用REST API的良好支持
- Instrumentation
  1. 依赖的Jar包
  
     spring-instrument：提供一些类级的工具支持和ClassLoader级的实现，用于服务器。
  
     spring-instrument-tomcat：针对tomcat的instrument实现。
  
  2. 作用
  
     - 为JVM添加代理(agent)的功能，为Tomcat提供了一个织入代理，能够为Tomcat传递类文件，动态加载。
- 测试
  1. 依赖的Jar包
  
     spring-test：spring测试，提供junit与mock测试功能。依赖spring-core。
  
  2. 作用
  
     - Spring为使用JNDI、Servlet和Portlet编写单元测试提供了一系列的mock对象实现。
     - 对于集成测试，该模块为加载Spring应用上下文中的bean集合以及与Spring上下文中的bean进行交互提供了支持。
####3.2Spring体系

- Spring Web Flow（比较少用）

  建立于Spring MVC框架之上，它为基于流程的会话式Web应用（可以想一下购物车或者向导功能）提供了支持。

- Spring Web Service

  Spring Web Service提供了契约优先的Web Service模型，服务的实现都是为了满足服务的契约而编写的。

- Spring Security（重要）

  利用Spring AOP，Spring Security为Spring应用提供了声明式的安全机制。 	

- Spring Integration

  Spring Integration提供了多种通用应用集成模式的Spring声明式风格实现。

- Spring Batch

  开发一个批处理应用。

- Spring Data（重要）

  Spring Data都为持久化提供了一种简单的编程模型，为多种数据库类型提供了一种自动化的Repository机制，它负责为你创建Repository的实现。

- Spring Social　

  Spring Social更多的是关注连接(connect)，通过REST API连接Spring应用

####　