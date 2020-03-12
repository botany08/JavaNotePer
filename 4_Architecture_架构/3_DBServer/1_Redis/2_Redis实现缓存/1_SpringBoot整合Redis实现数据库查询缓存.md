## Redis实现数据库查询缓存

### 1.SpringBoot的缓存机制                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          

#### 1.1JSR107缓存规范

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/2_SpringBoot缓存接口体系.png" style="zoom:80%;" />

- JSR107是关于缓存使用的规范，主要是5个核心接口。
  1. `CachingProvider`定义了创建、配置、获取、管理和控制多个`CacheManager`。一个应用可以在运行期访问多个`CachingProvider`。
  2. `CacheManager`定义了创建、配置、获取、管理和控制多个唯一命名的`Cache`，这些`Cache`存在于`CacheManager`的上下文中。一个`CacheManager`仅被一个`CachingProvider`所拥有。
  3. `Cache`是一个类似Map的数据结构并临时存储以Key为索引的值。一个`Cache`仅被一个`CacheManager`所拥有。
  4. `Entry`是一个存储在`Cache`中的`key-value`对。
  5. `Expiry`每一个存储在`Cache`中的条目有一个定义的有效期。一旦超过这个时间，条目为过期的状态。一旦过期，条目将不可访问、更新和删除。缓存有效期可以通过`ExpiryPolicy`设置。



#### 1.2SpringBoot中的缓存抽象

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/3_缓存使用过程.png" style="zoom:80%;" />

- **缓存接口**
  1. `org.springframework.cache.Cache`和`org.springframework.cache.CacheManager`，为`Spring3.1`中定义的，统一不同缓存技术的接口。
  2. `Cache`接口为缓存的组件规范定义，包含缓存的各种操作集合。
  3. `Cache`接口下`Spring`提供了各种`xxxCache`的实现，如`RedisCache`、`EhCacheCache`、`ConcurrentMapCache`等。
  4. 每次调用需要缓存功能的方法时，`Spring`会检查检查指定参数的指定的目标方法是否已经被调用过。如果有就直接从缓存中获取方法调用后的结果，如果没有就调用方法并缓存结果返回给用户，下次直接从缓存中获取。
- **缓存使用**
  1. 确定方法需要被缓存以及他们的缓存策略。
  2. 从缓存中读取之前缓存存储的数据。

#### 1.3缓存组件

- **缓存相关类**

  1. `Cache`缓存接口，定义缓存操作。实现类有`RedisCache`、`EhCacheCache`、`ConcurrentMapCache`等。
  2. `CacheManager`缓存管理器，管理各种缓存(Cache)组件。
  3. `keyGenerator`，缓存数据时，key的生成策略(cache块中key名称)。
  4. `serialize`，缓存数据时，value序列化策略(cache块名称)。

- **缓存注解**

  1. `@EnableCaching` ，开启基于注解的缓存。

  2. `@CacheConfig` ，用于类名上，指定该类的缓存块(`Cache`块名称)。

  3. `@Cacheable` ，配置在方法，根据方法请求参数缓存结果，并且第二次返回缓存。

     | 参数      | 解释                                                         | 例子                                                         |
     | --------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
     | value     | 缓存的名称，在 spring 配置文件中定义，必须指定至少一个       | @Cacheable(value=”mycache”)                                  |
     | key       | 缓存的 key，可以为空，如果指定要按照SpEL表达式编写，如果不指定，则缺省按照方法的所有参数进行组合 | @Cacheable(value=”testcache”,key=”#userName”)                |
     | condition | 缓存的条件，可以为空，使用 SpEL 编写，返回 true 或者 false，只有为 true 才进行缓存 | @Cacheable(value=”testcache”,condition=”#userName.length()>2”) |

     

  4. `@CachePut` ，配置在方法， 和`@Cacheable`不同的是，每次都会触发真实方法的调用，可以更新缓存。主要用于更新缓存。

  5. `@CacheEvict` ，主要针对方法配置，能够根据一定的条件对缓存进行清空。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_缓存注解参数.png)

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/5_SpeL表达式.png)

#### 1.4缓存实例

- **引入缓存依赖**

  ```xml
  <!--自带缓存实现-->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-cache</artifactId>
  </dependency>
  ```

  

- **配置类(启动类)开启缓存注解**

  ```java
  @Configuration
  @EnableCaching // 开启缓存注解
  public class CacheConfig {
  }
  
  ```

  

