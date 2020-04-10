## OpenFeign基础

###1.Feign应用实例

- **依赖包配置**

  ```xml
  <!-- 原生feign框架 -->
  <dependency>
      <groupId>io.github.openfeign</groupId>
      <artifactId>feign-gson</artifactId>
      <version>10.9</version>
  </dependency>
  ```

- **定义访问接口**

  ```java
  public interface SkuCustomerQueryService {
  
      // GET方式
      @RequestLine("GET /skucuslevel/customerlevelDownBox?productFamilyCode={productFamilyCode}")
      String customerlevelDownBox(@Param("productFamilyCode")String productFamilyCode);
  
      // POST方式
      @RequestLine("POST /skucuslevel/pageInfo")
      @Headers("Content-Type: application/json")
      String pageinfo(@Param("current")Integer current,
                      @Param("size")Integer size,
                      @Param("crmOrgCode")String crmOrgCode,
                      @Param("productFamilyCode")String productFamilyCode,
                      @Param("productFamilyName")String productFamilyName);
  }
  ```

- **初始化访问客户端**

  ```JAVA
  public class FeignTestController {
      
      public static void main(String[] args){
          // Feign.builder()初始化一个HTTP客户端
          SkuCustomerQueryService skuCustomerQueryService = Feign.builder()
                  .encoder(new GsonEncoder()) // 编码器,编码参数放入请求报文
                  .decoder(new GsonDecoder()) // 解码器,解码响应报文中的JSON串
                  .target(SkuCustomerQueryService.class, "http://10.73.129.229:11083");
  
          // 以调用接口的方式,实现HTTP访问
          String postResult = skuCustomerQueryService.pageinfo(1,10,
                  "107060000","01","TV");
  
          String getResult = skuCustomerQueryService.customerlevelDownBox("01");
  
          System.out.println(getResult);
      }
  }
  ```



###2.Feign介绍

- **框架目的**

  `Feign`的目的是，尽量减少资源和代码来实现和`HTTP API`的连接。通过自定义的编码解码器以及错误处理，可以编写任何基于文本的`HTTP API`。 

- **工作机制**

  1. 在接口方法上加注解，定义rest请求，构造出接口的动态代理对象。
  2. 然后通过调用接口方法就可以发送http请求，并且自动解析http响应为方法返回值，极大的简化了客户端调用rest api的代码。 
  
  
###3.Feign集成其他开源模块

- **Gson**

  1. `Gson`包含了一个编码器和一个解码器，可以被用于`JSON`格式的`API`。
  2. `Feign`已经自带了`Gson`，不用重新引入。

  ```java
  GsonCodec codec = new GsonCodec();
  GitHub github = Feign.builder()
                       .encoder(new GsonEncoder())
                       .decoder(new GsonDecoder())
                       .target(GitHub.class, "https://api.github.com");
  ```

- **Jackson**

  1. `Jackson`包含了一个编码器和一个解码器，可以被用于`JSON`格式的`API`。 
  2. 由于`Feign`没有自带，需要引入依赖。

  ```java
  GitHub github = Feign.builder()
                       .encoder(new JacksonEncoder())
                       .decoder(new JacksonDecoder())
                       .target(GitHub.class, "https://api.github.com");
  ```

  ```xml
  <dependency>
      <groupId>com.netflix.feign</groupId>
      <artifactId>feign-jackson</artifactId>
      <version>8.18.0</version>
  </dependency>
  ```

  

- **JAX-RS**

  1.  `JAXB`包含了一个编码器和一个解码器，可以被用于`XML`格式的`API`，需要引入依赖。

  ```java
  api = Feign.builder()
             .encoder(new JAXBEncoder())
             .decoder(new JAXBDecoder())
             .target(Api.class, "https://apihost");
  ```

  ```xml
  <dependency>
      <groupId>com.netflix.feign</groupId>
      <artifactId>feign-jaxb</artifactId>
      <version>8.18.0</version>
  </dependency>
  ```

  

- **OkHttp**

  1. `OkHttpClient`使用`OkHttp`来发送`Feign`的请求，`OkHttp`支持`SPDY`(SPDY是Google开发的基于TCP的传输层协议，用以最小化网络延迟，提升网络速度，优化用户的网络使用体验),并有更好的控制http请求。
  2. 需要配置Htpp客户端，并且引入依赖。

  ```java
  GitHub github = Feign.builder()
                       .client(new OkHttpClient())
                       .target(GitHub.class, "https://api.github.com");
  ```

  ```xml
  <dependency>
      <groupId>com.netflix.feign</groupId>
      <artifactId>feign-okhttp</artifactId>
      <version>8.18.0</version>
  </dependency>
  ```

  

- **Ribbon**

  1. `RibbonClient`重写了`Feign`客户端的对URL的处理，其添加了智能路由以及一些其他的弹性功能。
  2.  集成`Ribbon`需要将`ribbon`的客户端名称当做url的host部分来传递。

  ```java
  // myAppProd是你的ribbon client name
  MyService api = Feign.builder()
      .client(RibbonClient.create())
      .target(MyService.class, "https://myAppProd");
  ```

  ```xml
  <dependency>
      <groupId>com.netflix.feign</groupId>
      <artifactId>feign-ribbon</artifactId>
      <version>8.18.0</version>
  </dependency>
  ```

  

- **Hystrix**

  1. `HystrixFeign`配置了`Hystrix`提供的熔断机制。
  2. 需要添加`Hystrix`模块到你的环境变量，然后使用`HystrixFeign`来构造`API`。

  ```java
  MyService api = HystrixFeign.builder().target(MyService.class, "https://myAppProd");
  ```

  ```xml
  <dependency>
      <groupId>com.netflix.feign</groupId>
      <artifactId>feign-hystrix</artifactId>
      <version>8.18.0</version>
  </dependency>
  ```

  

- **SLF4J**

  1. `SLF4JModule`可以使用`SLF4J`作为`Feign`的日志记录模块，可以使用`Logback`,`Log4J`等框架来记录日志。
  2. 需要添加`SLF4J`模块和对应的日志记录实现模块(比如`Log4J`)到环境变量，然后配置`Feign`使用`Slf4jLogger`。

  ```java
  GitHub github = Feign.builder()
                       .logger(new Slf4jLogger())
                       .target(GitHub.class, "https://api.github.com");
  ```

  ```xml
  <dependency>
      <groupId>com.netflix.feign</groupId>
      <artifactId>feign-slf4j</artifactId>
      <version>8.18.0</version>
  </dependency>
  ```

  

  



