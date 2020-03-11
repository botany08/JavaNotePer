##SpingBoot启动过程分析

### 1.启动代码

```java
@SpringBootApplication
public class ProductBootApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProductBootApplication.class, args);
	}
}
```

- 启动过程分为两部分
  1. `SpringApplication`的构造过程，进行初始化。
  2. 构造完成之后调用`run`方法，启动`SpringApplication`，执行`run`方法。



###2.构造SpringApplication,进行初始化

####2.1基本流程

1. 把参数`sources`设置到`SpringApplication`属性中，这个`sources`可以是任何类型的参数。(启动代码中,参数为`ProductBootApplication`)
2. 判断是否是`web`程序，并设置到`webEnvironment`这个`boolean`属性中。
3. 找出所有的初始化器，默认有5个，设置到`initializers`属性中。
4. 找出所有的应用程序监听器，默认有9个，设置到`listeners`属性中。
5. 找出运行的主类(`mainclass`)。

####2.2SpringApplication构造方法

```java
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
    // 资源加载类
    this.resourceLoader = resourceLoader;
    Assert.notNull(primarySources, "PrimarySources must not be null");
    
    /**
    * 1.把sources设置到SpringApplication的sources属性中,目前只是一个ProductBootApplication类对象
    */
    this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
    
    /**
    * 2.判断是否是web程序,并设置到webEnvironment属性中
    *   javax.servlet.Servlet和org.springframework.web.context.ConfigurableWebApplicationContext
    *   都必须在类加载器中存在.
    */
    this.webApplicationType = WebApplicationType.deduceFromClasspath();
    
    /**
    * 3.找出所有的应用初始化器
    *   从spring.factories文件中找出key为ApplicationContextInitializer的类并实例化.
    *   设置到SpringApplication的initializers属性中.
    */
    setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
    
    /**
    * 4.找出所有的应用程序监听器
    *   从spring.factories文件中找出key为ApplicationListener的类并实例化.
    *   设置到SpringApplication的listeners属性中.
    */
    setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
    
    /**
    * 5.找出mainclass类，这里是ProductBootApplication类
    */
    this.mainApplicationClass = deduceMainApplicationClass();
}
```



#### 2.3判断web程序的类型

```java
static WebApplicationType deduceFromClasspath() {
		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) // DispatcherHandler对应Webflux
            && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null) // DispatcherServlet对应MVC
            && !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) { // ServletContainer对应Tomcat
			return WebApplicationType.REACTIVE;  // web应用类型是 webflux
		}
		for (String className : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return WebApplicationType.NONE; // 不是web应用
			}
		}
		return WebApplicationType.SERVLET; // web类型是 servlet,就是tomcat+springMVC
	}
```



#### 2.4应用初始化接口ApplicationContextInitializer

- 默认情况下，在`spring.factories`有5个类

  1. **org.springframework.boot.context.config.DelegatingApplicationContextInitializer**

     用来委派执行`ConfigurableEnvironment`中定义的那些`ApplicationContextInitializer`。 

  2. **org.springframework.boot.context.ContextIdApplicationContextInitializer**

     用来设置`contextId` 

  3. **org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer**

     用来检查一些错误的`configuration`。

  4. **org.springframework.boot.context.web.ServerPortInfoApplicationContextInitializer**

     用来设置`server`的端口至`Environment properties`。

  5. **org.springframework.boot.autoconfigure.logging.AutoConfigurationReportLoggingInitializer**

     用来打印自动配置的日志内容。

- 接口作用

  ```java
  /**
  * 接口定义
  **/
  public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {
  	void initialize(C applicationContext);
  }
  ```

  1. 在`ConfigurableApplicationContext`类型(或者子类型)的`ApplicationContext`做`refresh`之前，允许我们对`ConfigurableApplicationContext`的实例做进一步的设置或者处理。

  2. 通常用于需要对应用程序上下文进行编程初始化的web应用程序中。例如，根据上下文环境注册属性源或激活概要文件。 

     类似于`ContextLoader`和`FrameworkServlet`中支持定义`contextInitializerClasses`作为`context-param`或定义`init-param`。

  3. `ApplicationContextInitializer`支持`Order`注解，表示执行顺序，越小越早执行；



#### 2.5应用监听器接口ApplicationListener