- **使用缓存**

  1. 查询方法统一使用缓存，提高查询速度。可设置缓存过期时间，获取最新数据。
  2. 插入/.更新/删除方法，需要更新缓存，key的设置策略还不清楚。TODO
  3. `@CacheEvict`注解的使用，需要设置什么条件情况缓存，还不清楚。TODO

  ```java
  /**
   * 业务逻辑类
   *
   * @author zangbao.lin
   * @version 2020/3/12
   * @since JDK 1.8
   */
  @Service
  @CacheConfig(cacheNames = "democache")
  public class CacheDemoService {
  
      @Autowired
      private CacheQueryDao cacheQueryDao;
  
      /**
       * 获取表CAR全部数据并缓存,key为root.target
       * @return
       */
      @Cacheable(key = "#root.target")
      public List<CarVO> queryAll() {
          List<CarVO> carVOList = cacheQueryDao.queryAll();
  
          return carVOList;
      }
  
      /**
       * 插入并且更新缓存,key为root.target
       * @return
       */
      @CachePut(key ="#root.target")
      public List<CarVO> insertOne() {
          cacheQueryDao.insertOne(5,"长安","蓝色");
          // 返回最新数据,放入缓存中
          return cacheQueryDao.queryAll();
  
      }
  
  }
  
  
  /**
   * 具体SQL执行类
   *
   * @author zangbao.lin
   * @version 2020/3/12
   * @since JDK 1.8
   */
  @Mapper
  public interface CacheQueryDao {
  
      @Select("select * from car")
      List<CarVO> queryAll();
  
      @Select("select * from car where id #{id}")
      CarVO queryOneById(int id);
  
      @Select("select * from car where name = #{name}")
      List<CarVO> queryInfoByName(@Param("name") String name);
  
      @Insert("insert into car value (#{id},#{name},#{color});")
      int insertOne(@Param("id")int id,@Param("name")String name,@Param("color")String color);
  }
  
  ```



### 2.Redis作为缓存

####2.1Jedis基本操作

- **引入依赖**

  ```xml
  <!--SpringBoot2.x+Redis的依赖-->
  <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-pool2</artifactId>
      <version>2.4.2</version>
  </dependency>
  
  <!--SpringBoot2.x+Redis的依赖-->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
      <version>${parent.version}</version>
  </dependency>
  
  <!--自带缓存实现-->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-cache</artifactId>
  </dependency>
  ```

- **配置文件**

  ```yml
  spring:
    application:
      name: product-boot
    datasource:
      driver-class-name: com.mysql.jdbc.Driver
      type: com.alibaba.druid.pool.DruidDataSource
      url: jdbc:mysql://127.0.0.1:3306/nretail_tlink_server
      username: root
      password: 123456
    redis:
      # redis数据库索引,表示第几个数据库
      database: 0
      # redis服务器地址
      host: 127.0.0.1
      # 连接端口
      port: 6379
      # 连接密码(默认为空)
      password:
      # 连接超时时间(毫秒)
      timeout: 6000
  
      # Jedis为SpringBoot1.x的客户端
      jedis:
        pool:
          # 连接池最大连接数(使用负值表示没有限制)
          max-active: 8
          # 连接池最大阻塞等待时间(使用负值表示没有限制)
          max-wait: -1s
          # 最大空闲连接
          max-idle: 8
          # 最小空闲连接
          min-idle: 0
  
      # lettuce为SpringBoot2.x的客户端
      lettuce:
        pool:
          # 连接池最大连接数(使用负值表示没有限制)
          max-active: 8
          # 连接池最大阻塞等待时间(使用负值表示没有限制)
          max-wait: -1s
          # 最大空闲连接
          max-idle: 8
          # 最小空闲连接
          min-idle: 0
          shutdown-timeout: 100ms
  ```

  

- **数据库操作**

  ```java
  
  /**
   * 封装类StringRedisTemplate进行操作
   */
  @Service
  public class JedisService {
  
      @Autowired
      private StringRedisTemplate stringRedisTemplate;
  
      /**
       * 操作字符串
       */
      public void operateString() {
          stringRedisTemplate.opsForValue().set("black","xiaomi");
          String value = stringRedisTemplate.opsForValue().get("black");
          System.out.println(value);
      }
  
      /**
       * List简单操作,Redis列表是简单的字符串列表,按照插入顺序排序.
       * 可以添加一个元素到列表的头部(左边),或者尾部(右边).
       */
      public void operateList() {
  
          String key = "yellow";
          ListOperations<String,String> listOperations = stringRedisTemplate.opsForList();
          // 从左压入栈
          listOperations.leftPush(key,"huawei");
          listOperations.leftPush(key,"oppo");
          // 从右压入栈
          listOperations.rightPush(key,"vivo");
  
          List<String> list = listOperations.range(key,0,2);
  
          // 打印结果
          for(String s : list) {
              System.out.println(s);
          }
      }
  }
  
  ```



