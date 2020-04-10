## RocketMQ基础

### 1.基本介绍

#### 1.1简介

- `RocketMQ`是一个纯Java、分布式、队列模型的开源消息中间件，前身是MetaQ，是阿里参考`Kafka`特点研发的一个队列模型的消息中间件，后开源给apache基金会成为了apache的顶级开源项目，具有高性能、高可靠、高实时、分布式特点。

#### 1.2功能

- 发布/订阅消息传递模型
- 财务级交易消息
- 各种跨语言客户端，例如Java，C / C ++，Python，Go
- 可插拔的传输协议，例如TCP，SSL，AIO
- 内置的消息跟踪功能，还支持开放式跟踪
- 多功能的大数据和流生态系统集成
- 按时间或偏移量追溯消息
- 可靠的FIFO和严格的有序消息传递在同一队列中
- 高效的推拉消费模型
- 单个队列中的百万级消息累积容量
- 多种消息传递协议，例如JMS和OpenMessaging
- 灵活的分布式横向扩展部署架构
- 快如闪电的批量消息交换系统
- 各种消息过滤器机制，例如SQL和Tag
- 用于隔离测试和云隔离群集的Docker映像
- 功能丰富的管理仪表板，用于配置，指标和监视
- 认证与授权

#### 1.3核心模块

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/13_RocketMQ核心模块.png)

- rocketmq-broker：接受生产者发来的消息并存储(通过调用rocketmq-store)，消费者从这里取得消息
- rocketmq-client：提供发送、接受消息的客户端API。
- rocketmq-namesrv：NameServer，类似于Zookeeper，这里保存着消息的TopicName，队列等运行时的元信息。
- rocketmq-common：通用的一些类，方法，数据结构等。
- rocketmq-remoting：基于Netty4的client/server + fastjson序列化 + 自定义二进制协议。
- rocketmq-store：消息、索引存储等。
- rocketmq-filtersrv：消息过滤器Server，需要注意的是，要实现这种过滤，需要上传代码到MQ！(一般而言，我们利用Tag足以满足大部分的过滤需求，如果更灵活更复杂的过滤需求，可以考虑filtersrv组件)。
- rocketmq-tools：命令行工具。 

### 2.架构组成

- 四大核心组成部分：NameServer、Broker、Producer以及Consumer。
  1. RocketMQ都是集群部署的，这是他吞吐量大，高可用的原因之一，集群的模式也很花哨，可以支持多master 模式、多master多slave异步复制模式、多 master多slave同步双写模式。

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/14_架构组成.png)

#### 2.1NameServer

- **作用**

  主要负责对于源数据的管理，包括了对于Topic和路由信息的管理。

- **特点**

  1. `NameServer`是一个功能齐全的服务器，其角色类似`Dubbo`中的`Zookeeper`，但`NameServer`与`Zookeeper`相比更轻量。主要是因为每个`NameServer`节点互相之间是独立的，没有任何信息交互。

  2. `NameServer`压力不会太大，平时主要开销是在维持心跳和提供`Topic-Broker`的关系数据。

     `Broker`向`NameServer`发心跳时， 会带上当前自己所负责的所有`Topic`信息，如果`Topic`个数太多(万级别)，会导致一次心跳中，就Topic的数据就几十M，网络情况差的话， 网络传输失败，心跳失败，导致`NameServer`误认为`Broker`心跳失败。

  3. `NameServer`被设计成几乎无状态的，可以横向扩展，节点之间相互之间无通信，通过部署多台机器来标记自己是一个伪集群。

- **工作流程**

  每个`Broker`在启动的时候会到`NameServer`注册，`Producer`在发送消息前会根据`Topic`到`NameServer`获取到`Broker`的路由信息，`Consumer`也会定时获取`Topic`的路由信息。

#### 2.2Producer

- **作用**
  1. 消息生产者，负责产生消息，一般由业务系统负责产生消息。
  2. `Producer`由用户进行分布式部署，消息由`Producer`通过多种负载均衡模式发送到`Broker`集群，发送低延时，支持快速失败。
