## Tomcat处理HTTP请求原理

### 1.Connector连接器

####1.1Connector分类

Tomcat 源码中与 connector 相关的类位于 org.apache.coyote 包中，Connector 分为以下三类：

- Http Connector，基于 HTTP 协议，负责建立 HTTP 连接。 又分为 BIO Http Connector 与 NIO Http Connector 两种，后者提供非阻塞 IO 与长连接 Comet 支持。 

- AJP Connector, 基于 AJP 协议，AJP 是专门设计用来为 tomcat 与 http 服务器之间通信专门定制的协议，能提供较高的通信速度和效率。 如与 Apache 服务器集成时，采用这个协议。 

- APR HTTP Connector, 用 C 实现，通过 JNI 调用的。主要提升对静态资源(如 HTML、图片、CSS、JS 等)的访问性能。现在这个库已独立出来可用在任何项目中。Tomcat 在配置 APR 之后性能非常强劲。

  
####1.2Connector的配置

- BIO HTTP/1.1 Connector 配置

  ```xml
  <!-- 
  典型配置 
  acceptCount : 接受连接 request 的最大连接数目,默认值是 10
  address : 绑定 IP 地址,如果不绑定,默认将绑定任何 IP 地址
  allowTrace : 如果是 true, 将允许 TRACE HTTP 方法
  compressibleMimeTypes : 各个 mimeType, 以逗号分隔,如 text/html,text/xml
  compression : 如果带宽有限的话，可以用 GZIP 压缩
  connectionTimeout : 超时时间，默认为 60000ms (60s)
  maxKeepAliveRequest : 默认值是 100
  maxThreads : 处理请求的 Connector 的线程数目，默认值为 200
  -->
  <Connector port="8080" protocol="HTTP/1.1" maxThreads="150" 
  connectionTimeout="20000" redirectPort="8443"/>
  
  <!-- 
  SSL配置 
  keystoreFile：证书位置
  keystorePass：证书密码
  -->
  <Connector port="8181" protocol="HTTP/1.1" SSLEnabled="true" 
      maxThreads="150" scheme="https" secure="true" 
      clientAuth="false" sslProtocol = "TLS" 
      address="0.0.0.0" 
      keystoreFile="E:/java/jonas-full-5.1.0-RC3/conf/keystore.jks" 
      keystorePass="changeit" /> 
  ```

- NIO HTTP/1.1 Connector 配置

  ```xml
  <!-- 典型配置：区别在于protocol协议的配置-->
  <Connector port=”8080” protocol=”org.apache.coyote.http11.Http11NioProtocol” 
      maxThreads=”150” connectionTimeout=”20000” redirectPort=”8443”/>
  ```

- Native APR Connector 配置

  ```xml
  <!-- 
  配置流程
  1. ARP 是用 C/C++ 写的，对静态资源（HTML，图片等）进行了优化。所以要下载本地库 tcnative-1.dll 与
     openssl.exe，将其放在 %tomcat%\bin 目录下.
  -->
  
  <!-- 2. 在 server.xml 中要配置一个 Listener,这个配置 tomcat 是默认配好的。-->
  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" /> 
  
  <!-- 3. 配置使用 APR connector。-->
  <Connector port="8080" protocol="org.apache.coyote.http11.Http11AprProtocol" 
  maxThreads="150" connectionTimeout="20000" redirectPort="8443"/>
  
  <!-- 4. 配置成功，启动 tomcat, 会看到如下信息 -->
  org.apache.coyote.http11.Http11AprProtocol init 
  ```



### 2.Tomcat中的Connector组件

#### 2.1Tomcat架构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/2_Tomcat架构.png)

- Server(服务器)

  Server是Tomcat构成的顶级构成元素，所有一切均包含在Server中，Server的实现类StandardServer可以包含一个到多个Services。

- Service

  Service(次顶级元素)的实现类为StandardService，调用了容器(Container)接口，其实是调用了ServletEngine(引擎)，而且StandardService类中也指明了该Service归属的Server。

- Container(容器)

  总共有4个，引擎(Engine)，主机(Host)，上下文(Context)和Wrapper均继承自Container接口，本质上都是容器。
  在引擎(Engine)，主机(Host)，上下文(Context)和Wrapper中，引擎、主机、上下文和Wrapper从大小上来说构成父子关系，层层包含。

- Connector(连接器)

  将Service和Container连接起来，先把Connector注册到一个Service，把来自客户端的请求转发到Container(容器)。



#### 2.2Tomcat源码模块

- Jsper子模块
  1. 负责 jsp 页面的解析、jsp 属性的验证，同时也负责将 jsp 页面动态转换为 java 代码并编译成 class 文件。
  2. 在 Tomcat 源代码中，凡是属于 org.apache.jasper 包及其子包中的源代码都属于这个子模块。  