#### 2.2配置Redis为缓存

- **配置类**

  ```java
  package com.tcl.joker.productboot.config;
  
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.cache.annotation.EnableCaching;
  import org.springframework.cache.interceptor.KeyGenerator;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.data.redis.cache.RedisCacheConfiguration;
  import org.springframework.data.redis.cache.RedisCacheManager;
  import org.springframework.data.redis.connection.RedisConnectionFactory;
  import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
  import org.springframework.data.redis.serializer.RedisSerializationContext;
  import org.springframework.data.redis.serializer.RedisSerializer;
  import org.springframework.data.redis.serializer.StringRedisSerializer;
  
  import java.time.Duration;
  
  /**
   * 类 <code>{类名称}</code>{此类功能描述}
   *
   * @author zangbao.lin
   * @version 2020/3/12
   * @since JDK 1.8
   */
  @Configuration
  @EnableCaching
  public class CacheConfig {
  
      private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
  
      /**
       * 自定义key生成器,当没有配置key时,会通过这个生成
       * @return
       */
      @Bean
      public KeyGenerator keyGenerator(){
          return (o, method, params) ->{
              StringBuilder sb = new StringBuilder();
              // 类目
              sb.append(o.getClass().getName());
              // 方法名
              sb.append(method.getName());
              for(Object param: params){
                  // 参数名
                  sb.append(param.toString());
              }
              return sb.toString();
          };
      }
  
      /**
       * 将默认的缓存管理器,修改成自定义Redis缓存管理器
       * @param connectionFactory
       * @return
       */
      @Bean
      public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
          RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                  // 缓存过期时间,60s缓存失效
                  .entryTtl(Duration.ofSeconds(60))
                  // 设置key的序列化方式
                  .serializeKeysWith(RedisSerializationContext
                                     .SerializationPair
                                     .fromSerializer(keySerializer()))
                  // 设置value的序列化方式
                  .serializeValuesWith(RedisSerializationContext
                                       .SerializationPair
                                       .fromSerializer(valueSerializer()))
                  // 不缓存null值
                  .disableCachingNullValues();
  
          RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                  .cacheDefaults(config)
                  .transactionAware()
                  .build();
  
          logger.info("自定义RedisCacheManager加载完成");
          return redisCacheManager;
      }
  
    /*  @Bean
      public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory){
          RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
          redisTemplate.setConnectionFactory(connectionFactory);
          redisTemplate.setKeySerializer(keySerializer());
          redisTemplate.setHashKeySerializer(keySerializer());
          redisTemplate.setValueSerializer(valueSerializer());
          redisTemplate.setHashValueSerializer(valueSerializer());
          logger.info("序列化完成！");
          return redisTemplate;
      }*/
  
      // key键序列化方式
      private RedisSerializer<String> keySerializer() {
          return new StringRedisSerializer();
      }
  
      // value值序列化方式
      private GenericJackson2JsonRedisSerializer valueSerializer(){
          return new GenericJackson2JsonRedisSerializer();
      }
  }
  
  ```

  

- **缓存使用**

  ```java
  package com.tcl.joker.productboot.service;
  
  import com.tcl.joker.productboot.dao.CacheQueryDao;
  import com.tcl.joker.productboot.model.CarVO;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.cache.annotation.CacheConfig;
  import org.springframework.cache.annotation.CachePut;
  import org.springframework.cache.annotation.Cacheable;
  import org.springframework.stereotype.Service;
  
  import java.util.List;
  
  /**
   * 类 <code>{类名称}</code>{此类功能描述}
   *
   * @author zangbao.lin
   * @version 2020/3/12
   * @since JDK 1.8
   */
  @Service
  @CacheConfig(cacheNames = "democache")
  public class CacheDemoService {
  
      @Autowired
      private CacheQueryDao cacheQueryDao;
  
      /**
       * 获取表CAR全部数据并缓存,key为root.target
       * @return
       */
      @Cacheable
      public List<CarVO> queryAll() {
          List<CarVO> carVOList = cacheQueryDao.queryAll();
  
          return carVOList;
      }
  
      /**
       * 插入并且更新缓存,key为root.target
       * @return
       */
      @CachePut
      public List<CarVO> insertOne() {
          cacheQueryDao.insertOne(6,"观致","黑色");
          // 返回最新数据,放入缓存中
          return cacheQueryDao.queryAll();
  
      }
  }
  ```

  