## Feign源码解析

### 1.Feign初始化并生成动态代理

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Feignbuilder构建接口代理.png)

####1.1**Feign静态内部类Builder**

- 通过`Feign.builder()`生成`Feign.Builder`对象，然后设置相关的参数，再调用target方法构造代理对象。

    ```java
    public static class Builder {
        // 拦截器,组装完RequestTemplate,发请求之前的拦截处理RequestTemplate
        private final List<RequestInterceptor> requestInterceptors =
            new ArrayList<RequestInterceptor>();
        // 日志级别
        private Logger.Level logLevel = Logger.Level.NONE;
        // 契约模型,默认为Contract.Default.用户创建MethodMetadata.
        // spring cloud就是扩展这个实现springMVC注解
        private Contract contract = new Contract.Default();
        // 客户端,默认为Client.Default,可以扩展ApacheHttpClient/OKHttpClient/RibbonClient等
        private Client client = new Client.Default(null, null);
        // 重试设置,默认不设置
        private Retryer retryer = new Retryer.Default();
        // 日志,可以接入Slf4j
        private Logger logger = new NoOpLogger();
        // 编码器,用于请求参数body的编码
        private Encoder encoder = new Encoder.Default();
        // 解码器,用户response的解码
        private Decoder decoder = new Decoder.Default();
        // 用@QueryMap注解的参数编码器
        private QueryMapEncoder queryMapEncoder = new FieldQueryMapEncoder();
        // 请求错误解码器
        private ErrorDecoder errorDecoder = new ErrorDecoder.Default();
        // 参数配置，主要是超时时间之类的
        private Options options = new Options();
        // 动态代理工厂
        private InvocationHandlerFactory invocationHandlerFactory =
            new InvocationHandlerFactory.Default();
        // 是否decode404
        private boolean decode404;
        private boolean closeAfterDecode = true;
        private ExceptionPropagationPolicy propagationPolicy = NONE;
        private boolean forceDecoding = false;
        private List<Capability> capabilities = new ArrayList<>();
    }
    ```

#### 1.2ReflectFeign类

- **类的创建**

  1. `Feign.Builder`对象调用`target()`方法，再调用`Feign.build()`方法新建一个`ReflectFeign`对象 ， 然后调用`ReflectFeign`的`newInstance`方法创建动态代理。
  
  ```java
// 默认使用HardCodedTarget
  public <T> T target(Class<T> apiType, String url) {
      return target(new HardCodedTarget<T>(apiType, url));
  }
  
  // 调用ReflectFeign的newInstance方法创建动态代理
  public <T> T target(Target<T> target) {
      return build().newInstance(target);
  }
  
  // 主要用来构建ReflectFeign对象
  public Feign build() {
      SynchronousMethodHandler.Factory synchronousMethodHandlerFactory =
          new SynchronousMethodHandler.Factory(client, retryer, requestInterceptors, logger,
                                               logLevel, decode404, closeAfterDecode);
      
      // 封装Feign.Builder对象的属性
      ParseHandlersByName handlersByName =
          new ParseHandlersByName(contract, options, encoder, decoder, queryMapEncoder,
                                  errorDecoder, synchronousMethodHandlerFactory);
      /**
      * 1. handlersByName将所有参数进行封装，并提供解析接口方法的逻辑
      * 2. invocationHandlerFactory是Builder的属性，默认值是InvocationHandlerFactory.Default,
      *    用创建java动态代理的InvocationHandler实现
      **/
      return new ReflectiveFeign(handlersByName, invocationHandlerFactory, queryMapEncoder);
  }
  
  ```
  
