## IOC容器之ApplicationContext

###1.ApplicationContext的结构

- ApplicationContext继承体系

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/6_ApplicationContext继承图.png" style="zoom: 50%;" />

- ApplicationContext实现接口图

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_ApplicationContext实现接口.png)

####1.1ApplicationContext类型容器的常用实现

  1. FileSystemXmlApplicationContext

     在默认情况下，从文件系统加载bean定义以及相关资源的ApplicationContext实现。

  2. ClassPathXmlApplicationContext

     在默认情况下，从Classpath加载bean定义以及相关资源的ApplicationContext实现。

  3. XmlWebApplicationContext

     Spring提供的用于Web应用程序的ApplicationContext实现。

  4. AnnotationConfigApplicationContext

     在默认情况下，扫描注解@Configuration加载配置类，并从配置类中解析bean定义及相关资源。

### 2.统一资源加载策略

Spring提出了一套基于org.springframework.core.io.Resource和org.springframework.core.io.ResourceLoader接口的资源抽象和加载策略。

#### 2.1资源抽象-Resource接口

- Resource接口

  Resource接口可以根据资源的不同类型，或者资源所处的不同场合，给出相应的具体实现。

  1. ByteArrayResource

     将字节（byte）数组提供的数据作为一种资源进行封装，如果通过InputStream形式访问该类型的资源，该实现会根据字节数组的数据，构造相应的ByteArrayInputStream并返回。

  2. ClassPathResource

     从Java应用程序的ClassPath中加载具体资源并进行封装，可以使用指定的类加载器(ClassLoader)或者给定的类进行资源加载。 

  3. FileSystemResource

     对java.io.File类型的封装，可以以文件或者URL的形式对该类型资源进行访问，只要能跟File打的交道，基本上跟FileSystemResource也可以。

  4. UrlResource

     通过java.net.URL进行的具体资源查找定位的实现类，内部委派URL进行具体的资源操作。

  5. InputStreamResource

     将给定的InputStream视为一种资源的Resource实现类，较为少用。

- 自定义Resource接口

  可以通过实现Resouce接口，或者继承org.springframework.core.io.AbstractResource抽象类，但是一般很少用到自定义的Resource。

####2.2资源加载-ResourceLoader接口

ResourceLoader接口是资源查找定位策略的统一抽象，具体的策略则由相应的ResourceLoader实现类给出。

- ResourceLoader接口定义

  ```java
  public interface ResourceLoader {
  	// public static final String CLASSPATH_URL_PREFIX = "classpath:";
  	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;
  	
      // 根据指定的资源位置，定位到具体的资源实例。
  	Resource getResource(String location);
  
  	ClassLoader getClassLoader();
  }
  ```

- ResourceLoader具体实现类

  1. DefaultResourceLoader-默认实现类

  ```java
/**
  * 实例：DefaultResourceLoader加载资源
  ```
* 资源查找逻辑
  
  * 1.首先检查资源路径是否以classpath:前缀打头,如果是则构造ClassPathResource类型资源并返回.
* 2.再者通过URL，根据资源路径来定位资源.如果有则会构造UrlResource类型的资源并返回.
  * 3.再者委派getResourceByPath(String)方法来定位,默认实现逻辑是，构造ClassPathResource类型的资源并返回.
  * 4.如果最终没找到符合条件的相应资源，getResourceByPath(String)方法就会构造一个实际上并不存在的资源并返回.
  **/
  public class ResourceDemo {
      public static void main(String[] args){
          // 初始化资源加载器ResourceLoader
          ResourceLoader resourceLoader = new DefaultResourceLoader();
          
          // 返回ClassPathResource资源
          Resource resourceA = resourceLoader.getResource("classpath:ApplicationContext.xml");
          asserprint(resourceA);
          
          // 返回ClassPathResource资源
          Resource resourceB =resourceLoader.getResource("D:\\Developer\\ApplicationContext.xml");
          asserprint(resourceB);
          
          // 返回UrlResource资源
          Resource resourceC = resourceLoader.getResource("file:D:\\Developer\\Applicati.xml");
          asserprint(resourceC);
      }
  
      // 判断加载的资源类型
      public static void asserprint(Resource fileResource) {
          if(fileResource instanceof ClassPathResource) {
              System.out.println("ClassPathResource");
          }
          if(fileResource instanceof FileSystemResource) {
              System.out.println("FileSystemResource");
          }
          if(fileResource instanceof UrlResource) {
              System.out.println("UrlResource");
          }
      }
  }
  ```
  
  3. FileSystemResourceLoader-继承DefaultResourceLoader
  
     FileSystemResourceLoader继承自DefaultResourceLoader，但覆写了getResourceByPath(String)方法，为了避免父类在getResourceByPath(String)方法上的不恰当处理。如果从文件系统加载资源，则以FileSystemResource类型返回。
  
     FileSystemResourceLoader的逻辑和DefaultResourceLoader基本一样，除了最后getResourceByPath(String)方法可以返回FileSystemResource类型的资源。
  ```