- 默认情况下，在`spring.factories`有9个类

  1. **org.springframework.boot.context.config.ConfigFileApplicationListener**

     除了是一个监听器之外，实现了接口`EnvironmentPostProcessor`，在监听到事件时，会从指定的文件加载配置并配置application context。 

  2. **org.springframework.boot.context.config.AnsiOutputApplicationListener**

     用来获取配置`spring.output.ansi.enabled=false`， 设置logback在输出时，加了一个 ColorConverter ，渲染字段的颜色

  3. **org.springframework.boot.logging.LoggingApplicationListener**

     自动初始化底层日志系统

  4. **org.springframework.boot.logging.ClasspathLoggingApplicationListener**

     程序启动时，将classpath打印到debug日志，启动失败时classpath打印到info日志 

  5. **org.springframework.boot.autoconfigure.BackgroundPreinitializer**

     另起一个后台线程触发那些耗时的初始化，包括验证器、消息转换器等等 

  6. **org.springframework.boot.context.config.DelegatingApplicationListener**

     把`Listener`转发给配置的这些`class`处理，这样可以支持外围代码不去写`spring.factories`中的`org.springframework.context.ApplicationListener`相关配置，保持`springboot`原来代码的稳定。

  7. **org.springframework.boot.builder.ParentContextCloserApplicationListener**

     容器关闭时发出通知，如果父容器关闭，那么自容器也一起关闭。

  8. **org.springframework.boot.context.FileEncodingApplicationListener**

     在`springboot`环境准备完成以后运行，获取环境中的系统环境参数，检测当前系统环境的`file.encoding`和`spring.mandatory-file-encoding`设置的值是否一样,如果不一样则抛出异常。 如果不配置spring.mandatory-file-encoding则不检查。

  9. **org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener**

      如果存在，则使用springboot相关的版本进行替代 

- 接口作用

  ```java
  /**
  * 接口定义
  **/
  @FunctionalInterface
  public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
  	void onApplicationEvent(E event);
  }
  ```

  1.  `ApplicationListener`是基于观察者模式监听`ApplicationEvent`并触发指定的操作 。

- Spring事件机制原理

  1. 事件接口`ApplicationEvent`

     用来定义事件主体，运行逻辑。

  2. 监听器接口`ApplicationListener`

     监听事件，有触发的方法

  3. 发布接口`ApplicationEventPublisher`

     通过注册方法，注册事件到内部。在注册方法中，循环调用监听器的触发方法。

  4. 客户端

     客户端使用时，通过实例化发布对象，调用对象的注册方法，将事件注册进去，就可以循环调用监听器的方法。



###3.调用SpringApplication.run()方法

#### 3.1基本流程

1. 构造一个`StopWatch`，观察`SpringApplication`的执行
2. 找出所有的`SpringApplicationRunListener`并封装到`SpringApplicationRunListeners`中，用于监听`run`方法的执行。监听的过程中会封装成事件并广播出去让初始化过程中产生的应用程序监听器进行监听。
3. 构造Spring容器(`ApplicationContext`)，并返回。
  - 创建`Spring`容器的判断是否是`web`环境，是的话构造`AnnotationConfigEmbeddedWebApplicationContext`，否则构造`AnnotationConfigApplicationContext`
  - 初始化过程中产生的初始化器在这个时候开始工作
  - `Spring`容器的刷新(完成`bean`的解析、各种`processor`接口的执行、条件注解的解析等等)
4. 从`Spring`容器中找出`ApplicationRunner`和`CommandLineRunner`接口的实现类并排序后依次执行



#### 3.2Spring中的三种监听器

- `ApplicationListener`、`SpringApplicationRunListeners`、`SpringApplicationRunListener`之间的关系
  1. `SpringApplicationRunListeners`类和`SpringApplicationRunListener`类是`SpringBoot`中新增的类。`ApplicationListener`是`spring`中框架的类。
  2.  在`SpringBoot`(`SpringApplication`类)中，使用`SpringApplicationRunListeners`、`SpringApplicationRunListener`来间接调用`ApplicationListener`。 
  3. 为了批量执行，`SpringApplicationRunListeners`封装了`SpringApplicationRunListener`，持有一个`List`。
  4. `SpringApplicationRunListeners`与`SpringApplicationRunListener`生命周期相同，调用每个周期的各个`SpringApplicationRunListener`。然后广播相应的事件到`Spring`框架的`ApplicationListener` 。
  5. `SpringApplicationRunListener`就是一个`ApplicationListener`的代理类。

#### 3.3SpringApplicationRunListener

```java
/**
* 作用：用来在整个启动流程中接收不同执行点事件通知的监听者.
*      SpringApplicationRunListener接口规定了SpringBoot的生命周期,在各个生命周期广播相应的事件,
*      调用实际的ApplicationListener类。
**/
public interface SpringApplicationRunListener {
    //刚执行run方法时
    void started();
    
    //环境建立好时候
    void environmentPrepared(ConfigurableEnvironment environment);
    
    //上下文建立好的时候
    void contextPrepared(ConfigurableApplicationContext context);
    
    //上下文载入配置时候
    void contextLoaded(ConfigurableApplicationContext context);
    
    //上下文刷新完成后，run方法执行完之前
    void finished(ConfigurableApplicationContext context, Throwable exception);
}
```

