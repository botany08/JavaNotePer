## 服务注册中心Eureka

### 1.搭建Eureka

- **引入依赖**

  ```xml
  <dependencies>
      <!-- Eureka服务端 -->
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
      </dependency>
  
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-test</artifactId>
          <scope>test</scope>
      </dependency>
  </dependencies>
  ```

  

- **配置文件**

  1. `server.port`：为了与后续要进行注册的服务区分，这里将服务注册中心的端口设置为7000。
  2. `eureka.client.register-with-eureka`：表示是否将自己注册到EurekaServer，默认为true。
  3. `eureka.client.fetch-registry`：表示是否从EurekaServer获取注册信息，默认为true。
  4. `eureka.client.service-url.defaultZone`：设置与`EurekaServer`交互的地址，查询服务和注册服务都需要依赖这个地址。默认是http://localhost:8761/eureka，多个地址可使用英文逗号(,)分隔。

  ```yml
  spring:
    application:
      name: eureka-server
  server:
    port: 7000
  eureka:
    instance:
      hostname: localhost
    client:
      register-with-eureka: false
      fetch-registry: false
      service-url:
        defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  
  ```

  

- **启动类注解@EnableEurekaServer**

  1. `@EnableEurekaServer`启动一个服务注册中心提供给其他应用进行对话。

  ```java
  @EnableEurekaServer
  @SpringBootApplication
  public class EurekaServerApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(EurekaServerApplication.class, args);
      }
  }
  ```

  

### 2.集群

####2.1双节点注册中心

- **部署背景**

  1. 在一个分布式系统中，服务注册中心是最重要的基础部分，理应随时处于可以提供服务的状态。为了维持其可用性，使用集群是很好的解决方案。
  2. `Eureka`通过互相注册的方式来实现高可用的部署，只需要将`EurekeServer`配置其他可用的`service-url`就能实现高可用部署。

- **配置文件**

  1. 通过互相注册，分别启动两个应用，就可以部署相关节点。
  2. 在搭建`EurekaServer`双节点或集群的时候，要把`eureka.client.register-with-eureka`和`eureka.client.fetch-registry`均改为`true`(默认)。
  3. 在注册的时候，配置文件中的`spring.application.name`必须一致。

  ```yml
  # EurekaServer节点1
  spring:
    application:
      name: eureka-server
  server:
    port: 7001
  eureka:
    instance:
      hostname: peer1
    client:
      register-with-eureka: true
      fetch-registry: true
      service-url:
        defaultZone: http://peer2:7002/eureka/ # 注册到节点2
  
  # EurekaServer节点2
  spring:
    application:
      name: eureka-server
  server:
    port: 7002
  eureka:
    instance:
      hostname: peer2
    client:
      register-with-eureka: true
      fetch-registry: true
      service-url:
        defaultZone: http://peer1:7001/eureka/ # 注册到节点1
  
  ```

#### 2.2Eureka 集群使用(三节点部署)

- **配置文件**

  ```yml
  # EurekaServer节点1
  spring:
    application:
      name: eureka-server
  server:
    port: 7001
  eureka:
    instance:
      hostname: peer1
    client:
      service-url:
        defaultZone: http://peer2:7002/eureka/,http://peer3:7003/eureka/
  
  # EurekaServer节点2
  spring:
    application:
      name: eureka-server
  server:
    port: 7002
  eureka:
    instance:
      hostname: peer2
    client:
      service-url:
        defaultZone: http://peer2:7002/eureka/,http://peer3:7003/eureka/
  
  # EurekaServer节点3
  spring:
    application:
      name: eureka-server
  server:
    port: 7003
  eureka:
    instance:
      hostname: peer3
    client:
      service-url:
        defaultZone: http://peer1:7001/eureka/,http://peer2:7002/eureka/
  
  ```

  

### 3.服务注册与发现

#### 3.1工作流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/11_服务注册发现工作流程.png)

- 启动注册中心
- 服务提供者生产服务并注册到服务中心中
- 消费者从服务中心中获取服务并执行

#### 3.2服务提供者

- 引入依赖

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <!-- Eureka客户端 -->
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```

  

- 配置文件

  ```yml
  spring:
    application:
      # 指定微服务的名称,在调用的时候只需要使用该名称就可以进行服务的访问.
      name: eureka-producer
  # eureka.client.enabled=false,可以禁用服务注册与发现
  eureka:
    client:
      service-url:
        # 对应服务注册中心的配置内容,指定服务注册中心的位置.
        defaultZone: http://localhost:7000/eureka/
  server:
    port: 8000
  ```

  

- 启动类

  ```java
  /**
  * 和通常SpringBoot一样
  **/
  @SpringBootApplication
  public class EurekaProducerApplication {
      public static void main(String[] args) {
          SpringApplication.run(EurekaProducerApplication.class, args);
      }
  
  }
  ```

  

- Controller类--访问接口

  ```java
  @RestController
  @RequestMapping("/hello")
  public class HelloController {
      @GetMapping("/")
      public String hello(@RequestParam String name) {
          return "Hello, " + name + " " + new Date();
      }
  
  }
  ```

  

#### 3.3服务消费者

创建服务消费者根据使用 API 的不同，大致分为三种方式。

##### 3.3.1使用 LoadBalancerClient

- **基本定义**
  1.  `LoadBalancerClient`是一个负载均衡客户端的抽象定义 

- **POM依赖配置**

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <!-- Eureka客户端/包括提供者和消费者 -->
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```