- **构造方法**

  1. **ParseHandlersByName**：将`builder`所有参数进行封装，并提供解析接口方法的逻辑。
  2. **InvocationHandlerFactory**：`java`动态代理的`InvocationHandler`的工厂类，默认值是`InvocationHandlerFactory.Default`
  3. **QueryMapEncoder**：接口参数注解`@QueryMap`时，参数的编码器。

  ```java
  ReflectiveFeign(ParseHandlersByName targetToHandlersByName, InvocationHandlerFactory factory,
        QueryMapEncoder queryMapEncoder) {
      this.targetToHandlersByName = targetToHandlersByName;
      this.factory = factory;
      this.queryMapEncoder = queryMapEncoder;
    }
  ```

- **创建动态代理对象`newInstance()`方法**

  1. 创建`MethodHandler`的映射，这里创建的是实现类`SynchronousMethodHandler`。
  2. 通过`InvocationHandlerFatory`创建`InvocationHandler`。
  3. 绑定接口的`default`方法，通过`DefaultMethodHandler`绑定。

  ```java
  // Target封装了,传入的接口和URL
  public <T> T newInstance(Target<T> target) {
    // targetToHandlersByName是构造器传入的ParseHandlersByName对象，根据target对象生成MethodHandler映射
    Map<String, MethodHandler> nameToHandler = targetToHandlersByName.apply(target);
    Map<Method, MethodHandler> methodToHandler = new LinkedHashMap<Method, MethodHandler>();
    List<DefaultMethodHandler> defaultMethodHandlers = new LinkedList<DefaultMethodHandler>();
      
      
    /**
    * 接口中方法分为两类,SynchronousMethodHandler和DefaultMethodHandler.
    * 1. 都实现了InvocationHandlerFactory.MethodHandler接口.
    * 2. DefaultMethodHandler不会进行代理,直接绑定在动态代理对象上,不进行invoke()调用.
    * 3. SynchronousMethodHandler会进行invoke()方法调用.
    **/
    // 1.遍历接口所有方法，构建Method->MethodHandler的映射
    for (Method method : target.type().getMethods()) {
        // 返回该method对象的类Class,如果是Object中的方法,不做代理.
        if (method.getDeclaringClass() == Object.class) {
            continue;
        } 
        // 如果不是public abstarct的方法,不做代理.定义为DefaultMethodHandler
        else if(Util.isDefault(method)) {
            // 接口default方法的Handler，这类方法直接调用
            DefaultMethodHandler handler = new DefaultMethodHandler(method);
            defaultMethodHandlers.add(handler);
            methodToHandler.put(method, handler);
        } 
        else {
            methodToHandler.put(method, nameToHandler.get(Feign.configKey(target.type(), method)));
        }
    }
      
    // 这里factory是构造其中传入的,创建InvocationHandler,代理对象会调用其invoke方法实现代理.
    InvocationHandler handler = factory.create(target, methodToHandler);
      
    // 2.Java动态代理,Proxy.newProxyInstance用来获取代理对象
    T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(), 
                                         new Class<?>[]{target.type()}, handler);
      
    // 3.将default方法直接绑定到动态代理对象上
    for(DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers) {
      // 直接调用MethodHandle.bindTo(),MethodHandle类是对象方法的句柄,可以直接调用
      defaultMethodHandler.bindTo(proxy);
    }
      
    return proxy;
  }
  
  // JDK8中接口可以有static方法,如果不是public abstarct的方法,不做代理
  public static boolean isDefault(Method method) {
      final int SYNTHETIC = 0x00001000;
      return ((method.getModifiers()
               & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC | SYNTHETIC)) 
              == Modifier.PUBLIC)
          && method.getDeclaringClass().isInterface();
  }
  ```



#### 1.3SynchronousMethodHandler方法映射类

- **类的创建**

  1. `ReflectFeign`调用`ReflectFeign.apply()`方法，根据`method`创建`MethodHandler`类。