- 5个步骤

  1. **started**(调用run方法后立马执行，对应事件的类型是`ApplicationStartedEvent`)：通知监听器，SpringBoot开始执行。

  2. **environmentPrepared**(`ApplicationContext`创建之前并且环境信息准备好的时候调用，对应事件的类型是`ApplicationEnvironmentPreparedEvent`)：通知监听器，Environment准备完成。

  3. **contextPrepared**(`ApplicationContext`创建好并且在`source`加载之前调用一次，没有具体的对应事件)：通知监听器，`ApplicationContext`已经创建并初始化完成。

  4. **contextLoaded**(`ApplicationContext`创建并加载之后并在`refresh`之前调用，对应事件的类型是`ApplicationPreparedEvent`)：通知监听器，ApplicationContext已经完成IoC配置。

  5. **finished**(run方法结束之前调用，对应事件的类型是`ApplicationReadyEvent`或`ApplicationFailedEvent`)：通知监听器，SpringBoot启动完成。

     

- 唯一实现类**EventPublishingRunListener**

  1. 使用了Spring广播器`SimpleApplicationEventMulticaster`。

  2. 把监听的过程封装成了`SpringApplicationEvent`事件。

  3. 通过`SimpleApplicationEventMulticaster`将事件广播出去。

     ```java
     @Override
     public void multicastEvent(final ApplicationEvent event, 
                                @Nullable ResolvableType eventType) {
         ResolvableType type = (eventType != null ? 
                                eventType : resolveDefaultEventType(event));
         // 获取执行器,异步调用
         Executor executor = getTaskExecutor();
     
         // 获取SprngApplication中的监听器,将事件广播出去
         for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
             if (executor != null) {
                 executor.execute(() -> invokeListener(listener, event));
             }
             else {
                 invokeListener(listener, event);
             }
         }
     }
     ```

  4. 广播出去的事件对象会被`SpringApplication`中的`listeners`属性进行处理。

- 事件广播的流程

  `SpringApplicationRunListener`和`ApplicationListener`之间的关系是通过`ApplicationEventMulticaster`广播出去的`SpringApplicationEvent`所联系起来的。 

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_事件广播.png)

#### 3.4SpringApplication的run()方法

```java
// 调用SpringApplication.run(...)静态方法后，它会new一个SpringApplication去调用真正的run方法(该方法)
public ConfigurableApplicationContext run(String... args) {
    // 构造一个任务执行观察器
    StopWatch stopWatch = new StopWatch();
    // 开始执行，记录开始时间
    stopWatch.start();
    
    ConfigurableApplicationContext context = null;
    Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
    configureHeadlessProperty();
    
    /** 
    * 初始化监听器
    * 获取SpringApplicationRunListeners,内部只有一个EventPublishingRunListener
    **/
    SpringApplicationRunListeners listeners = getRunListeners(args);
    
    /**
    * 启动监听器
    * 封装成SpringApplicationEvent事件然后广播出去给SpringApplication中的listeners所监听,
    * 接受ApplicationStartedEvent事件的listener会执行相应的操作.
    **/
    listeners.starting();
    
    // 平时开发时遇到Spring Boot启动失败却没有打印出错误信息,可以选择在这边debug
    try {
        // 装配参数和环境，选择对的profile
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        
        // 装配完环境后会触发ApplicationEnvironmentPreparedEvent事件
        ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
        configureIgnoreBeanInfo(environment);
        
        // 打印Banner-就是LOGO
        Banner printedBanner = printBanner(environment);
        
        // 根据是否web环境创建Spring的应用上下文
        context = createApplicationContext();
        exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
            new Class[] { ConfigurableApplicationContext.class }, context);
        
        // 创建好后就装配应用上下文
        prepareContext(context, environment, listeners, applicationArguments, printedBanner);
        
        // 刷新上下文,会根据web环境创建一个Servlet容器,默认是tomcat
        refreshContext(context);
        // 容器创建完成之后执行额外一些操作,目前是空实现,钩子方法
        afterRefresh(context, applicationArguments);
        
        // 任务观察器停止
        stopWatch.stop();
        
        if (this.logStartupInfo) {
            new StartupInfoLogger(this.mainApplicationClass)
                .logStarted(getApplicationLog(), stopWatch);
        }
        
        // 刷新结束后会发布ApplicationReadyEvent事件
        listeners.started(context);
        
        // 然后会调用自定的Runner.(这些Runner用在SpringApplication启动后运行一些特殊代码)
        callRunners(context, applicationArguments);
    }
    catch (Throwable ex) {
        handleRunFailure(context, ex, exceptionReporters, listeners);
        throw new IllegalStateException(ex);
    }

    try {
        listeners.running(context);
    }
    catch (Throwable ex) {
        handleRunFailure(context, ex, exceptionReporters, null);
        throw new IllegalStateException(ex);
    }
    return context;
}
```