- Servlet 和 Jsp 规范的实现模块 
  1. 源代码属于javax.servlet包及其子包，如javax.servlet.Servlet 接口、javax.servet.http.HttpServlet类及 javax.servlet.jsp.HttpJspPage就位于这个子模块中。
- Catalina子模块
  1. 包含了所有以 org.apache.catalina 开头的源代码，大量运用了组合模式(Composite).
  2. 规范了Tomcat的总体架构，定义了Server、Service、Host、Connector、Context、Session及Cluster等关键组件及这些组件的实现.
  3. 规范了Catalina的启动及停止等事件的执行流程.
- Connectors子模块
  1. 具体是Web服务器的实现。连接器(Connector)连接客户端和服务器，接收用户的请求，按照标准的Http协议，负责给客户端发送响应页面。
- Resource子模块 
  1. 包含一些资源文件，如Server.xml及Web.xml配置文件。严格说来不包含java源代码，却是 Tomcat 编译运行所必需的。



#### 2.4Tomcat运行流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/3_Tomcat运行流程.png)

- 假设请求URL为  http://localhost:8080/test/index.jsp，执行流程如下
    1. 请求被发送到本机端口8080，被在那里侦听的 Coyote HTTP/1.1 Connector 获得
    2. Connector把该请求交给它所在的Service的Engine 来处理，并等待Engine的回应
    3. Engine获得请求 localhost:8080/test/index.jsp，匹配它所有虚拟主机 Host
    4. Engine匹配到名为localhost的Host（即使匹配不到也把请求交给该Host处理，因为该Host被定义为该 Engine 的默认主机）
    5. localhost Host 获得请求 /test/index.jsp，匹配它所拥有的所有Context
    6. Host匹配到路径为 /test 的 Context（如果匹配不到就把该请求交给路径名为 "" 的 Context 去处理）
    7. path="/test" 的Context获得请求 /index.jsp，在它的mapping table中寻找对应的 servlet
    8. Context匹配到URL PATTERN为 *.jsp 的servlet，对应于JspServlet类
    9. 构造HttpServletRequest对象和HttpServletResponse对象，作为参数调用JspServlet的doGet或doPost方法
    10. Context把执行完了之后的HttpServletResponse对象返回给Host
    11. Host把HttpServletResponse对象返回给Engine
    12. Engine把HttpServletResponse对象返回给Connector
    13. Connector把HttpServletResponse对象返回给客户browser

### 3.Tomcat启动过程详解

#### 3.1Tomcat的启动分析与集成

- Tomcat的启动

  1. 双击 bin/startup.bat

     本质上是调用Bootstrap类的main方法，启动tomcat。

  2. 运行 bin/catalina.bat run 

     本质上是调用Catalina类，此类使用Apache Digester解析 conf/server.xml 文件生成tomcat组件，然后再调用 Embedded类的start方法启动tomcat。 

- 集成Tomcat

  1. 沿用 tomcat 自身的 server.xml 
  2. 自定义一个xml格式来配置tocmat的各参数，再解析这段 xml，然后使用tomcat提供的API根据这些xml来生成 Tomcat组件，最后调用Embedded类的start方法启动tomcat 

#### 3.2Linux启动与结束Tomcat基本操作

 在Linux系统下，启动和关闭Tomcat使用命令操作。 

1. 执行tomcat 的`./shutdown.sh`后，虽然tomcat服务不能正常访问了，但是`ps -ef | grep tomcat` 后，发现`tomcat`对应的`java`进程未随web容器关闭而销毁，进而存在僵尸`java`进程。 
2. 导致僵尸进程的原因可能是有非守护线程（即User Thread）存在，jvm不会退出。`ps -ef|grep tomcat` 查看Tomcat进程是否结束 ，如果存在用户线程，给kill掉就好了即使用`kill -9 pid` 

```shell
## 1.进入Tomcat下的bin目录
cd /java/tomcat/bin
## 2.启动Tomcat命令
./startup.sh
## 3.停止Tomcat服务命令
./shutdown.sh

```

#### 3.3启动过程Bootstrap详解

- 启动方式
  
  1. 直接执行`startup.sh`脚本，实质上是调用了`org.apache.catalina.startup.Bootstrap.java`这个类下的`start()`方法。