- **发送消息的三种方式**
  1. **同步发送**：同步发送指消息发送方发出数据后会在收到接收方发回响应之后才发下一个数据包。一般用于重要通知消息，例如重要通知邮件、营销短信。
  2. **异步发送**：异步发送指发送方发出数据后，不等接收方发回响应，接着发送下个数据包，一般用于可能链路耗时较长而对响应时间敏感的业务场景，例如用户视频上传后通知启动转码服务。
  3. **单向发送**：单向发送是指只负责发送消息而不等待服务器回应且没有回调函数触发，适用于某些耗时非常短但对可靠性要求并不高的场景，例如日志收集。

#### 2.3Broker

- **作用**
  1. 消息中转角色，负责存储消息，转发消息。
  2. 具有上亿级消息堆积能力，同时可严格保证消息的有序性。
- **特点**
  1. `Broker`是具体提供业务的服务器，单个`Broker`节点与所有的`NameServer`节点保持长连接及心跳，并会定时将`Topic`信息注册到`NameServer`，顺带一提底层的通信和连接都是基于Netty实现的。
  2. `Broker`负责消息存储，以`Topic`为纬度支持轻量级的队列，单机可以支撑上万队列规模，支持消息推拉模型。

#### 2.4Consumer

- **作用**
  1. 消息消费者，负责消费消息，一般是后台系统负责异步消费。
  2. `Consumer`由用户部署，支持`PUSH`和`PULL`两种消费模式，支持集群消费和广播消息，提供实时的消息订阅机制。
- **拉取型消费者(Pull)**
  1. 主动从消息服务器拉取信息，只要批量拉取到消息，用户应用就会启动消费过程。
- **推送型消费者(Push)**
  1. 封装了消息的拉取、消费进度和其他的内部维护工作，将消息到达时执行的回调接口留给用户应用程序来实现。
  2. 从实现上看还是从消息服务器中拉取消息，不同于`Pull`的是`Push`首先要注册消费监听器，当监听器处触发后才开始消费消息。

###3.消息领域模型

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/15_消息领域模型.png)

#### 3.1Message

- **定义及作用**
  1. 消息(`Message`)就是要传输的信息。
  2. 一条消息必须有一个主题(`Topic`)，`Topic`可以看做是你的信件要邮寄的地址。
  3. 一条消息也可以拥有一个可选的标签(`Tag`)和额处的键值对，它们可以用于设置一个业务`Key`并在`Broker`上查找此消息以便在开发期间查找问题。

#### 3.2Topic

- **定义及作用**
  1. Topic(主题)可以看做消息的规类，是消息的第一级类型。比如一个电商系统可以分为，交易消息、物流消息等，一条消息必须有一个Topic。
  2. Topic与生产者和消费者的关系非常松散，一个Topic可以有0个、1个、多个生产者向其发送消息，一个生产者也可以同时向不同的Topic发送消息，一个Topic也可以被0个、1个、多个消费者订阅。

#### 3.3Tag

- **定义及作用**
  1. Tag(标签)可以看作子主题，它是消息的第二级类型，用于为用户提供额外的灵活性。
  2. 使用标签，同一业务模块不同目的的消息，就可以用相同Topic而不同的Tag来标识。比如交易消息又可以分为：交易创建消息、交易完成消息等，一条消息可以没有Tag。
  3. 标签有助于保持您的代码干净和连贯，并且还可以为`RocketMQ`提供的查询系统提供帮助。

#### 3.4Group

- **定义及作用**
  1. 分组(`Group`)，一个组可以订阅多个`Topic`。分为`ProducerGroup`，`ConsumerGroup`，代表某一类的生产者和消费者。
  2. 一般来说同一个服务可以作为`Group`，同一个`Group`一般来说发送和消费的消息都是一样。

#### 3.5Queue

- **定义及作用**
  1. 在`Kafka`中叫`Partition`，每个`Queue`内部是有序的，在`RocketMQ`中分为读和写两种队列，一般来说读写队列数量一致，如果不一致就会出现很多问题。