- ResourcePatternResolver-批量查找的ResourceLoader

  1. 接口定义

     ResourceLoader每次只能根据资源路径返回确定的单个Resource实例。

     ResourcePatternResolver则可以根据指定的资源路径匹配模式，每次返回多个Resource实例。

  ```java
  public interface ResourcePatternResolver extends ResourceLoader {
  	String CLASSPATH_ALL_URL_PREFIX = "classpath*:";
      // 根据路径匹配模式返回多个Resources的功能
  	Resource[] getResources(String locationPattern) throws IOException;
  }
  ```

  2. 接口实现类-PathMatchingResourcePatternResolver

     支持ResourceLoader级别的资源加载，支持ResourcePatternResolver新增加的classpath*:前缀等。

     在构造实例时，可以指定一个ResourceLoader。如果不指定，则会默认构造一个DefaultResourceLoader实例。

     PathMatchingResourcePatternResolver内部会将匹配后确定的资源路径，委派给它的ResourceLoader来查找和定位资源。

#### 2.3ApplicationContext与ResourceLoader

- Resource和ResourceLoader类层次图

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_ResourceLoader层次结构图.png)

- 接口ApplicationContext

  ```java
  // ApplicationContext定义
  public interface ApplicationContext 
      extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
  		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {...}
  ```

  1. ApplicationContext接口继承了ResourcePatternResolver，间接实现了ResourceLoader接口。任何的ApplicationContext实现都可以看作是一个ResourceLoader甚至ResourcePatternResolver。

  2. 所以，ApplicationContext支持Spring内统一资源加载策略。

     

- 抽象类AbstractApplicationContext

  ```java
  // AbstractApplicationContext定义
  public abstract class AbstractApplicationContext extends DefaultResourceLoader
  		implements ConfigurableApplicationContext, DisposableBean
  ```

  1. 抽象类继承了DefaultResourceLoader，ResourcePatternResolver接口getResource(String)可以直接用DefaultResourceLoader实例。
  2. AbstractApplicationContext一方面实现了ResourcePatternResolver接口，一方面本身也持有ResourcePatternResolver，对应的实例类型为PathMatchingResourcePatternResolver 。且将自身引用传入到PathMatchingResourcePatternResolver 中。
  3. ApplicationContext的实现类在作为ResourceLoader或者ResourcePatternResolver时候的行为，完全就是委派给了PathMatchingResourcePatternResolver和DefaultResourceLoader来做。

- ApplicationContext与ResourceLoader层次结构图

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_Application与ResourceLoader层次结构图.png)

  

- 作为ResourceLoader使用-ApplicationContext实现类

  1. 作用：加载文件资源，抽象出资源对象Resource。比较少用到。
  2. 实例

  ```java
  // 用ClassPathXmlApplicationContext初始化一个ResourceLoader实例
  ResourceLoader resourceLoader = new ClassPathXmlApplicationContext("配置文件路径");
  
  // 加载资源
  Resource fileResource = resourceLoader.getResource("D:/spring21site/README");
  Resource urlResource2 = resourceLoader.getResource("http://www.spring21.cn");
  ```