- 启动过程
  1. 执行`Bootstrap`中的`start()`方法
  
     ```java
     public void start() throws Exception {
         if (catalinaDaemon == null) {
             // 当Catalina实例为空时,即第一次启动,进行初始化
             init();
         }
         // 调用Catalina实例的start()方法
         Method method = catalinaDaemon.getClass().getMethod("start", (Class [])null);
         method.invoke(catalinaDaemon, (Object [])null);
     }
     ```
  
  2. 当第一次启动时，会调用其`init()`，其主要用于创建`org.apache.catalina.startup.Catalina.java`的类实例。
  
     ```java
     public void init() throws Exception {
     
         initClassLoaders();
         Thread.currentThread().setContextClassLoader(catalinaLoader);
         SecurityClassLoad.securityClassLoad(catalinaLoader);
     
         // 加载并实例化Catalina类
         if (log.isDebugEnabled())
             log.debug("Loading startup class");
         Class<?> startupClass =
             catalinaLoader.loadClass("org.apache.catalina.startup.Catalina");
         Object startupInstance = startupClass.getConstructor().newInstance();
     
         // Catalina类反射调用方法,进行基本设置
         if (log.isDebugEnabled())
             log.debug("Setting startup class properties");
         String methodName = "setParentClassLoader";
         Class<?> paramTypes[] = new Class[1];
         paramTypes[0] = Class.forName("java.lang.ClassLoader");
         Object paramValues[] = new Object[1];
         paramValues[0] = sharedLoader;
         Method method =
             startupInstance.getClass().getMethod(methodName, paramTypes);
         method.invoke(startupInstance, paramValues);
     
         catalinaDaemon = startupInstance;
     }
     ```
  
  3. 在`Bootstrap`的`start()`方法中会调用`Catalina`实例的`start()`方法。

####3.3启动过程Catalina详解

#####3.3.1`Catalina`的`start()`方法

```java
public void start() {
    // 当Server实例为空时,进行加载Server实例
    if (getServer() == null) {
        load();
    }

	......

    // 调用Server的启动方法start()
    try {
        getServer().start();
    } catch (LifecycleException e) {
        log.fatal(sm.getString("catalina.serverStartFail"), e);
        try {
            getServer().destroy();
        } catch (LifecycleException e1) {
            log.debug("destroy() failed for failed Server ", e1);
        }
        return;
    }
    
    ......
}
```



#####3.3.2`Catalina`的`load()`方法

```java
public void load() {

	......

    // 创建并配置将用来启动的Digester,此接口主要是解析XML文档,默认Servlert类为StandardServer
    Digester digester = createStartDigester();

    ...... //获取所配置的server.xml文件，依次对里面属性进行配置
	
	// 未开始解析时先调用,用来为catalina设置server
    digester.push(this);
    digester.parse(inputSource);
 
	// 配置server实例的属性
    getServer().setCatalina(this);
    getServer().setCatalinaHome(Bootstrap.getCatalinaHomeFile());
    getServer().setCatalinaBase(Bootstrap.getCatalinaBaseFile());

	......

    // 初始化server实例
    try {
        getServer().init();
    } catch (LifecycleException e) {
        if (Boolean.getBoolean("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE")) {
            throw new java.lang.Error(e);
        } else {
            log.error("Catalina.start", e);
        }
    }

	......
}
```

#####3.3.3  StandardServer.initInternal()方法

`Catalina.load()`方法中，`getServer().init()`调用的是`LifecycleBase.init()`方法

- StandardServer类的体系结构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/6_StandarServer的类体系结构.png)

- `LifecycleBase.init()`方法定义

```java
// init()方法定义于,LifecycleBase抽象类中. Lifecycle接口负责管理Server的生命周期.
public final synchronized void init() throws LifecycleException {
    if (!state.equals(LifecycleState.NEW)) {
        invalidTransition(Lifecycle.BEFORE_INIT_EVENT);
    }
    try {
        setStateInternal(LifecycleState.INITIALIZING, null, false);
        // 封装成initInternal(),由具体实现类StandardServer进行实现
        initInternal();
        setStateInternal(LifecycleState.INITIALIZED, null, false);
    } catch (Throwable t) {
        handleSubClassException(t, "lifecycleBase.initFail", toString());
    }
}
```

- `StandardServer.initInternal()`方法处理流程
1. 调用父类org.apache.catalina.util.LifecycleMBeanBase#initInternal方法，注册MBean。
  
2. 注册本类的其它属性的MBean 
  
3. NamingResources初始化：globalNamingResources.init()。
  
4. 从common ClassLoader开始往上查看，直到SystemClassLoader，遍历各个classLoader对应的查看路径，找到jar结尾的文件，读取Manifest信息，加入到ExtensionValidator#containerManifestResources属性中。 
  
5. 初始化service，默认实现是StandardService。同时初始化了container ，Executor及Connector。

#####3.3.4StandardServer.startInternal()方法

`Catalina.start()`方法中，`getServer().start()`调用的是`LifecycleBase.start()`方法。

`LifecycleBase.start()`方法封装了`startInternal()`方法，让子类`StandardServer`实现。