- **创建流程**

  1. 通过`Contract`解析接口方法，生成`MethodMetadata`，默认的`Contract`解析`Feign`自定义的http注解。
  2. 根据`MethodMetadata`方法元数据生成特定的`RequestTemplate`(请求模板)的工厂。
  3. 使用`SynchronousMethodHandler.Factory`工厂创建`SynchronousMethodHandler`。
  4. 这里有两个工厂不要搞混淆了，`SynchronousMethodHandler`工厂和`RequestTemplate`工厂，`SynchronousMethodHandler`的属性包含`RequestTemplate`工厂。

  ```java
  /**
  * 根据target对象,为每个method创建MethodHandler类,此类封装了target接口的被代理的方法.
  **/
  public Map<String, MethodHandler> apply(Target key) {
      // 通过contract解析接口方法，生成MethodMetadata列表，默认的contract解析Feign自定义的http注解
      List<MethodMetadata> metadata = contract.parseAndValidatateMetadata(key.type());
      
      // 初始化返回Map,有顺序的map
      Map<String, MethodHandler> result = new LinkedHashMap<String, MethodHandler>();
      
      for (MethodMetadata md : metadata) {
          // BuildTemplateByResolvingArgs实现RequestTemplate.Factory(RequestTemplate的工厂)
          BuildTemplateByResolvingArgs buildTemplate;
          // 根据方法元数据，使用不同的RequestTemplate的工厂
          if (!md.formParams().isEmpty() && md.template().bodyTemplate() == null) {
              // 如果有formParam，并且bodyTemplate不为空，请求体为x-www-form-urlencoded格式
              // 将会解析form参数，填充到bodyTemplate中
              buildTemplate = new BuildFormEncodedTemplateFromArgs(md, encoder, queryMapEncoder);
          } else if (md.bodyIndex() != null) {
              // 如果包含请求体，将会用encoder编码请求体对象
              buildTemplate = new BuildEncodedTemplateFromArgs(md, encoder, queryMapEncoder);
          } else {
              // 默认的RequestTemplate的工厂，没有请求体，不需要编码器
              buildTemplate = new BuildTemplateByResolvingArgs(md, queryMapEncoder);
          }
          // 使用工厂SynchronousMethodHandler.Factory创建SynchronousMethodHandler
          result.put(md.configKey(),
                     factory.create(key, md, buildTemplate, options, decoder, errorDecoder));
      }
      return result;
  }
  ```



#### 1.4MethodMetadata类

- **类的创建**

  1. `Contract`解析接口方法生成`MethodMetadata`，解析方法为`contract.parseAndValidatateMetadata()`。
  2.  `feign`默认的解析器是`Contract.Default`继承了`Contract.BaseContract`。

- **解析流程**

  1.  先处理类上的注解，再处理方法上注解，最后处理方法参数注解 。

  ```java
  public final class MethodMetadata implements Serializable {
      // 标识方法的key，接口名加方法签名：GitHub#contributors(String,String)
      private String configKey;
      // 方法返回值类型
      private transient Type returnType;
      // uri参数的位置，方法中可以写个uri参数，发请求时直接使用这个参数
      private Integer urlIndex;
      // body参数的位置，只能有一个未注解的参数为body，否则报错
      private Integer bodyIndex;
      // headerMap参数的位置
      private Integer headerMapIndex;
      // @QueryMap注解参数位置
      private Integer queryMapIndex;
      // @QueryMap注解里面encode参数，是否已经urlEncode编码过了
      private boolean queryMapEncoded;
      // body的类型
      private transient Type bodyType;
      // RequestTemplate 原型
      private RequestTemplate template = new RequestTemplate();
      // form请求参数
      private List<String> formParams = new ArrayList<String>();
      // 方法参数位置和名称的map
      private Map<Integer, Collection<String>> indexToName ;
      // @Param中注解的expander方法，可以指定解析参数类
      private Map<Integer, Class<? extends Expander>> indexToExpanderClass ;
      // 参数是否被urlEncode编码过了，@Param中encoded方法
      private Map<Integer, Boolean> indexToEncoded ;
      // 自定义的Expander
      private transient Map<Integer, Expander> indexToExpander;
  }
  ```



