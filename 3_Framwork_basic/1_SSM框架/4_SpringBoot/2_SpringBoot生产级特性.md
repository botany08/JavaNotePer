## SpringBoot生产级特性

## 1.端点

#### 1.1对应插件

- `Actuator`插件提供了一系列`HTTP`请求，可以发送相应的请求，来获取`SpringBoot`应用程序的相关信息。
- `HTTP`请求都是`GET`类型的，而且都不带任何请求参数，被称为`端点`。

```xml
<!-- 配置依赖项,启用端点 -->
<dependency>
    <!-- 如果引入SpringBoot的Parent,启用插件就不用配置version -->
    <groupld>org.springframework.boot</grouped>
    <artifactld>spring-boot-starter-actuator</artifactld>
</dependency>

<!-- 访问端点方式 -->
http://127.0.0.1:10222/actuator  查看端点的访问地址

```



#### 1.2端点及其描述

| 端点        | 描述                     |
| ----------- | ------------------------ |
| conditions  | 获取自动配置信息         |
| beans       | 获取`SpringBean`基本信息 |
| configprops | 获取配置项信息           |
| dump        | 获取当前线程基本信息     |
| env         | 获取环境变量信息         |
| health      | 获取健康检查信息         |
| info        | 获取应用基本信息         |
| metrics     | 获取性能指标信息         |
| mappings    | 获取请求映射信息         |
| trace       | 获取请求调用信息         |



#### 1.3端点的配置

- 在`springboot`的旧版本中，监控端点(`如/env`)是默认开启的，所以只要项目正常启动，就能通过`url`获取信息。可是在2.0版本以后，由于安全性考虑，除了`/health`和`/info`的端点，默认都是不暴露的。 
  1.  要通过actuator暴露端点，必须同时是启用(enabled)和暴露(exposed)的。
  2.  所有除了/health和/info的端点，默认都是不暴露的。
  3.  所有除了/shutdown的端点，默认都是启用的 。

```properties
# 1.设置暴露所有端点
management.endpoints.web.exposure.include=*
 
# 2.设置单个端点（/shutdown）可用
management.endpoint.shutdown.enabled=true
 
# 3.设置暴露所有端点，除了env
management.endpoints.web.exposure.include=*
management.endpoints.web.exposure.exclude=env

```

- 访问方式
  1.  http://127.0.0.1:10222/actuator ：查看所有端点的访问地址。
  2.  http://127.0.0.1:10222/actuator/health ：查看health端点的健康检查信息。



#### 1.4端点的汇总和管理

- `HATEOAS`插件可以汇总端点信息，包括各个端点的名称与链接。通过配置插件`HATEOAS`，可以启动端点`actuato`，在发送`/actuator`请求后，将看到所有的端点及其访问链接。-- TODO暂时没用

  ```xml
  <!-- 配置依赖，开启HATEOAS插件 -->
  <dependency>
      <!-- 如果引入SpringBoot的Parent,启用插件就不用配置version -->
      <groupId>org•springframework.boot</groupId>
      <artifactId>spring-boot-starter-hateoas</artifactId>
  </dependency>
  ```

  ```properties
  # 禁用actuator端点
  endpoints.actuator.enab1ed=false
  # 设置端点的基础路径
  management.endpoints.web.base-path=/endpoints/actuator
  ```

- 图形化工具管理端点HALBrowser -- TODO暂时没用

  发送`/actuator`请求时，此时显示的将不再是一段`JSON`数据，而是一个非常漂亮的图形化界面。

  ```xml
  <!-- 配置依赖，开启图形化界面插件HALBrowser -->
  <dependency>
      <!-- 如果引入SpringBoot的Parent,启用插件就不用配置version -->
      <groupId>org.webjars</groupId>
      <artifactId>hal-browser</artifactId>
  </dependency>
  ```

- `Actuator`文档插件 --TODO暂时没用

  可以发送`/docs`请求，在浏览器中查看`Actuator`端点文档。

  ```xml
  <!-- 配置依赖，开启端点文档 -->
  <dependency>
      <!-- 如果引入SpringBoot的Parent,启用插件就不用配置version -->
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-actuator-docs</artifactId>
  </dependency>
  ```



### 2.健康检查 - health端点

#### 2.1基础磁盘信息检查

```properties
# 1.默认访问地址
http://127.0.0.1:10222/actuator/health

# 2.默认情况下,公开并且不显示细节,开启细节显示如下配置
management.endpoint.health.show-details=always

# 3.每次发送/health请求时，每次获取的健康情况实际上是从缓存中读取的，缓存时间默认为1000ms,可修改配置
management.endpoint.health.cache.time-to-live=500
```

```json
{
    // status为UP表示当前应用处于运行状态
    "status": "UP",
    "components": {
        // diskSpace表示磁盘空间的使用情况
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 107373129728,
                "free": 11460915200,
                "threshold": 10485760
            }
        },
        "ping": {
            "status": "UP"
        }
    }
}
```



#### 2.2其他健康检查指标

- 实际上，`SpringBoot`包含了许多内置的健康检查功能，每项功能对应具体的健康检查指标类(`Healthindicator`)。
- 添加相关的`SpringBoot`插件后，即可开启对应的健康检查功能，默认情况下只有`ApplicationHealthlndicator`与`DiskSpaceHealthlndicator`是启用的。
- 自定义检查类，实现`org.springfiramework.boot.actuate.health.Hcalthlndicator`接口，并覆盖health()方法即可。