- `StandardServer.startInternal()`方法处理流程
  1. 触发CONFIGURE_START_EVENT事件。 
  2. 设置本对象状态为STARTING.
  3. NameingResource启动，globalNamingResources.start(); 
  4. StandardService启动，同时启动StandardEngine、Executor 、Connector。



###4.Connector连接器详解

#### 4.1Connector 的工作流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_Connector连接器工作流程.png)

- 准备进入服务器监听状态

  1. Connector类通过反射实例化，具体的处理类Http11AprProtocol(实现ProtocolHandler接口)。
  2. Http11AprProtocol类进行初始化(init)，将初始化工作委托给NioEndpoint类(继承AbstractEndpoint抽象类)。
  3. NioEndpoint类，初始化构建一个ServerSocket对象，服务器监听状态就绪。

- 处理客户端传入的请求

  1. ServletSocket监听端口，接收到客户端的请求，新建一个socket匹配该请求。

  2. Connector类将socket传入Http11AprProtocol类，此类利用线程池新建一个线程，执行Acceptor任务类。

  3. Acceptor任务类，接受socket并委托ConnectionHandler类(实现Handler接口,Http11AprProtocol内部类)处理。

  4. Handler接口实现类，通过process()方法调用Http11Processor类(接口Processor接口)，主要逻辑处理点。

  5. Http11Processor类，通过Adapter接口调用Servlet容器生成响应数据，并封装到Response类中。

  6. Response类，action()方法回调Http11Processor类的回调方法。

     

#### 4.2Connector构造器

- 连接器的默认配置

  ```xml
  <!-- HTTP1.1协议连接器 -->
  <Connector port="8080" protocol="HTTP/1.1"
              connectionTimeout="20000"
              redirectPort="8443" />
  <!-- AJP1.3协议连接器 -->            
  <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
  ```

- `Connector`构造器

  ```java
  public Connector() {
      this(null);
  }
  
  /**
  * 传入协议构造
  **/
  public Connector(String protocol) {
      setProtocol(protocol);
      // Instantiate protocol handler
      ProtocolHandler p = null;
      try {
          Class<?> clazz = Class.forName(protocolHandlerClassName);
          p = (ProtocolHandler) clazz.getConstructor().newInstance();
      } catch (Exception e) {
          log.error(sm.getString(
              "coyoteConnector.protocolHandlerInstantiationFailed"), e);
      } finally {
          this.protocolHandler = p;
      }
  
      if (Globals.STRICT_SERVLET_COMPLIANCE) {
          uriCharset = StandardCharsets.ISO_8859_1;
      } else {
          uriCharset = StandardCharsets.UTF_8;
      }
  }
  
  /**
  * HTTP/1.1协议默认采用 Http11NioProtocol 类
  * AJP/1.3协议默认采用 AjpNioProtocol 类
  * aprConnector表示的是,是否采用APR协议对静态资源进行优化,需要另外配置
  **/
  public void setProtocol(String protocol) {
      boolean aprConnector = AprLifecycleListener.isAprAvailable() &&
          AprLifecycleListener.getUseAprConnector();
  
      if ("HTTP/1.1".equals(protocol) || protocol == null) {
          if (aprConnector) {
              setProtocolHandlerClassName("org.apache.coyote.http11.Http11AprProtocol");
          } else {
              setProtocolHandlerClassName("org.apache.coyote.http11.Http11NioProtocol");
          }
      } else if ("AJP/1.3".equals(protocol)) {
          if (aprConnector) {
              setProtocolHandlerClassName("org.apache.coyote.ajp.AjpAprProtocol");
          } else {
              setProtocolHandlerClassName("org.apache.coyote.ajp.AjpNioProtocol");
          }
      } else {
          setProtocolHandlerClassName(protocol);
      }
  }
  ```

####4.3Connector初始化与启动

##### 4.3.1Connector. initInternal()初始化方法

- 处理流程

  1. 注册MBean 

  2. `CoyoteAdapter`实例化，`CoyoteAdapter`是请求的入口。当有请求时，`CoyoteAdapter`对状态进行了处理，结尾处对请求进行回收，中间过程交由pipeline来处理。

  3. `protocolHandler`初始化`(org.apache.coyote.http11.Http11NioProtocol)`，调用`protocolHandler.init()`

     `protocolHandler`接口已经在`Connector`构造函数中，默认用`Http11NioProtocol`实例化。在其的默认构造方法中，用`NioEndpoint`类初始化了`AbstractProtocol`抽象类中的`AbstractEndpoint`参数。

##### 4.3.2Connector. startInternal()启动方法

- 处理流程
  1.  设定本对象状态为STARTING 
  2.  同时调用`protocolHandler.start();` 



####4.4ProtocolHandler接口

- 类结构层次图

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_Protocol接口Http和Ajp实现.png)