- ResourceLoader类型的注入

  1. 应用场景

     在容器中，某个bean需要依赖于ResourceLoader来查找定位资源，可以为其注入容器中声明的某个具体的ResourceLoader实现。该bean也无需实现任何接口，直接通过构造方法注入或者setter方法注入规则声明依赖。

  2. 实现Aware接口

     ApplicationContext容器本身就是一个ResourceLoader，可以直接将当前的ApplicationContext容器作为ResourceLoader注入。

     ```java
     /**
     * 实现接口ResourceLoaderAware
     **/
     public class Foo implements ResourceLoaderAware {
     	
         private ResourceLoader resourceLoader;
         
         // 依赖注入ResourceLoader
         public void setResourceLoader(ResourceLoader resourceLoader) {
             this.resourceLoader = resourceLoader;
         }
     }
     
     /**
     * 实现接口ApplicationContextAware
     **/
     public class Foo implements ApplicationContextAware {
     
         private ResourceLoader resourceLoader;
         
         // 依赖注入ResourceLoader
         public void setApplicationContext(ApplicationContext applicationContext) 
             throws BeansException {
             this.resourceLoader = applicationContext;
         }
     }
     ```

- Resource类型的注入

  1. 应用背景

     对于BeanFactory来说，容器可以将bean定义文件中的字符串形式表达的信息，正确地转换成具体对象定义的依赖类型。默认情况下，BeanFactory容器不会为org.springframework.core.io.Resource类型提供相应的PropertyEditor，需要注册自定义的PropertyEditor到BeanFactory容器。

     对于ApplicationContext来说，可以正确识别Resource类型并转换后注入相关对象。ApplicationContext启动伊始，会通过一个org.springframework.beans.support.ResourceEditorRegistrar来注册Spring提供的针对Resource类型的PropertyEditor实现到容器中，这个PropertyEditor叫做org.springframework.core.io.ResourceEditor。

  2. 实例

     ```java
     /**
     * 注入Resource类型
     **/
     public class Foo {
         // 持有Resource引用
         private Resource resourceLoader;
         
         // 依赖注入Resource类型
         public void setResourceLoader(Resource resourceLoader) {
             this.resourceLoader = resourceLoader;
         }
     }
     
     // 配置文件
     <bean id="mailer" class="...XMailer">
         // 主要设置String类型的文件地址,就可以自动转换为Resource类型
     	<property name="template" value="..resources.default_template.vm"/>
     < /bean>
     
     ```

     

- ApplicationContext的Resource加载行为

  1. 资源加载路径

     ResourceLoader中的资源路径协议--classpath，ResourcePatternResolver另外还有--classpath*

     classpath*与classpath的唯一区别就在于，如果能够在classpath中找到多个指定的资源，则返回多个。

  2. 默认加载行为

     当ClassPathXmlApplicationContext在实例化的时候，即使没有指明classpath:或者classpath*:等前缀，会默认从classpath中加载bean定义配置文件。

     FileSystemXmlApplicationContext会尝试从文件系统中加载bean定义文件。FileSystemXmlApplicationContext之所以如此，是因为它与org.springframework.core.io.FileSystemResourceLoader一样，也覆写了DefaultResourceLoader的getResourceByPath(String)方法，逻辑跟FileSystemResourceLoader一模一样。

     如果添加classpath前缀，则FileSystemXmlApplicationContext加载的资源是ClassPathResource类型。如果去掉这个前缀，则默认从文件系统加载资源。

### 3.国际化信息支持（省略不看）

###4.容器内部事件发布

Spring的ApplicationContext容器提供的容器内事件发布功能，是通过提供一套基于JavaSE标准自定义事件类而实现的。

#### 4.1自定义事件发布

- Java类结构图

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/10_自定义事件发布类结构图.png)

  1. EventObject：表示自定义的事件类型。自定义事件类型可以通过扩展EventObject来实现。
  2. EventListener：表示自定义的事件监听器。事件的监听器可以扩展自EventListener实现。
  3. EventPublisher：表示自定义的事件发布者。