| 名称                         | 描述                             |
| ---------------------------- | -------------------------------- |
| ApplicationHealthlndicator   | 检查应用运行状态(对应status部分) |
| DiskSpaceHealthlndicator     | 检查磁盘空间(对应diskSpace部分)  |
| DataSourceHealthlndicator    | 检查数据库连接                   |
| MailHealthlndicator          | 检查邮件服务器                   |
| JmsHealthlndicator           | 检查JMS代理                      |
| RedisHealthlndicator         | 检查Redis服务器                  |
| MongoHealthlndicator         | 检查MongoDB数据库                |
| CassandraHealthlndicator     | 检查Cassandra数据库              |
| RabbitHealthlndicator        | 检查RabbitMQ服务器               |
| SolrHealthlndicator          | 检查Solr服务器                   |
| ElasticsearchHealthlndicator | 检查ElasticSearch集群            |



###3.应用基本信息 - info端点

- 可以通过info端点，来获取SpringBoot应用程序的基本信息，比如应用程序的名称、描述、版本等。

```properties
# 1.访问地址
http://127.0.0.1:10222/actuator/info

# 2.应用基本信息的相关配置都是以info为前缀的配置项,都是自定义的配置项，只要以info开头就行
info.app.name=HelloWorld!
info.app.version=1.0.0
info.app.path=/info

# 返回结果
{
    "app": {
        "name": "HelloWorld!",
        "version": "1.0.0",
        "path": "/info"
    }
}

# 3.从pom.xml文件中获取这些属性，并写入application.properties文件
info.app.name=@project.name@
info.app.version=@project.version@
info.app.description=@project.description@

# pom.xml配置
<project>
....
	<version>1.0.0-SNAPSHOT</version>
	<name>product-boot</name>
	<description>Demo project for Spring Boot</description>
....
</project>
```



### 4.跨域

#### 4.1跨域问题

- 同源策略

  同domain(或ip)，同端口，同协议视为同一个域。一个域内的脚本仅仅具有本域内的权限，可以理解为本域脚本只能读写本域内的资源，而无法访问其它域的资源。这种安全限制称为同源策略。 内网中的服务器。

- 跨域

  不同源的两个服务器之间Http访问，就会产生跨域问题，不能访问。

#### 4.2跨域问题的解决

- 轻量级的`CORS(Cross-Origin Resource Sharing)`来实现跨域问题，在Spring 4.2以后才开始支持COR

  ```properties
  # 1.在SpringBoot中，只需要配置关于CORS的端点，就能开启该特性，默认情况下它是禁用的。
  management.endpoints.web.cors.allowed-origins=http://www.baidu.com
  management.endpoints.web.cors.allowed-methods=GET,POST,PUT,DELETE
  ```

  ```java
  /**
  * 2.通过配置类,配置跨域允许的域名
  **/
  @Configuration
  GEnableWebMvc
  publie class WebConfig extends WebMvcConfigurerAdapter {
      @Override
      public void addCorsMappings(CorsRegistry registry) { 
          registry.addMapping("/**")
              .allowedOrigins("http://www.xxx.com") 
              .allowedMethods ("GET","POST","PUT","DELETE");
      }
  }
  
  /**
  * 使用注解@CrossOrigin解决跨域问题
  **/ 
  @RestController
  @CrossOrigin
  public class AppinfoController {
      
      @RequestMapping(path = "/appinfo",method = RequestMethod.GET)
      public String appinfo() {
          return "AppInfo!";
      }
  }
  ```

- 在过滤器配置返回头

  ```java
  /**
  * 在返回头配置属性
  **/
  @Override
  public void doFilter(ServletRequest request, 
                       ServletResponse response, 
                       FilterChain chain) throws IOException, ServletException {
      HttpServletRequest req = (HttpServletRequest) request;
  
      HttpServletResponse res = (HttpServletResponse) response;
      res.setHeader("Access-Control-Allow-Origin", "http://www.xxx.com");
      res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
      res.setHeader("Access-Control-Max-Age", "3600");
      res.setHeader("Access-Control-Allow-Headers", "x-requested-with");
      chain.doFilter(request, response);
  }
  ```

  

### 5.外部配置

- 应用配置的三种方式
  1. `application.properties`配置文件中指定`SpringBoot`的相关配置项。
  2. 可使用`@...@`占位符获取`Maven`资源过滤的相关属性，依旧配置在`application.properties`中
  3. 通过外部配置覆盖`SpringBoot`配置项的默认值。
- 配置的读取顺序
  1. Java命令行参数
  2. JNDI属性
  3. Java系统属性
  4. 操作系统环境变量
  5. jar包外的application.properties配置文件
  6. jar包内的application.properties配置文件
  7. @PropertySource注解。
  8. SpringApplication.setDefaultProperties默认值

### 6.远程监控 

- 开启Shell连接SpirngBoot程序

```xml
<!-- 可通过ssh远程连接正在运行中的SpringBoot应用程序 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-remote-shell</artifactId>
</dependency>
```

- 开启telnet连接SpringBoot程序

```xml
<!-- telnet远程连接 -->
<dependency>
    <groupld>org.crsh</groupld>
    <artifactId>crsh.shell.telnet</artifactId>
    <version>1.2.11</version>
</dependency>

```

