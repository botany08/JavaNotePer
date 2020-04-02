##Spring@Async异步线程池

### 1.Spring的线程池实现

- **接口`TaskExecutor`**

  1. `TaskExecutor`是`Spring`异步线程池的接口类，继承于`java.util.concurrent.Executor`。

- **默认实现类**

  | 名称                           | 特点                                                         |
  | ------------------------------ | ------------------------------------------------------------ |
  | `SimpleAsyncTaskExecutor`      | `@Async`默认线程池，不重用线程，每次调用都会**创建一个新的线程**。本质上不是一个真正的线程池。有限流的功能来防止无限制创建线程。 |
  | `SyncTaskExecutor`             | 是一个同步操作，在原线程执行，没有实现异步调用。本质上不是一个真正的线程池。 |
  | `ConcurrentTaskExecutor`       | `Executor`的适配类，不推荐使用。如果`ThreadPoolTaskExecutor`不满足要求时，才用考虑使用这个类。 |
  | `ThreadPoolTaskExecutor`       | 常用的线程池，本质上是对JUC线程池的封装。持有一个`ThreadPoolExecutor`引用，在Spring容器创建时初始化，阻塞队列是`LinkedBlockingQueue`。 |
  | `SimpleThreadPoolTaskExecutor` | 监听`Spring’s lifecycle callbacks`，并且可以和`Quartz`的`Component`兼容。线程池同时被`quartz`和非`quartz`使用，才需要使用此类。 |
  | `TimerTaskExecutor`            | -                                                            |
  | `WorkManagerTaskExecutor`      | -                                                            |



### 2.@Async注解原理

- **@Async**

  1.  不要返回值直接`void`，需要返回值用`AsyncResult`或者`CompletableFuture` 。
  2.  可自定义执行器并指定例如：`@Async("otherExecutor")` 
  3. 必须在不同类间调用，如果在同一个类中调用，会变同步执行。
     底层实现是代理对注解扫描实现的，调用者其实是this当前对象，不是真正的代理对象。方法上没有注解，没有生成相应的代理类。

  ```java
  // 可用于类、方法
  @Target({ElementType.TYPE, ElementType.METHOD})
  // 运行时生效
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface Async {
  	
      // 用以限定执行方法的执行器名称(自定义)：Executor或者TaskExecutor.
      // 加在类上表示整个类都使用，加在方法上会覆盖类上的设置
  	String value() default "";
  
  }
  ```

  

### 3.@EnableAsync注解原理

 https://www.cnblogs.com/dennyzhangdd/p/9026303.html 

 https://www.wetsion.site/spring-boot-annotation-async.html 