#### 1.5初始化流程

- **主要类**
  1. `ReflectiveFeign`初始化入口。
  2. `FeignInvocationHandler`实现动态代理的`InvocHandler`。
  3. `SynchronousMethodHandler`方法处理器，方法调用处理器。
  4. `MethodMetadata`方法元数据。

- **Feign初始化结构为动态代理的整个过程** 
  1. 初始化`Feign.Builder`传入参数，构造`ReflectiveFeign`。
  2. `ReflectiveFeign`通过内部类`ParseHandlersByName`的`Contract`属性，解析接口生成`MethodMetadata`。
  3. `ParseHandlersByName`根据`MethodMetadata`生成`RequestTemplate`工厂。
  4. `ParseHandlersByName`创建`SynchronousMethodHandler`，传入`MethodMetadata`、`RequestTemplate`工厂和`Feign.Builder`相关参数。
  5. `ReflectiveFeign`创建`FeignInvocationHandler`，传入参数`SynchronousMethodHandler`，绑定`DefaultMethodHandler`。
  6. `ReflectiveFeign`根据`FeignInvocationHandler`创建`Proxy`。



### 2.接口调用

#### 2.1接口调用执行流程

1. 代理层：动态代理调用层。
2. 转换层：方法转http请求，解码http响应。
3. 网络层：http请求发送。

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/2_接口调用执行流程.png)



#### 2.2 FeignInvocationHandler.invoke()

- **工作流程**

  1. `java`动态代理接口方法调用，会调用到`InvocaHandler`的`invoke`方法。
  2. `feign`里面实现类是`FeignInvocationHandler`。
  
  ```java
  private final Map<Method, MethodHandler> dispatch;
  
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
  
  return dispatch.get(method).invoke(args);
  }
  ```
  
  

#### 2.3SynchronousMethodHandler.invoke()

- **工作流程**

  1. 根据方法找到`MethodHandler`，除接口的`default`方法外，找到的是`SynchronousMethodHandler`对象，然后调用`SynchronousMethodHandlerd.invoke`方法。
  2. 生成`RquestTemplate`，将请求参数转换为`Request`，调用`client`客户端发送`http`请求，最后`Decoder`解析`Response`。

  ```java
  /**
  * SynchronousMethodHandler.invoke()方法
  **/
  public Object invoke(Object[] argv) throws Throwable {
    // buildTemplateFromArgs是RequestTemplate工程对象，根据方法参数创建RequestTemplate
    RequestTemplate template = buildTemplateFromArgs.create(argv);
    // 重试设置
    Retryer retryer = this.retryer.clone();
    while (true) {
      try {
        // 执行和解码
        return executeAndDecode(template);
      } catch (RetryableException e) {
        retryer.continueOrPropagate(e);
        。。。
        continue;
      }
    }
  }
  
  /**
  * 执行HTTP请求访问,解码响应报文
  **/
  Object executeAndDecode(RequestTemplate template) throws Throwable {
    //RequestTemplate转换为Request
    Request request = targetRequest(template)
    。。。
    Response response;
    long start = System.nanoTime();
    try {
      response = client.execute(request, options);
      response.toBuilder().request(request).build();
    } catch (IOException e) {
      。。。
      throw errorExecuting(request, e);
    }
    long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
  
    boolean shouldClose = true;
    try {
      。。。
      if (Response.class == metadata.returnType()) {
        //如果接口方法返回的是Response类
        if (response.body() == null) {
          //body为空，直接返回
          return response;
        }
        if (response.body().length() == null ||
                response.body().length() > MAX_RESPONSE_BUFFER_SIZE) {
          //body不为空，且length>最大缓存值，返回response，但是不能关闭response
          shouldClose = false;
          return response;
        }
        // 读取body字节数组，返回response
        byte[] bodyData = Util.toByteArray(response.body().asInputStream());
        return response.toBuilder().body(bodyData).build();
      }
      if (response.status() >= 200 && response.status() < 300) {
        //响应成功
        if (void.class == metadata.returnType()) {
          //接口返回void
          return null;
        } else {
          //解码response，直接调用decoder解码
          Object result = decode(response);
          shouldClose = closeAfterDecode;
          return result;
        }
      } else if (decode404 && response.status() == 404 && void.class != metadata.returnType()) {
        //404解析
        Object result = decode(response);
        shouldClose = closeAfterDecode;
        return result;
      } else {
        //其他返回码，使用errorDecoder解析，抛出异常
        throw errorDecoder.decode(metadata.configKey(), response);
      }
    } catch (IOException e) {
      throw errorReading(request, response, e);
    } finally {
      //是否需要关闭response，根据Feign.Builder 参数设置是否要关闭流
      if (shouldClose) {
        ensureClosed(response.body());
      }
    }
  }
  ```

  