#### 3.6Message Queue

- **定义及作用**
  1. `MessageQueue`(消息队列)，主题被划分为一个或多个子主题，即消息队列。
  2. 一个`Topic`下可以设置多个消息队列，发送消息时执行该消息的`Topic`，`RocketMQ`会轮询该Topic下的所有队列将消息发出去。
  3. 消息的物理管理单位。一个`Topic`下可以有多个`Queue`，`Queue`的引入使得消息的存储可以分布式集群化，具有了水平扩展能力。

#### 3.7Offset

- **定义及作用**
  1. 在`RocketMQ`中，所有消息队列都是持久化，长度无限的数据结构。
  2. 所谓长度无限是指队列中的每个存储单元都是定长，访问其中的存储单元使用`Offset`来访问，`Offset`为javalong类型，64位，理论上在100年内不会溢出，所以认为是长度无限。也可以认为`MessageQueue`是一个长度无限的数组，`Offset`就是下标。

#### 3.8消息消费模式

- **Clustering(集群消费)**
  1. 默认情况下就是集群消费，一个消费者集群共同消费一个主题的多个队列，一个队列只会被一个消费者消费，如果某个消费者挂掉，分组内其它消费者会接替挂掉的消费者继续消费。
- **Broadcasting(广播消费)**
  1. 广播消费消息会发给消费者组中的每一个消费者进行消费。

#### 3.9Message Order

- MessageOrder(消息顺序)有两种，**Orderly(顺序消费)**和**Concurrently(并行消费)**。
  1. 顺序消费表示，消息消费的顺序同生产者为每个消息队列发送的顺序一致，所以如果正在处理全局顺序是强制性的场景，需要确保使用的主题只有一个消息队列。
  2. 并行消费不再保证消息顺序，消费的最大并行数量受每个消费者客户端指定的线程池限制。



### 4.一次完整的通讯流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/16_Rocket运行流程.png)

1. Producer与NameServer集群中的其中一个节点(随机选择)建立长连接，定期从NameServer获取Topic路由信息。
2. 同时向提供Topic服务的BrokerMaster建立长连接，且定时向Broker发送心跳。
3. Producer只能将消息发送到BrokerMaster，但是Consumer则不一样。可以同时和提供Topic服务的Master和Slave建立长连接，既可以从BrokerMaster订阅消息，也可以从BrokerSlave订阅消息。

#### 4.1NameService启动流程

- 在`org.apache.rocketmq.namesrv`目录下的`NamesrvStartup`这个启动类基本上描述了启动过程

  1. 初始化配置。

  2. 创建`NamesrvController`实例，并开启两个定时任务。第一个是每隔10s扫描一次`Broker`，移除处于不激活的`Broker`。第二个是每隔10s打印一次KV配置。

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/17_Nameserver启动流程.png)

  3. 注册钩子函数，启动服务器并监听`Broker`。

#### 4.2Producer发送消息流程

- 程序执行链路图

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/18_Producter链路图.png)

- 发送流程

  1. 通过轮询，Producer轮训某个Topic下面的所有队列实现发送方的负载均衡。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/19_轮询发送.png)

#### 4.3Broker处理流程

- 初始化及处理

  1. `Broker`模块是通过`BrokerStartup`进行启动的，会实例化`BrokerController`，并且调用其初始化方法。
  2. 初始化流程很冗长，会根据配置创建很多线程池主要用来发送消息、拉取消息、查询消息、客户端管理和消费者管理，也有很多定时任务，同时也注册了很多请求处理器，用来发送拉取消息查询消息的。
  3. `Broker`在`RocketMQ`中是进行处理`Producer`发送消息请求，`Consumer`消费消息的请求，并且进行消息的持久化，以及HA策略和服务端过滤，就是集群中很重的工作都是交给了`Broker`进行处理。

  

#### 4.4Consumer消费流程

- 消费流程

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/20_消费者链路.png)

- 接收消息

  1. 消费端会通过**RebalanceService**线程，10秒钟做一次基于**Topic**下的所有队列负载。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/21_消息接收.png)