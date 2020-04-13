## API网关基础功能

### 1.系统架构中的网关

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_API网关结构.png)

- **基础定义**
  1. API网关是系统的统一入口，提供最基本的路由服务，将调用转发到上游服务。
  2. 在微服务架构中，API网关可能往往需要兼顾内部和外部的所有微服务，所有的外部调用者都会先走网关，再进入具体的服务当中。
- **主要作用**
  1. 安全相关：用户认证、鉴权、IP黑白名单
  2. 流量控制：进行限流
  3. 集成服务发现：调用具体的微服务
  4. 动态路由
  5. 日志和审计
  6. 负载均衡



### 2.API网关的技术选型

#### 2.1Kong

- **基本介绍**
  1. 从技术的角度讲，`Kong`可以认为是一个`OpenResty`应用程序。`OpenResty`运行在`Nginx`之上，使用Lua扩展了`Nginx`。`Lua`是一种非常容易使用的脚本语言，可以在`Nginx`中编写一些逻辑操作。
  2. 相当于，Kong = OpenResty + Nginx + Lua 
  3. `OpenResty(ngx_openresty)`是一个基于`NGINX`的可伸缩的`Web`平台，是一个强大的Web应用服务器。Web开发人员可以使用`Lua`脚本语言调动`Nginx`支持的各种`C`以及`Lua`模块,更主要的是在性能方面，`OpenResty`可以快速构造出足以胜任`10K`以上并发连接响应的超高性能`Web`应用系统。
- **主要功能**
  1. 云原生：与平台无关，`Kong`可以从裸机运行到`Kubernetes`
  2. 动态路由：`Kong`的背后是`OpenResty`+`Lua`，所以从`OpenResty`继承了动态路由的特性
  3. 熔断
  4. 健康检查
  5. 日志：可以记录通过`Kong`的`HTTP`，`TCP`，`UDP`请求和响应。
  6. 鉴权：权限控制，`IP`黑白名单，同样是`OpenResty`的特性
  7. `SSL`：`SetupaSpecificSSLCertificateforanunderlyingserviceorAPI.`
  8. 监控：`Kong`提供了实时监控插件
  9. 认证：如数支持`HMAC`,`JWT`,`Basic`,`OAuth`2.0等常用协议
  10. 限流
  11. `RESTAPI`：通过`RestAPI`进行配置管理，从繁琐的配置文件中解放
  12. 可用性：天然支持分布式
  13. 高性能：背靠非阻塞通信的`nginx`，性能自不用说
  14. 插件机制：提供众多开箱即用的插件，且有易于扩展的自定义插件接口，用户可以使用`Lua`自行开发插件

#### 2.2Zuul

- **主要功能**

  Zuul**在内部使用了一系列不同类型的过滤器**，实现了网关的功能。

  1. 用户认证和鉴权：`SpringSecurity/Shiro`、`OAuth2.0`
  2. 监控：在边缘跟踪有意义的数据和统计数据，以便给我们一个准确的生产视图
  3. 动态路由：动态路由请求到不同的后端集群
  4. 熔断隔离：`Hystrix` 
  5. 限流：`Hystrix` 
  6. 负载均衡：`Ribbon` 

- **Zuul1.x架构**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/3_Zuul1架构.png)

  1. 本质上就是一个同步`Servlet`，采用**多线程阻塞模型**。

  2. 同步`Servlet`使用`thread per connection`方式处理请求。每来一个请求，`Servlet`容器要为该请求分配一个线程专门负责处理这个请求，直到响应返回客户端这个线程才会被释放返回容器线程池。如果后台服务调用比较耗时，那么这个线程就会被阻塞，阻塞期间线程资源被占用。

  3. Servlet容器线程池的大小是有限制的，当前端请求量大，而后台慢服务比较多时，很容易耗尽容器线程池内的线程，造成容器无法接受新的请求。

  4. 适合计算密集型(CPU bound)应用场景。同步阻塞模式一般会启动很多的线程，必然引入线程切换开销。容器线程池的数量一般是固定的，造成对连接数有一定限制，当后台服务慢，容器线程池易被耗尽。当碰到IO阻塞时，容易浪费线程的处理能力。

     

- **Zuul2.x架构**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/2_Zuul2架构.png)

  1. 采用了Netty实现**异步非阻塞编程模型**，一般异步模式的本质都是使用队列Queue。
  2. 前端有一个队列专门负责处理用户请求，后端有个队列专门负责处理后台服务调用，中间有个事件环线程(Event Loop Thread)，它同时监听前后两个队列上的事件，有事件就触发回调函数处理事件。
  3. 需要的线程比较少，基本上每个CPU核上只需要一个事件环处理线程，前端的连接数可以很多，连接来了只需要进队列，不需要启动线程，事件环线程由事件触发，没有多线程阻塞问题。
  4. 异步非阻塞模式比较适用于IO密集型(IO bound)场景，这种场景下系统大部分时间在处理IO，CPU计算比较轻，少量事件环线程就能处理。

#### 2.3Spring-Cloud-gateway

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_Gateway架构.png)

- **工作流程**

  1. 客户端向SpringCloudGateway发出请求。
  2. 如果GatewayHandlerMapping中找到与请求相匹配的路由，将其发送到GatewayWebHandler。
  3. Handler再通过指定的过滤器链来将请求发送到实际的服务执行业务逻辑，然后返回。
  4. 过滤器之间用虚线分开是因为过滤器可能会在发送代理请求之前("pre")或之后("post")执行业务逻辑。

- **特性**

  1. 基于SpringFramework5，ProjectReactor和SpringBoot2.0
  2. 动态路由
  3. Predicates和Filters作用于特定路由
  4. 集成Hystrix断路器
  5. 集成SpringCloudDiscoveryClient
  6. 易于编写的Predicates和Filters
  7. 限流
  8. 路径重写

- **原理**

  1. 基于Filter链的方式提供了网关基本的功能。

  2. Spring webflux有一个全新的非堵塞的函数式Reactive Web 框架，可以用来构建异步的、非堵塞的、事件驱动的服务，在伸缩性方面表现非常好。使用非阻塞API。 Websockets得到支持，并且由于它与Spring紧密集成。

  3. 和zuul2差不多，但是由于Cloud没有继承zuul2，所以一般使用gateway。

     

#### 2.4自定义实现

- **基本原理**

  使用一个代理Servlet对请求进行拦截，开源项目smiley-http-proxy

- **基本功能**

  1. 路由
  2. 负载均衡
  3. 限流和隔离