- 实例

  ```java
  /**
  * 自定义事件类型
  **/
  public class MethodExecutionEvent extends EventObject implements Serializable {
  
      private static final long serialVersionUID = 4843493753232370425L;
      private String methodName;
  
      // 默认构造器
      public MethodExecutionEvent(Object source) {
          super(source);
      }
  
      public MethodExecutionEvent(Object source, String methodName) {
          super(source);
          this.methodName = methodName;
      }
  
      public String getMethodName() {
          return methodName;
      }
  
      public void setMethodName(String methodName) {
          this.methodName = methodName;
      }
  }
  
  /**
  * 自定义事件监听器接口：自定义事件监听器类只负责监听其对应的自定义事件并进行处理
  **/
  public interface MethodExecutionEventListener extends EventListener {
      // 处理方法开始执行的时候发布的MethodExecutionEvent事件
      void onMethodBegin(MethodExecutionEvent evt);
  
      // 处理方法执行将结束时候发布的MethodExecutionEvent事件
      void onMethodEnd(MethodExecutionEvent evt);
  }
  
  // 实现自定义事件监听器的接口
  public class SimpleMethodExecutionEventListener implements MethodExecutionEventListener {
      
      public void onMethodBegin(MethodExecutionEvent evt) {
          String methodName = evt.getMethodName();
          System.out.println("start to execute the method["+methodName+"].");
      }
  
      public void onMethodEnd(MethodExecutionEvent evt) {
          String methodName = evt.getMethodName();
          System.out.println("finished to execute the method["+methodName+"].");
      }
  }
  
  /**
  * 组合事件类和监听器，发布事件
  **/
  public class MethodExeuctionEventPublisher {
  
      // 发布事件实例
      public static void main(String[] args){
          // 1.添加监听器对象
          MethodExeuctionEventPublisher publisher = new MethodExeuctionEventPublisher();
          publisher.addMethodExecutionEventListener(new SimpleMethodExecutionEventListener());
          // 2.发布事件
          publisher.methodToMonitor();
      }
  
      // 声明基础监听器集合
      private List<MethodExecutionEventListener> listeners = 
          new ArrayList<MethodExecutionEventListener>();
      
      // 初始化事件对象以及发布事件
      public void methodToMonitor() {
          // 1.初始化事件对象
          MethodExecutionEvent event2Publish = new MethodExecutionEvent(this,"methodToMonitor");
  
          // 2.发布事件开始
          publishEvent(MethodExecutionStatus.BEGIN,event2Publish);
  
          // 3.发布事件结束
          publishEvent(MethodExecutionStatus.END,event2Publish);
      }
  
      // 发布事件
      protected void publishEvent(
          MethodExecutionStatus status,MethodExecutionEvent methodExecutionEvent) {
          // 复制一份事件监听集合-深拷贝
          List<MethodExecutionEventListener> copyListeners = 
              new ArrayList<MethodExecutionEventListener>(listeners);
  
          // 遍历事件监听对象,让每个对象都对事件做出反应
          for(MethodExecutionEventListener listener:copyListeners)
          {
              if(MethodExecutionStatus.BEGIN.equals(status)) {
                  listener.onMethodBegin(methodExecutionEvent);
              } else {
                  listener.onMethodEnd(methodExecutionEvent);
              }
          }
      }
  
  
      // 添加监听器对象
      public void addMethodExecutionEventListener(MethodExecutionEventListener listener) {
          this.listeners.add(listener);
      }
      
      // 移除监听器对象
      public void removeListener(MethodExecutionEventListener listener) {
          if(this.listeners.contains(listener)) {
              this.listeners.remove(listener);
          }
      }
      
      // 移除所有监听器
      public void removeAllListeners() {
          this.listeners.clear();
      }
  }
  ```

- 事件发布的关注点

  1. 具体时点上自定义事件的发布。

     为了避免事件处理期间事件监听器的注册或移除操作影响处理过程，对事件发布时点的监听器列表进行了一个安全复制(safe-copy)。

     事件的发布是顺序执行，所以为了能够不影响处理性能，事件监听器的处理逻辑应该尽量简短。

  2. 自定义事件监听器的管理。