- **配置类**

  ```java
  @Configuration
  public class LbcConfig {
      
      // 初始化RestTemplate,用来发起REST请求.
      @Bean
      public RestTemplate restTemplate() {
          return new RestTemplate();
      }
  }
  ```

  

- **消费服务的接口Controller**

  ```java
  @RequestMapping("/hello")
  @RestController
  public class HelloController {
  
      @Autowired
      private LoadBalancerClient client;
  
      @Autowired
      private RestTemplate restTemplate;
  
      @GetMapping("/")
      public String hello(@RequestParam String name) {
          name += "!";
          
          // 1.通过loadBalancerClient的choose方法来负载均衡的选出一个eureka-producer的服务实例
          //   服务实例的基本信息存储在ServiceInstance中
          ServiceInstance instance = client.choose("eureka-producer");
          
          // 2.通过这些对象中的信息拼接出访问服务调用者的 /hello/ 接口的详细地址
          String url = "http://" + instance.getHost() + ":" 
              + instance.getPort() + "/hello/?name=" + name;
          
          // 3.利用RestTemplate对象实现对服务提供者接口的调用
          return restTemplate.getForObject(url, String.class);
      }
  
  }
  ```

  

##### 3.3.2Spring Cloud Ribbon

- **Ribbon基本定义**
  1. `Ribbon`是一个基于`HTTP`和`TCP`的客户端负载均衡器，通过在客户端中配置`ribbonServerList`来设置服务端列表去轮询访问以达到均衡负载的作用。
  2. 当`Ribbon`与`Eureka`联合使用时，`ribbonServerList`会被`DiscoveryEnabledNIWSServerList`重写，扩展成从`Eureka`注册中心中获取服务实例列表。同时也会用`NIWSDiscoveryPing`来取代IPing，将职责委托给Eureka来确定服务端是否已经启动。

- **POM依赖配置**

  1. `Finchley.RC1`版本中，`spring-cloud-starter-netflix-eureka-client`里边已经包含了`spring-cloud-starter-netflix-ribbon`了。

  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```

- **配置类**

  ```java
  @Configuration
  public class RibbonConfig {
      
      // 初始化RestTemplate,用来发起REST请求.
      @LoadBalanced
      @Bean
      public RestTemplate restTemplate() {
          return new RestTemplate();
      }
  }
  ```

- **调用接口Controller**

  1. 直接用服务名`eureka-producer`取代了之前的具体的`host:port`，`SpringCloudRibbon`有一个拦截器，在进行实际调用的时候，自动的去选取服务实例，并将这里的服务名替换成实际要请求的`IP`地址和端口，从而完成服务接口的调用。

  ```java
  @GetMapping("/")
  public String hello(@RequestParam String name) {
      name += "!";
      String url = "http://eureka-producer/hello/?name=" + name;
      // 去掉LoadBalancerClient,并修改相应的方法,直接用RestTemplate发起请求.
      return restTemplate.getForObject(url, String.class);
  }
  ```

  

##### 3.3.3Spring Cloud Feign

- **Feign基本定义**
  1. `Feign`是基于`Ribbon`实现的，所以自带了客户端负载均衡功能，也可以通过`Ribbon`的`IRule`进行策略扩展。
  2. `Feign`还整合的`Hystrix`来实现服务的容错保护，默认是关闭的。

- **POM依赖配置**

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  
  <!-- Feign工具包 -->
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
  </dependency>
  
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```

- **配置类**

  1. 注解`@EnableFeignClients`，用来启动Feign

  ```java
  @Configuration
  @EnableFeignClients
  public class FeignConfig {
      
  }
  ```

- **Feign调用接口**

  1. 创建一个`Feign`的客户端接口定义，使用`@FeignClient`注解来指定这个接口所要调用的服务名称。
  2. 接口中定义的各个函数使用`SpringMVC`的注解就可以来绑定服务提供方的`REST`接口，此类中的方法和远程服务中Contoller中的方法名和参数需保持一致。
  3. 当参数没有被`@RequestParam`注解修饰时，会自动被当做`requestbody`来处理。只要有`body`，就会被`Feign`认为是`POST`请求，所以整个服务是被当作带有`requestparameter`和`body`的`POST`请求发送出去的。

  ```java
  @FeignClient(name = "eureka-producer")
  public interface HelloRemote {
  
      @GetMapping("/hello/")
      String hello(@RequestParam(value = "name") String name);
  
  }
  ```

- **Controller类**

  ```java
  @RequestMapping("/hello")
  @RestController
  public class HelloController {
  
      @Autowired
      HelloRemote helloRemote;
  
      @GetMapping("/{name}")
      public String index(@PathVariable("name") String name) {
          // 和调用其他类方法一一样,这里应用了Java动态代理.
          return helloRemote.hello(name + "!");
      }
  
  }
  ```

  

