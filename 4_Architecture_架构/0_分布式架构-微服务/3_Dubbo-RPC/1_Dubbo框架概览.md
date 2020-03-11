## Dubbo框架概览

### 1.Dubbo技术组件

####1.1组件运行流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Dubbo组件.png)

- **组件角色**

  | 组件角色  | 说明                                   |
  | --------- | -------------------------------------- |
  | Provider  | 暴露服务的服务提供方                   |
  | Consumer  | 调用远程服务的服务消费方               |
  | Registry  | 服务注册与发现的注册中心               |
  | Monitor   | 统计服务的调用次数和调用时间的监控中心 |
  | Container | 服务运行容器                           |

  

- **组件调用关系**

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/2_组件调用关系.png" style="zoom:80%;" />

  1. 服务容器`Container`负责启动，加载，运行服务提供者。
  2. 服务提供者`Provider`在启动时，向注册中心注册自己提供的服务。
  3. 服务消费者`Consumer`在启动时，向注册中心订阅自己所需的服务。
  4. 注册中心`Registry`返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。
  5. 服务消费者`Consumer`，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。
  6. 服务消费者`Consumer`和提供者`Provider`，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心Monitor。

#### 1.2Dubbo核心功能

- **Remoting远程通讯**
  提供对多种NIO框架抽象封装，包括“同步转异步”和“请求-响应”模式的信息交换方式。
- **Cluster服务框架**
  提供基于接口方法的透明远程过程调用，包括多协议支持，以及软负载均衡，失败容错，地址路由，动态配置等集群支持。
- **Registry服务注册中心**
  基于注册中心目录服务，使服务消费方能动态的查找服务提供方，使地址透明，使服务提供方可以平滑增加或减少机器。



### 2.Dubbo框架总体架构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/3_Dubbo总体架构.png)

#### 2.1分层结构

- Dubbo是按照分层的方式来架构，使用这种方式可以使各个层之间解耦合(或者最大限度地松耦合)。
  1. Dubbo框架设计一共划分了10个层，最上面的Service层是留给实际想要使用Dubbo开发分布式服务的开发者实现业务逻辑的接口层。
  2. 图中左边淡蓝背景的为服务消费方使用的接口，右边淡绿色背景的为服务提供方使用的接口， 位于中轴线上的为双方都用到的接口。
- **服务接口层(Service)**
  与实际业务逻辑相关的，根据服务提供方和服务消费方的业务设计对应的接口和实现。
- **配置层(Config)**
  对外配置接口，以`ServiceConfig`和`ReferenceConfig`为中心，可以直接new配置类，也可以通过Spring解析配置生成配置类。
- **服务代理层(Proxy)**
  服务接口透明代理，生成服务的客户端`Stub`和服务器端`Skeleton`，以`ServiceProxy`为中心，扩展接口为`ProxyFactory`。
- **服务注册层(Registry)**
  封装服务地址的注册与发现，以服务URL为中心，扩展接口为`RegistryFactory`、`Registry`和`RegistryService`。可能没有服务注册中心，此时服务提供方直接暴露服务。
- **集群层(Cluster)**
  封装多个提供者的路由及负载均衡，并桥接注册中心，以`Invoker`为中心，扩展接口为`Cluster`、`Directory`、`Router`和`LoadBalance`。将多个服务提供方组合为一个服务提供方，实现对服务消费方来透明，只需要与一个服务提供方进行交互。
- **监控层(Monitor)**
  `RPC`调用次数和调用时间监控，以`Statistics`为中心，扩展接口为`MonitorFactory`、`Monitor`和`MonitorService`。
- **远程调用层(Protocol)**
  1. 封装`RPC`调用，以`Invocation`和`Result`为中心，扩展接口为`Protocol`、`Invoker`和`Exporter`。
  2. `Protocol`是服务域，它是`Invoker`暴露和引用的主功能入口，它负责`Invoker`的生命周期管理。
  3. `Invoker`是实体域，它是`Dubbo`的核心模型，其它模型都向它靠扰，或转换成它，它代表一个可执行体，可向它发起invoke调用，它有可能是一个本地的实现，也可能是一个远程的实现，也可能一个集群实现。
  
- **信息交换层(Exchange)**
  封装请求-响应模式，同步转异步，以`Request`和`Response`为中心，扩展接口为`Exchanger`、`ExchangeChannel`、`ExchangeClient`和`ExchangeServer`。
- **网络传输层(Transport)**
  抽象`mina`和`netty`为统一接口，以`Message`为中心，扩展接口为`Channel`、`Transporter`、`Client`、`Server`和`Codec`。
- **数据序列化层(Serialize)**
  可复用的一些工具，扩展接口为`Serialization`、 `ObjectInput`、`ObjectOutput`和`ThreadPool`。

#### 2.2分层关系

1. `Protocol`是核心层，也就是只要有`Protocol+Invoker+Exporter`就可以完成非透明的`RPC`调用，然后在`Invoker`的主过程上`Filter`拦截点。
2. `Consumer`和`Provider`是抽象概念，只是想让看图者更直观的了解哪些类分属于客户端与服务器端，不用`Client`和`Server`的原因是`Dubbo`在很多场景下都使用`Provider、Consumer、Registry、Monitor`划分逻辑拓扑节点，保持统一概念。
3. `Cluster`是外围概念，所以`Cluster`的目的是将多个Invoker伪装成一个Invoker，这样其它人只要关注`Protocol`层`Invoker`即可，加上`Cluster`或者去掉`Cluster`对其它层都不会造成影响，因为只有一个提供者时，是不需要`Cluster`的。
4. `Proxy`层封装了所有接口的透明化代理，而在其它层都以`Invoker`为中心，只有到了暴露给用户使用时，才用`Proxy`将`Invoker`转成接口，或将接口实现转成`Invoker`，也就是去掉`Proxy`层`RPC`是可以运行的，只是不那么透明，不那么看起来像调本地服务一样调远程服务。
5. `Remoting`实现是`Dubbo`协议的实现，如果选择`RMI`协议，整个`Remoting`都不会用上。`Remoting`内部再划为`Transport`传输层和`Exchange`信息交换层，`Transport`层只负责单向消息传输，是对`Mina、Netty、Grizzly`的抽象，它也可以扩展`UDP`传输，而`Exchange`层是在传输层之上封装了`Request-Response`语义。
6. `Registry`和`Monitor`实际上不算一层，而是一个独立的节点，只是为了全局概览，用层的方式画在一起。



#### 2.3服务调用流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4Dubbo调用流程.png)