#### 4.2Spring的容器内事件发布类结构分析

- ApplicationEvent-自定义事件

  ```java
  public abstract class ApplicationEvent extends EventObject {}
  ```

  默认情况下，Spring提供了三个实现。

  1. ContextClosedEvent：ApplicationContext容器在即将关闭的时候发布的事件类型。
  2. ContextRefreshedEvent：ApplicationContext容器在初始化或者刷新的时候发布的事件类型。
  3. RequestHandledEvent：Web请求处理后发布的事件，其有一子类ServletRequestHandledEvent提供特定于Java EE的Servlet相关事件。

- ApplicationListener-事件监听器

  ```java
  public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {}
  ```

  1. ApplicationContext容器在启动时，会自动识别并加载EventListener类型bean定义，一旦容器内有事件发布，将通知这些注册到容器的EventListener。
  2. 一旦容器内发布ApplicationEvent及其子类型的事件，注册到容器的ApplicationListener就会对这些事件进行处理。

- ApplicationContext-事件发布者

  ApplicationContext容器担当的是事件发布者的角色。

  ```java
  // 继承了ApplicationEventPublisher事件发布者接口
  public interface ApplicationContext extends 
      EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
  		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {}
  
  // ApplicationEventPublisher接口定义
  public interface ApplicationEventPublisher {
  	// 发布ApplicationEvent事件
  	void publishEvent(ApplicationEvent event);
  	// 发布Object事件
  	void publishEvent(Object event);
  
  }
  ```

  

- Spring事件发布流程

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/11_Spring事件发布类图.png)

  1. ApplicationContext容器的具体实现类在实现事件的发布和事件监听器的注册方面，将功能委托于接口ApplicationEventMulticaster实现，该接口定义了**具体事件监听器的注册管理**以及**事件发布**的方法。
  2. AbstractApplicationEventMulticaster抽象类，实现接口ApplicationEventMulticaster，实现了事件监听器的管理
     功能，事件的发布功能则委托给了其子类。
  3. SimpleApplicationEventMulticaster具体实现类，继承于AbstractApplicationEventMulticaster抽象类。添加了事件发布功能的实现，默认使用了SyncTaskExecutor进行事件的发布。
  4. 容器启动伊始，就会检查容器内是否存在名称为applicationEventMulticaster的ApplicationEventMulticaster对象实例。如果有就使用，如果没有则默认初始化一个SimpleApplicationEventMulticaster作为将会使用的ApplicationEventMulticaster。

#### 4.3Spring容器内事件发布的应用

- 应用场景

  1. Spring的ApplicationContext容器内的事件发布机制，主要用于单一容器内的简单消息通知和处理，并不适合分布式、多进程、多容器之间的事件通知。