- 抽象类`AbstractProtocol`

  1. 实现了生命周期方法 `init()`初始化与`start()` 启动
  2. 内部是由自实现的相对应的`endpoint`来执行具体逻辑，持有一个`AbstractEndpoint`引用
  3. 其实现类 `Http11NioProtocol` ，持有的引用为 `NioEndpoint`。 



#### 4.5AbstractEndpoint抽象类

- `AbstractEndpoint`类继承结构

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_Endpoint类继承结构.png)

#####4.5.1Endpoint主要作用

- 内部处理请求的具体流程

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_endpoint内部处理请求的具体流程.png)

- 工作内容

  1. 用于处理具体连接和传输数据，实现网络连接和控制，是服务器对外`I/O`操作的接入点。
  2. 准备服务器的监听状态，绑定ServerSocket。
  3. 管理对外的`socket`连接，同时将建立好的`socket`连接交到合适的工作线程中去。 
  4. 主要的属性类为 `Acceptor`和`Poller`、`SocketProcessor` 。

#####4.5.1Endpoint的初始化和启动方法

- `AbstractEndpoint`抽象类的初始化和启动

  1. 初步实现了`init()`方法，并封装了`bind()`方法供子类自定义实现。

  2. 初步实现了`start()`方法，并封装了`startInternal()`方法供子类自定义实现。

     

- `NioEndpoint`类的`bind()`方法
  
   1. 以普通阻塞方式启动了`ServerSocket`。
   
   2. 检查了代表`Acceptor`和`Poller`初始化的线程数量的`cceptorThreadCount`属性和`pollerThreadCount` 属性，它们的值至少为1。 
   
   3. 初始化SSL内容。
   
   ```java
   // 构建一个ServerSocket对象,并且绑定监听端口
   public void bind() throws Exception {
       /**
         * 1.初始化了ServerSocket对象
         **/ 
       if (!getUseInheritedChannel()) {
           serverSock = ServerSocketChannel.open();
           socketProperties.setProperties(serverSock.socket());
           InetSocketAddress addr = (getAddress()!=null?
                                     new InetSocketAddress(getAddress(),getPort()):
                                     new InetSocketAddress(getPort()));
           serverSock.socket().bind(addr,getAcceptCount());
       } else {
           // Retrieve the channel provided by the OS
           Channel ic = System.inheritedChannel();
           if (ic instanceof ServerSocketChannel) {
               serverSock = (ServerSocketChannel) ic;
           }
           if (serverSock == null) {
               throw new
                   IllegalArgumentException(sm.getString("endpoint.init.bind.inherited"));
           }
       }
       serverSock.configureBlocking(true); //mimic APR behavior
   
       /**
         * 2.检查Acceptor和Poller初始化线程数量
         *   Acceptor用于接收请求,将接收到请求交给Poller处理,都是通过启动线程处理.
         **/ 
       if (acceptorThreadCount == 0) {
   
           acceptorThreadCount = 1;
       }
       if (pollerThreadCount <= 0) {
   
           pollerThreadCount = 1;
       }
       setStopLatch(new CountDownLatch(pollerThreadCount));
   
       /**
         * 3.初始化SSL内容
         **/
       initialiseSsl();
   
       selectorPool.open();
   }
   ```