#### 2.4RquestTemplate类

- **构建RquestTemplate类**

  1. 在`SynchronousMethodHandler.invoke`方法中生成`RequestTemplate`
     ```java
     // buildTemplateFromArgs是RequestTemplate.Factory实现类
     RequestTemplate template = buildTemplateFromArgs.create(argv);
     ```
  2. `RequestTemplate.Factory`有三个实现类
     
     - **BuildTemplateByResolvingArgs**：`RequestTemplate`工厂。
     - **BuildEncodedTemplateFromArgs**：`BuildTemplateByResolvingArgs`的子类，重载`resolve`方法，解析`form`表单请求。
     - **BuildFormEncodedTemplateFromArgs**：`BuildTemplateByResolvingArgs`的子类，重载`resolve`方法，解析`body`请求。

  

- **将RquestTemplate转换为Request**

  1. `SynchronousMethodHandler`的`targetRequest`方法将`RequestTemplate`转换为`Request`。

     ```java
     Request targetRequest(RequestTemplate template) {
       // 先应用所用拦截器，拦截器是在Feign.Builder中传入的，拦截器可以修改RequestTemplate信息
       for (RequestInterceptor interceptor : requestInterceptors) {
         interceptor.apply(template);
       }
       // 调用Target的apply方法，默认Target是HardCodedTarget
       return target.apply(new RequestTemplate(template));
     }
     ```

     

  2. 先应用所有拦截器，然后调用`target`的`apply`方法。

     ```java
     // 1.HardCodedTarget的apply方法
     public Request apply(RequestTemplate input) {
         if (input.url().indexOf("http") != 0) {
             input.insert(0, url());
         }
         // 调用RequestTemplate的request方法
         return input.request();
     }
     
     // 2.RequestTemplate的request方法
     public Request request() {
         // 安全拷贝所有header
         Map<String, Collection<String>> safeCopy = 
             new LinkedHashMap<String, Collection<String>>();
         safeCopy.putAll(headers);
         // 调用Request的create静态方法
         return Request.create(
             method, url + queryLine(),
             Collections.unmodifiableMap(safeCopy),
             body, charset
         );
     }
     
     // 3.Request的create方法
     public static Request create(String method, String url, Map<String, Collection<String>> headers,
                                  byte[] body, Charset charset) {
         //new 对象
         return new Request(method, url, headers, body, charset);
     }
     ```

     

  3. 拦截器可以在构造好`RequestTemplate`后和发请求前修改请求信息。

  4. `target`默认使用`HardCodedTarget`直接发请求，`feign`还提供了`LoadBalancingTarget`，适配`Ribbon`来发请求，实现客户端的负载均衡。

     ```java
     // 实现负载均衡
     public Request apply(RequestTemplate input) {
       // 选取一个Server，lb是Ribbon的AbstractLoadBalancer类
       Server currentServer = lb.chooseServer(null);
       // 生成url
       String url = format("%s://%s%s", scheme, currentServer.getHostPort(), path);
       input.insert(0, url);
       try {
         // 生成Request
         return input.request();
       } finally {
         lb.getLoadBalancerStats().incrementNumRequests(currentServer);
       }
     }
     ```

