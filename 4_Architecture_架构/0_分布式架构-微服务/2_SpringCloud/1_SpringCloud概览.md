## SpringCloud概览

### 1.SpringCloud基本定义

#### 1.1背景

1.  `SpringCloud`是一系列框架的有序集合。 利用`SpringBoot`的开发便利性巧妙地简化了分布式系统基础设施的开发，如服务发现注册、配置中心、消息总线、负载均衡、断路器、数据监控等，都可以用Spring Boot的开发风格做到一键启动和部署。 
2. `Spring`将目前各家公司开发的比较成熟、经得起实际考验的服务框架组合起来，通过`SpringBoot`风格进行再封装屏蔽掉了复杂的配置和实现原理，最终给开发者留出了一套简单易懂、易部署和易维护的分布式系统开发工具包。
3. 服务是可以独立部署、水平扩展、独立访问（或者有独立的数据库）的服务单元，采用了微服务这种架构之后，项目的数量会非常多，`SpringCloud`就是用来管理这一群项目。

#### 1.2SpringCloud技术栈

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/1_SpringCloud技术组件.png"  />

- **服务治理**

  `SpringCloud`的核心，主要通过整合Netflix的相关产品来实现这方面的功能(`SpringCloudNetflix`)

    1. 服务注册和发现的`Eureka`，`SpringCloud`也整合了`Consul`和`Zookeeper`作为备选，但是因为这两个方案在CAP理论上都遵循CP而不是AP，所以官方并没有推荐使用。
    2. 调用断路器`Hystrix`。
    3. 调用端负载均衡`Ribbon`。
      4. Rest客户端`Feign`。
      5. 智能服务路由`Zuul`。
      6. 用于监控数据收集和展示的`Spectator`、`Servo`、`Atlas`。
      7. 用于配置读取的`Archaius`和提供`Controller`层`Reactive`封装的`RxJava`。
      8. `Feign`和`RxJava`并不是`Netiflix`的产品，但是被整合到了`SpringCloudNetflix`中。

- **分布式链路监控**

  `SpringCloudSleuth`提供了全自动、可配置的数据埋点，以收集微服务调用链路上的性能数据，并发送给`Zipkin`进行存储、统计和展示。

- **消息组件**

  1. `SpringCloudStream`对于分布式消息的各种需求进行了抽象，包括发布订阅、分组消费、消息分片等功能，实现了微服务之间的异步通信。
  2. `SpringCloudStream`也集成了第三方的`RabbitMQ`和`ApacheKafka`作为消息队列的实现。
  3. `SpringCloudBus`基于`SpringCloudStream`，主要提供了服务间的事件通信(比如刷新配置)。

- **配置中心**

  基于`SpringCloudNetflix`和`SpringCloudBus`，`Spring`又提供了`SpringCloudConfig`，实现了配置集中管理、动态刷新的配置中心概念。配置通过Git或者简单文件来存储，支持加解密。

- **安全控制**

  `SpringCloudSecurity`基于`OAuth2`这个开放网络的安全标准，提供了微服务环境下的单点登录、资源授权、令牌管理等功能。

- **命令行工具**

  `SpringCloudCli`提供了以命令行和脚本的方式来管理微服务及`SpringCloud`组件的方式。

- **集群工具**

  `SpringCloudCluster`提供了集群选主、分布式锁(暂未实现)、一次性令牌(暂未实现)等分布式集群需要的技术组件。

  

####1.3SpringCloud和Dubbo的区别

|              | Dubbo         | SpringCloud                  |
| ------------ | ------------- | ---------------------------- |
| 服务注册中心 | Zookeeper     | Spring Cloud Netflix Eureka  |
| 服务调用方式 | RPC           | REST API                     |
| 服务监控     | Dubbo-monitor | Spring Boot Admin            |
| 断路器       | 不完善        | Spring Cloud Netflix Hystrix |
| 服务网关     | 无            | Spring Cloud Netflix Zuul    |
| 分布式配置   | 无            | Spring Cloud Config          |
| 服务跟踪     | 无            | Spring Cloud Sleuth          |
| 消息总线     | 无            | Spring Cloud Bus             |
| 数据流       | 无            | Spring Cloud Stream          |
| 批量任务     | 无            | Spring Cloud Task            |

Dubbo比较倾向于是一个RPC框架，引入了开源的服务治理中心(`zookeeper`)，其他微服务架构都要自己搭建。SpringCloud是一系列微服务架构技术的总称，包括但不限于服务治理，远程调用等。假设的情况下，SpringCloud也可以使用Dubbo作为RPC框架，将两者融合起来。

### 2.SpringCloud整体架构

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/2_SC整体架构.png" style="zoom:80%;" />

- 工作流程
  1. Eureka负责服务的注册与发现，各服务信息的集中地。
  2. Hystrix负责监控服务之间的调用情况，连续多次失败进行熔断保护。
  3. Hystrixdashboard，Turbine负责监控Hystrix的熔断情况，并给予图形化的展示。
  4. SpringCloudConfig提供了统一的配置中心服务。
  5. 当配置文件发生变化的时候，SpringCloudBus负责通知各服务去获取最新的配置信息。
  6. 所有客户端的请求和服务，都通过Zuul来进行转发，起到API网关的作用。
  7. 使用Sleuth+Zipkin将所有的请求数据记录下来，进行链路追踪分析。

#### 2.1Eureka服务注册与发现