- `NioEndpoint`类的`startInternal()`方法
  
  1. 以 `new Thread` 的方式，启动`Acceptor`线程组，线程数量为`acceptorThreadCount`，默认为1。
  2. 以 `new Thread` 的方式，启动`Poller`线程组，线程数量为`pollerThreadCount`，默认为1。
  3. 如果`Executor`线程池为空，则创建一个线程池。
  
  ```java
  /**
     * 客户端请求的处理流程
     * 1.Acceptor(AbstractEndpoint内部类)接收请求,交给Poller处理
     * 2.Poller(NioEndpoint内部类)接收请求后,交给SocketProcessor(NioEndpoint内部类)处理
     * 3.SocketProcessor将请求传递到Handler接口(AbstractEndpoint内部接口)处理,
     *   实现类为ConnectionHandler(AbstractProtocol内部类)
     **/
  public void startInternal() throws Exception {
  
      if (!running) {
          running = true;
          paused = false;
  
          // processorCache变量为SynchronizedStack<SocketProcessor>类型
          processorCache = new SynchronizedStack<>(SynchronizedStack.DEFAULT_SIZE,
                                                   socketProperties.getProcessorCache());
          eventCache = new SynchronizedStack<>(SynchronizedStack.DEFAULT_SIZE,
                                               socketProperties.getEventCache());
          nioChannels = new SynchronizedStack<>(SynchronizedStack.DEFAULT_SIZE,
                                                socketProperties.getBufferPool());
  
          // 创建一个线程池
          if ( getExecutor() == null ) {
              createExecutor();
          }
  
          initializeConnectionLatch();
  
          // 启动Poller线程组-默认数量为1
          pollers = new Poller[getPollerThreadCount()];
          for (int i=0; i<pollers.length; i++) {
              pollers[i] = new Poller();
              Thread pollerThread = new Thread(pollers[i], getName() + "-ClientPoller-"+i);
              pollerThread.setPriority(threadPriority);
              pollerThread.setDaemon(true);
              pollerThread.start();
          }
  
          // 启动Acceptor线程组-默认数量为1
          startAcceptorThreads();
      }
  }
  
  /**
     * 创建一个线程池Executor
     **/
  public void createExecutor() {
      internalExecutor = true;
      TaskQueue taskqueue = new TaskQueue();
      TaskThreadFactory tf = 
          new TaskThreadFactory(getName() + "-exec-", daemon, getThreadPriority());
      executor = new ThreadPoolExecutor(
          getMinSpareThreads(), getMaxThreads(), 60, TimeUnit.SECONDS,taskqueue, tf);
      taskqueue.setParent( (ThreadPoolExecutor) executor);
  }
  
  /**
     * 启动Acceptor线程组
     **/
  protected final void startAcceptorThreads() {
      int count = getAcceptorThreadCount();
      acceptors = new Acceptor[count];
  
      for (int i = 0; i < count; i++) {
          acceptors[i] = createAcceptor();
          String threadName = getName() + "-Acceptor-" + i;
          acceptors[i].setThreadName(threadName);
          Thread t = new Thread(acceptors[i], threadName);
          t.setPriority(getAcceptorThreadPriority());
          t.setDaemon(getDaemon());
          t.start();
      }
  }
  ```



### 5.NioEndpoint中的请求处理

####5.1Acceptor和Poller的协同工作6

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/10_NioEndpoint工作处理流程.png)



#### 5.2Acceptor工作方式

##### 5.2.1 Acceptor.run()方法

- 逻辑处理流程
  1. 如果请求连接达到了最大连接数，则等待。
  2. 阻塞的监听接口,直到此端口有连接进来,获取到SocketChannel对象。
  3. 将连接传入setSocketOptions(socket)方法,此方法主要用来处理SocketChannel管道。
  4. 关闭socket管道。

```java
public void run() {
    ......

    // 接收线程Acceptor保持监听状态,
    while (running) {

        ......

        try {
            // 1.如果请求连接达到了最大连接数，则等待
            countUpOrAwaitConnection();
            
            // SocketChannel为会话管道对象
            SocketChannel socket = null;
            try {
                // 2.阻塞的监听接口,直到此端口有连接进来,获取到SocketChannel对象
                socket = serverSock.accept();
            } catch (IOException ioe) {
                ......
            }
            
            ......

            // 当NioEndpoint状态为正常运行且没暂停
            if (running && !paused) {
                // 3.将连接传入setSocketOptions(socket)方法,此方法主要用来处理SocketChannel管道
                if (!setSocketOptions(socket)) {
                    closeSocket(socket);
                }
            } else {
                // 4.关闭socket管道
                closeSocket(socket);
            }
        } catch (Throwable t) {
            ......
        }
    }
    state = AcceptorState.ENDED;
}
```



#####5.2.2Acceptor.setSocketOptions(socket)方法

- 逻辑处理流程
  1. 将SocketChannel实例设置为非阻塞模式。
  2. 从缓存中拿一个NioChannel实例,若无则创建一个,并将SocketChannel传入。
  3. 从pollers数组中获取一个Poller对象,注册NioChannel实例。

```java
/**
* 作用：根据SocketChannel构建一个NioChannel对象,然后将NioChannel注册到Poller的事件列表中,等待poller轮询.
**/
protected boolean setSocketOptions(SocketChannel socket) {
    
    try {
        // 1.将SocketChannel实例设置为非阻塞模式
        socket.configureBlocking(false);
        Socket sock = socket.socket();
        socketProperties.setProperties(sock);
        
        // 2.从缓存中拿一个NioChannel实例,若无则创建一个,并将SocketChannel传入.
        NioChannel channel = nioChannels.pop();
        if (channel == null) {
            SocketBufferHandler bufhandler = new SocketBufferHandler(
                socketProperties.getAppReadBufSize(),
                socketProperties.getAppWriteBufSize(),
                socketProperties.getDirectBuffer());
            if (isSSLEnabled()) {
                channel = new SecureNioChannel(socket, bufhandler, selectorPool, this);
            } else {
                channel = new NioChannel(socket, bufhandler);
            }
        } else {
            channel.setIOChannel(socket);
            channel.reset();
        }
        // 3.从pollers数组中获取一个Poller对象,注册NioChannel实例
        getPoller0().register(channel);
    } catch (Throwable t) {
        ......
        return false;
    }
    return true;
}
```