- **发送HTTP请求**

  1. `SynchronousMethodHandler`中构造好`Request`后，直接调用`client`的`execute`方法发送请求。

     ```java
     response = client.execute(request, options);
     ```

     

  2. `client`是一个`Client`接口，默认实现类是`Client.Default`，使用`java api`中的`HttpURLConnection`发送`http`请求。

  3. `feign`的其他`HTTP`客户端，
     - `ApacheHttpClient`
     - `OkHttpClient`
     - `RibbonClient`：实现客户端负载均衡。

#### 2.5接口调用流程

1. 接口的动态代理`Proxy`调用接口方法会执行的`FeignInvocationHandler`。
2. `FeignInvocationHandler`通过方法签名在属性`Map`<`Method`, `MethodHandler`> `dispatch`中找到`SynchronousMethodHandler`，调用`invoke`方法。
3. `SynchronousMethodHandler`的`invoke`方法根据传入的方法参数，通过自身属性工厂对象`RequestTemplate.Factory`创建`RequestTemplate`，工厂里面会用根据需要进行`Encode`。
4. `SynchronousMethodHandler`遍历自身属性`RequestInterceptor`列表，对`RequestTemplate`进行改造。
4. `SynchronousMethodHandler`调用自身`Target`属性的`apply`方法，将`RequestTemplate`转换为`Request`对象。
5. `SynchronousMethodHandler`调用自身`Client`的`execute`方法，传入`Request`对象。
6. `Client`将`Request`转换为`http`请求，发送后将`http`响应转换为`Response`对象。
7. `SynchronousMethodHandler`调用`Decoder`的方法对`Response`对象解码后返回。
8. 返回的对象最后返回到`Proxy`。



### 3.Feign的扩展点

- **Contract契约**
  1. `Contract`的作用是解析接口方法，生成`Rest`定义。`feign`默认使用自己的定义的注解，还提供了`JAXRSContract`作为`javax.ws.rs`注解接口实现。
  2. `SpringContract`是`SpringCloud`提供`SpringMVC`注解实现方式。

- **InvocationHandler动态代理handler**
  1. 通过`InvocationHandlerFactory`注入到`Feign.Builder`中，`feign`提供了`Hystrix`的扩展，实现`Hystrix`接入。

- **Encoder请求body编码器**
  1. 默认编码器，只能处理`String`和`byte[]`。
  2. `json`编码器`GsonEncoder`、`JacksonEncoder`。
  3. `XML`编码器`JAXBEncoder`。

- **Decoder响应解码器**
  1. `json`解码器 `GsonDecoder`、`JacksonDecoder`。
  2. `XML`解码器 `JAXBDecoder`。
  3. `Stream`流解码器 `StreamDecoder`。

- **Target请求转换器**
  1. `HardCodedTarget`默认`Target`，不做任何处理。
  2. `LoadBalancingTarget`使用`Ribbon`进行客户端路由。

- **Client 发送http请求的客户端**
  1. `Client.Default`默认实现，使用`java api`的`HttpClientConnection`发送http请求。
  2. `ApacheHttpClient`使用`apache`的`Http`客户端发送请求。
  3. `OkHttpClient`使用`OKHttp`客户端发送请求。
  4. `RibbonClient`使用`Ribbon`进行客户端路由。

- **RequestInterceptor请求拦截器**
  1. 调用客户端发请求前，修改`RequestTemplate`，比如为所有请求添加`Header`就可以用拦截器实现。

- **Retryer重试策略**
  1. 默认的策略是`Retryer.Default`，包含3个参数：间隔、最大间隔和重试次数，第一次失败重试前会`sleep`输入的间隔时间的，后面每次重试`sleep`时间是前一次的1.5倍，超过最大时间或者最大重试次数就失败。