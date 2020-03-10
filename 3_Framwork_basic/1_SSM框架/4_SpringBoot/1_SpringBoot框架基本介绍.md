## SpringBoot基本介绍

### 1.SpringBoot产生的背景

- **Spring1.0**

  用`XML`文件来配置`Bean`,在`XML`文件中可以轻松地进行依赖注入。当Bean的数量越来越多时，XML配置也会越来越复杂，维护起来很困难。

- **Spring2.0**

  在XML命名空间上做了一定的优化，让配置看起来尽可能简单，但仍然没有彻底地解决配置上的问题。

- **Spring3.0**

  `Java5`新增了注解，可以使用`Spring`提供的`Java`注解来取代曾经的XML配置。

- **Spring4.0**

  `XML`配置文件都不再需要，完全使用`Java`源码级别的配置与`Spring`提供的注解就能快速地开发出`Spring`应用程序。

- **SpringBoot1.0**

  在`Spring4`的基础上，`SpringBoot`封装了`Spring`及`WebServer`，可以通过启动一个`Jar`包来启动一个应用。

### 2.SpringBoot的特性

#### 2.1可创建独立的Spring应用程序

- 可直接运行带有`@SpringBootApplication`注解的类的`main()`方法就能运行一个`Spring`应用程序，本质上是在`SpringBoot`应用程序内部嵌入了一个`WebServer`。

#### 2.2提供嵌入式WebServer(无须部署war包)

- 不再需要将`war`包部署到`WebServer`中，启动`SpringBoot`应用程序后，会在默认端口号`8080`下启动一个嵌入式`Tomcat`，也可以自定义端口号。
- 除了`Tomcat`, `SpringBoot`还提供了`Jetty`、`Undertow`等嵌入式`WebServer`。可以通过配置插件选择不同的服务器。

#### 2.3无任何代码生成技术也无任何XML配置

- 代码生成技术，指的是在程序运行时动态地生成class文件并将其加载到JVM中。
- 在`SpringBoot`中没有使用任何的代码生成技术，也不用配置大量的`XML`文件，只有一个`application.properties`配置文件。

#### 2.4自动化配置

- 自动化配置，指的是在`SpringBoot`的配置文件`application.properties`中存在大量的配置项，但是一般只需要配置少量参数，未配置的选项都会自动加载默认值。
- 实现原理，实际上是由`SpringBoot`提供的一系列`@ConditionalOnXxx`条件注解来实现的，而底层使用了`Spring4.0`的Condition接口。

#### 2.5提供一系列生产级特性

- **核心指标**

  可以随时给`SpringBoot`应用发送`/metrics`请求，随后可获取一个`JSON`数据，包括内存、Java堆、类加载、处理器、线程池等信息。

- **健康检查**

- **外部配置**

  在`Java`命令行上直接运行`SpringBoot`应用，并带上外部配置参数，这些参数将覆盖己有的默认配置参数。

- 自动化关闭

  通过发送一个`URL`请求去关闭`SpringBoot`应用，在自动化技术中会有一定的帮助。

#### 2.6提供开箱即用的Spring插件

- `SpringBoot`提供了大量开箱即用的插件，只需添加一段`Maven`依赖配置即可开启使用。插件一般称作`Starter`，每个`Starter`可能都会有自己的配置项, 而这些配置项都可在`application.properties`文件中进行统一配置。
- `SpringBoot`是一个典型的`核心+插件`的系统架构，核心包含`Spring`最基础的功能，其他更多的功能都通过插件的方式来扩展。



### 3.SpringBoot相关插件

SpringBoot官方提供了大量插件，涉及的面非常广，包括Web、SQL、NoSQL、安全、验证、缓存、消息队列、分布式事务、模板引擎、工作流等，还提供了Cloud、Social、Ops方面的支持。

- SpringBoot对某项技术提供了多种选型

  | 模块名称     | 技术                           |
  | ------------ | ------------------------------ |
  | SQL API      | JDBC、JPA、jOOQ                |
  | 关系数据库   | MySQL、PostgreSQL              |
  | 内存数据库   | H2、HSQLDB、Derby              |
  | NoSQL 数据库 | Redis、MongoDB、Cassandra      |
  | 消息队列     | RabbitMQ、Artemis、HometQ      |
  | 分布式事务   | Atomikos、Bitronix             |
  | 模板引擎     | Velocity、Freemarker、Mustache |

  