##### 5.2.3Poller.register(channel)方法

- 逻辑处理流程
  1. 创建一个NioSocketWrapper对象,包装NioChannel实例。
  2. 配置NioSocketWrapper相关属性。
  3. NioSocketWrapper设置interestOps为SelectionKey.OP_READ。
  4. 从缓存中取出一个PollerEvent对象，若没有则创建(通过NioChannel和NioSocketWrapper)。
  5. 初始化或者重置此Event对象，会将其interestOps设置为OP_REGISTER(Poller轮询时会用到)。
  6. 将新的PollerEvent添加到这个Poller的事件列表events，等待Poller线程轮询。

```java
public void register(final NioChannel socket) {
    // 1.创建一个NioSocketWrapper对象,包装NioChannel实例
    socket.setPoller(this);
    NioSocketWrapper ka = new NioSocketWrapper(socket, NioEndpoint.this);
    socket.setSocketWrapper(ka);
    
    // 2.配置NioSocketWrapper相关属性
    ka.setPoller(this);
    ka.setReadTimeout(getSocketProperties().getSoTimeout());
    ka.setWriteTimeout(getSocketProperties().getSoTimeout());
    ka.setKeepAliveLeft(NioEndpoint.this.getMaxKeepAliveRequests());
    ka.setSecure(isSSLEnabled());
    ka.setReadTimeout(getConnectionTimeout());
    ka.setWriteTimeout(getConnectionTimeout());
    
    // 3.设置interestOps为SelectionKey.OP_READ
    ka.interestOps(SelectionKey.OP_READ);
    
    // 4.从缓存中取出一个PollerEvent对象，若没有则创建。
    PollerEvent r = eventCache.pop();
    if ( r==null) {
        r = new PollerEvent(socket,ka,OP_REGISTER);
    } else {
        // 5.初始化或者重置此Event对象，会将其interestOps设置为OP_REGISTER(Poller轮询时会用到)
        r.reset(socket,ka,OP_REGISTER);
    }
    
    /**
    * 6.将新的PollerEvent添加到这个Poller的事件列表events，等待Poller线程轮询.
    *   事件列表的数据结构为Tomcat自定义的同步队列SynchronizedQueue,本质上是对象数组.
    **/
    addEvent(r);
}
```



#### 5.3Poller工作方式

##### 5.3.1Poller.run()方法

- 逻辑处理流程
  1. 遍历events，将PollerEvent的Channel注册到Poller的Selector中。
  2. 如果阻塞等待的select方法超时或者被唤醒,即当前Selector中还没有就绪的Channel,先将events队列中的Channel注册到Selector上.(events队列会由Acceptor线程持续更新添加)。
  3. Selector调用selectedKeys(),获取IO数据已经就绪的Channel,遍历并调用processKey方法来处理每一个Channel就绪的事件。

```java
public void run() {
    // 一直循环直到destory()被调用
    while (true) {

        boolean hasEvents = false;

        try {
            // 当NioEndpoint处于非关闭状态
            if (!close) {
                // 1.遍历events，将PollerEvent的Channel注册到Poller的Selector中
                hasEvents = events();
                if (wakeupCounter.getAndSet(-1) > 0) {
                   	// 调用非阻塞的select方法,直接返回Selector中就绪Channel的数量.
                    keyCount = selector.selectNow();
                } else {
                    // 如果Selector中还没有就绪的Channel,则阻塞等待操作系统返回就绪的Channel,有超时时间.
                    keyCount = selector.select(selectorTimeout);
                }
                wakeupCounter.set(0);
            }
            
            // 当NioEndpoint处于关闭状态
            if (close) {
               ......
            }
        } catch (Throwable x) {
            ......
        }
        
        /**
        * 2.如果阻塞等待的select方法超时或者被唤醒,即当前Selector中还没有就绪的Channel,先将events队列中的
        * Channel注册到Selector上.(events队列会由Acceptor线程持续更新添加)
        **/
        if ( keyCount == 0 ) {
            hasEvents = (hasEvents | events());
        }
		
        /**
        * 3.Selector调用selectedKeys(),获取IO数据已经就绪的Channel,遍历并调用processKey方法来处理每一个
        * Channel就绪的事件.
        **/
        Iterator<SelectionKey> iterator =
            keyCount > 0 ? selector.selectedKeys().iterator() : null;
        
        while (iterator != null && iterator.hasNext()) {
            SelectionKey sk = iterator.next();
            NioSocketWrapper attachment = (NioSocketWrapper)sk.attachment();
            
            // 如果其它线程已调用，则Attachment可能为空
            if (attachment == null) {
                iterator.remove();
            } else {
                iterator.remove();
                // 调用processKey来处理该Socket的IO,创建一个SocketProcessor,放入Tomcat线程池去执行.
                processKey(sk, attachment);
            }
        }

       
        timeout(keyCount,hasEvents);
    }

    getStopLatch().countDown();
}
```