- **服务注册与发现**

  Eureka就是一个服务中心，将所有可以提供的服务都注册到这里来管理，其它各调用者需要的时候去注册中心获取，然后再进行调用，避免了服务之间的直接调用，方便后续的水平扩展、故障转移等。需要搭建Eureka集群来保持高可用性，防止挂掉影响全部服务，至少两台。

- **负载均衡** 

  随着系统的流量不断增加，需要根据情况来扩展某个服务，Eureka内部已经提供均衡负载的功能，只需要增加相应的服务端实例既可。 

- **故障转移**

  Eureka内有一个心跳检测机制，如果某个实例在规定的时间内没有进行通讯则会自动被剔除掉，避免了某个实例挂掉而影响服务。

#### 2.2Hystrix熔断中心

- **雪崩效应**

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/3_雪崩效应.png" style="zoom: 80%;" />

  1. 在微服务架构中通常会有多个服务层调用，基础服务的故障可能会导致级联故障，进而造成整个系统不可用的情况，这种现象被称为服务雪崩效应。 
  2. 服务雪崩效应是一种因 “服务提供者” 的不可用导致 “服务消费者” 的不可用，并将不可用逐渐放大的过程。

- **故障隔离**

  **Hystrix**会在某个服务连续调用 N 次不响应的情况下，立即通知调用端调用失败，避免调用端持续等待而影响了整体服务。**Hystrix**间隔时间会再次检查此服务，如果服务恢复将继续提供服务。

#### 2.3HystrixDashboard和Turbine熔断监控

- **Hystrix-dashboard**

  一款针对`Hystrix`进行实时监控的工具，可以直观地看到各`HystrixCommand`的请求响应时间，请求成功率等数据。当熔断发生的时候需要迅速的响应来解决问题，避免故障进一步扩散。 

- **Turbine**

  用来汇总系统内多个服务的数据并显示到`HystrixDashboard`上。只使用`HystrixDashboard`的话，只能看到单个应用内的服务信息。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_Turbine效果图.png)

#### 2.4SpringCloudConfig配置中心

- 基本定义
  1. 一个解决分布式系统的配置管理方案，包含了`Client`和`Server`两个部分。`Server`提供配置文件的存储、以接口的形式将配置文件的内容提供出去。`Client`(服务应用)通过接口获取数据、并依据此数据初始化本服务的应用。 
  2. 就是`Server`端将所有的配置文件服务化，需要配置文件的服务实例去`ConfigServer`获取对应的数据。将所有的配置文件统一整理，避免了配置文件碎片化。
  3. 如果服务运行期间改变配置文件，服务是不会得到最新的配置信息，需要解决这个问题就需要引入`Refresh`。可以在服务的运行期间重新加载配置文件 
- 安全性
  1. 当所有的配置文件都存储在配置中心的时候，配置中心就成为了一个非常重要的组件。如果配置中心出现问题将会导致灾难性的后果，因此在生产中建议对配置中心做集群，来支持配置中心高可用性。 

#### 2.5SpringCloudBus消息总线配置更新

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/5_bus消息总线原理.png)

- 出现背景

  `Refresh`方案虽然可以解决单个微服务运行期间重载配置信息的问题，但是在真正的实践生产中，可能会有N多的服务需要更新配置，如果每次依靠手动`Refresh`将是一个巨大的工作量。

- 实现原理

  1. `SpringCloudBus`通过轻量消息代理连接各个分布的节点，这会用在广播状态的变化(例如配置变化)或者其它的消息指令中。
  2. `SpringCloudBus`的一个核心思想是通过分布式的启动器对`SpringBoot`应用进行扩展，也可以用来建立一个或多个应用之间的通信频道。目前唯一实现的方式是用`AMQP`消息代理作为通道。
  3. `SpringCloudBus`是轻量级的通讯组件，也可以用在其它类似的场景中。有了`SpringCloudBus`之后，当改变配置文件提交到版本库中时，会自动的触发对应实例的`Refresh`。

#### 2.6SpringCloudZuul网关

- 出现背景
  1. 在微服务架构模式下，后端服务的实例数一般是动态的，对于客户端而言很难发现动态改变的服务实例的访问地址信息。
  2. 在基于微服务的项目中为了简化前端的调用逻辑，通常会引入APIGateway作为轻量级网关，同时APIGateway中也会实现相关的认证逻辑从而简化内部服务之间相互调用的复杂度。
- 网关实现与功能
  1. SpringCloudZuul路由是微服务架构中不可或缺的一部分，提供动态路由，监控，弹性，安全等的边缘服务。Zuul是Netflix出品的一个基于JVM路由和服务端的负载均衡器。
  2. 具体作用就是服务转发，接收并转发所有内外部的客户端调用。使用Zuul可以作为资源的统一访问入口，同时也可以在网关做一些权限校验等类似的功能。

#### 2.7链路追踪

- 出现背景
  1. 随着服务的越来越多，对调用链的分析会越来越复杂，如服务之间的调用关系、某个请求对应的调用链、调用之间消费的时间等，对这些信息进行监控就成为一个问题。
  2. 在实际的使用中我们需要监控服务和服务之间通讯的各项指标，这些数据将是改进系统架构的主要依据。
- 具体实现
  1. SpringCloudSleuth为服务之间调用提供链路追踪。通过Sleuth可以很清楚的了解到一个服务请求经过了哪些服务，每个服务处理花费了多长时间。从而让我们可以很方便的理清各微服务间的调用关系。
  2. Zipkin是Twitter的一个开源项目，允许开发者收集Twitter各个服务上的监控数据，并提供查询接口。
  3. 分布式链路跟踪需要Sleuth+Zipkin结合来实现。