- 实例

  ```java
  /**
  * 自定义ApplicationEvent事件
  **/
  public class AppEvent extends ApplicationEvent {
  
      private static final long serialVersionUID = -71960369269303337L;
  
      private String methodName;
  
      private MethodExecutionStatus methodExecutionStatus;
  
  
      public AppEvent(Object source) {
          super(source);
      }
  
      public AppEvent(Object source, String methodName, 
                      MethodExecutionStatus methodExecutionStatus) {
          super(source);
          this.methodName = methodName;
          this.methodExecutionStatus = methodExecutionStatus;
      }
  
      public static long getSerialVersionUID() {
          return serialVersionUID;
      }
  
      public String getMethodName() {
          return methodName;
      }
  
      public void setMethodName(String methodName) {
          this.methodName = methodName;
      }
  
      public MethodExecutionStatus getMethodExecutionStatus() {
          return methodExecutionStatus;
      }
  
      public void setMethodExecutionStatus(MethodExecutionStatus methodExecutionStatus) {
          this.methodExecutionStatus = methodExecutionStatus;
      }
  }
  
  /**
  * 自定义监听器,需要注册到Spirng容器中
  **/
  @Component
  public class AppListener implements ApplicationListener<ApplicationEvent> {
  
      public void onApplicationEvent(ApplicationEvent event) {
          // 判断捕获的事件对象
          if(event instanceof AppEvent) {
              switch (((AppEvent) event).getMethodExecutionStatus()) {
                  case BEGIN:
                      System.out.println("捕获方法开始-"+((AppEvent) event).getMethodName());
                  case END:
                      System.out.println("捕获方法结束-"+((AppEvent) event).getMethodName());
                  default:
                    break;
              }
          }
      }
  }
  
  /**
  * 事件发布,需要注册到Spring容器中
  **/
  @Component
  public class ApplicationPublisher implements ApplicationEventPublisherAware {
  
      public static void main(String[] args){
          ApplicationContext context = 
              new ClassPathXmlApplicationContext("classpath:ApplicationContext.xml");
  
          ApplicationPublisher publisher = 
              (ApplicationPublisher)context.getBean("applicationPublisher");
  
          publisher.methodToMonitor();
  
      }
  
      // 自动注入ApplicationEventPublisher实例
      private ApplicationEventPublisher publisher;
  
      public void methodToMonitor() {
          // 1.初始化事件对象
          AppEvent beginEvt = new AppEvent(this,"methodToMonitor",MethodExecutionStatus.BEGIN);
          // 2.发布对象
          this.publisher.publishEvent(beginEvt);
  
          AppEvent endEvt = new AppEvent(this,"methodToMonitor",MethodExecutionStatus.END);
          this.publisher.publishEvent(endEvt);
      }
  
  
      public void setApplicationEventPublisher
          (ApplicationEventPublisher applicationEventPublisher) {
          this.publisher = applicationEventPublisher;
      }
  }
  ```

### 5.EnvironmentCapable接口

- 应用背景：ApplicationContext继承接口EnvironmentCapable

  ```java
  public interface ApplicationContext extends 
      EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
  		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {...}
  ```


#### 5.1基本定义

- EnvironmentCapable和Environment接口

  ```java
  /**
  * 接口EnvironmentCapable的定义
  * 1.表示包括或暴露一个Environment环境引用的组件,用于应用上下文与环境交互.
  * 2.ApplicationContext扩展了EnvironmentCapable接口,通过getEnvironment方法暴露环境配置.
  * 3. ConfigurableApplicationContext将会重定义getEnvironment方法,返回一个ConfigurableEnvironment.
  * 4.ConfigurableApplicationContext的getEnvironment方法返回的环境对象时可修改的,在这之前是只读的.
  **/
  public interface EnvironmentCapable {
      // 返回组件关联的环境Environment，没有则为空。
  	Environment getEnvironment();
  }
  
  
  /**
  * 接口Environment的定义
  * 1.Environment接口表示当前应用正在运行的环境
  **/
  public interface Environment extends PropertyResolver {
      // 返回当前环境显示激活的配置集,配置用于创建有条件地注册bean定义的逻辑分组,比如基于开发环境的配置.
  	String[] getActiveProfiles();
  	// 当没有配置显示激活， 返回的默认将会被自动激活的配置集。
  	String[] getDefaultProfiles();
  	// 判断一个或多个配置是否激活,或者在默认显示激活的配置情况下,一个或多个配置是否在默认的配置集.
  	boolean acceptsProfiles(String... profiles);
  
  }
  s
   public interface PropertyResolver {...}
  ```

  

### 6.基于注解的依赖注入

- 使用注解的基础原理
  1. 首先提供一个BeanPostProcessor实现，让这个BeanPostProcessor在实例化bean定义的过程中，来检查当前对象是否有@Autowired标注的依赖需要注入。
  2. org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor就是Spring提供的用于这一目的的BeanPostProcessor实现。---用于Spring注解
  3. org.springframework.context.annotation.CommonAnnotationBeanPostProcessor是用于JSR250的注解。
- 注解体系
  1. 使用Spring提供的@Autowired和@Qualifier来标注相应类定义。
  2. 使用JSR250的@Resource和@PostConstruct以及@PreDestroy对相应类进行标注。