##### 5.3.2Poller.events()方法和PollerEvent.run()方法

- 逻辑处理流程
  1. 遍历所有的PollerEvent事件对象。
  2. 把SocketChannel的interestOps注册到Poller的Selector中。

```java
/**
* Poller.events()方法定义
**/ 
public boolean events() {
    boolean result = false;

    PollerEvent pe = null;
    // 1.遍历所有的PollerEvent事件对象
    for (int i = 0, size = events.size(); i < size && (pe = events.poll()) != null; i++ ) {
        result = true;
        try {
            // 2.把SocketChannel的interestOps注册到Selector中
            pe.run();
            pe.reset();
            if (running && !paused) {
                eventCache.push(pe);
            }
        } catch ( Throwable x ) {
            ......
        }
    }
    return result;
}

/**
* PollerEvent.run()方法定义
**/ 
public void run() {
    // Acceptor调用Poller.register()方法时，创建的PollerEvent的interestOps为OP_REGISTER
    if (interestOps == OP_REGISTER) {
        try {
            // 将PollerEvent的Channel注册到Poller的Selector中,interestOps为OP_READ.
            socket.getIOChannel().register(
                socket.getPoller().getSelector(), SelectionKey.OP_READ, socketWrapper);
        } catch (Exception x) {
            log.error(sm.getString("endpoint.nio.registerFail"), x);
        }
    } else {
        ......
    }
}
```



##### 5.3.3Poller.processKey(sk,attachment)方法和AbstractEndpoint.processSocket(...)方法

- 逻辑处理流程
  1. 从缓存获取一个SocketProcessor实例,如果没有则创建一个。SocketProcessor是一个任务抽象，用来将SocketChannel对接到Servlet容器上。
  2. 获取执行器实例,执行SocketProcessor实例。

```java
/**
* Poller.processKey(sk,attachment)方法定义
* 作用：对每个就绪通道创建一个SocketProcessor,交由Tomcat线程池去处理
**/
protected void processKey(SelectionKey sk, NioSocketWrapper attachment) {
    try {
        if ( close ) {
            ......
        } else if ( sk.isValid() && attachment != null ) {
            if (sk.isReadable() || sk.isWritable() ) {
                if ( attachment.getSendfileData() != null ) {
                    processSendfile(sk,attachment, false);
                } else {
                    ......
                        
                    if (sk.isReadable()) {
                        // processSocket用来处理通道
                        if (!processSocket(attachment, SocketEvent.OPEN_READ, true)) {
                            closeSocket = true;
                        }
                    }
                    if (!closeSocket && sk.isWritable()) {
                        if (!processSocket(attachment, SocketEvent.OPEN_WRITE, true)) {
                            closeSocket = true;
                        }
                    }
                    ......
                }
            }
        } else {
            ......
        }
    } catch ( CancelledKeyException ckx ) {
        ......
    } catch (Throwable t) {
        ......
    }
}

/**
* AbstractEndpoint.processSocket(...)方法定义
* 作用：对每个就绪通道创建一个SocketProcessor,交由Tomcat线程池去处理
**/
public boolean processSocket(SocketWrapperBase<S> socketWrapper,
                             SocketEvent event, boolean dispatch) {
    try {
        ......
            
        // 1.从缓存获取一个SocketProcessor实例,如果没有则创建一个.
        SocketProcessorBase<S> sc = processorCache.pop();
        if (sc == null) {
            sc = createSocketProcessor(socketWrapper, event);
        } else {
            sc.reset(socketWrapper, event);
        }
        
        // 2.获取执行器实例,执行SocketProcessor实例.
        Executor executor = getExecutor();
        if (dispatch && executor != null) {
            executor.execute(sc);
        } else {
            sc.run();
        }
    } catch (RejectedExecutionException ree) {
        ......
        return false;
    } catch (Throwable t) {
        ......
        return false;
    }
    return true;
}
```



#### 5.4SocketProcessor处理请求

-  请求处理流程

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/11_SocketProcessor处理请求过程.png)

- 封装Request和Resoponse

  1. 在类Http11Processor中封装, 调用`prepareRequest`方法来准备好请求数据。

  2. 调用`CoyoteAdapter`的`service`方法进行`request`和`response`的适配，之后交给`Tomcat`容器进行处理。 

  3. Request的转化流程

     `org.apache.coyote.Request`

     `org.apache.catalina.connector.Request`

     `org.apache.catalina.servlet4preview.http.HttpServletRequest`

     `javax.servlet.http.HttpServletRequest`