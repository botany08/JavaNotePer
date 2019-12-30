## SpringIOC容器之BeanFactory

### 1.Spring容器的基础概念

#### 1.1Spring容器和IOC Sevice Provider的关系

Spring的IoC容器是一个提供IoC支持的轻量级容器，除此之外还提供了一些高级特性。

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_容器和ISP的关系.png)

#### 1.2Spring容器的分类

- BeanFactory

  1. 作用：基础类型IoC容器，提供完整的IoC服务支持。
  2. 初始化策略：默认采用延迟初始化策略(lazy-load)。当客户端对象需要访问容器中的某个受管对象的时候，才对
     该受管对象进行初始化以及依赖注入操作。
  3. 优点：容器启动初期较快，所需资源少。

- ApplicationContext

  1. 实现：ApplicationContext在BeanFactory的基础上构建的。
  2. 作用：除了拥有BeanFactory的所有支持，提供了其他高级特性，如统一资源加载策略，国际化信息支持，内部事件发布。
  3. 初始化策略：ApplicationContext所管理的对象，在该类型容器启动之后，默认全部初始化并绑定完成。
  4. 优点：可以提供更多的功能，所需的系统资源更多。

- BeanFactory和ApplicationContext的继承关系

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/2_BeanFactory继承图.png)



###2.BeanFactory容器中Bean信息的配置方式

- IOC的基本步骤
  1. 初始化Spring容器。
  2. 将Bean详细信息以及依赖信息，注册到Spring容器中。-此时还未实例化Bean
  3. 根据需要，显式或隐式实例化容器中的Bean。
  4. 获取到一个实例化的Bean

#### 2.1直接编码方式

- BeanFactory相关接口

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/3_BeanFactory相关接口.png)

1. BeanFactory接口
   - 定义了访问容器内管理的Bean的方法。
2. BeanDefinitionRegistry接口
   - 定义了Bean的注册逻辑
3. DefaultListableBeanFactory类
   - 实现了BeanFactory和BeanDefinitionRegistry
   - 可以访问容器内管理的Bean，负责往容器内注册Bean
4. BeanDefinition接口
   - 负责保存对象的所有必要信息，包括其对应的对象的class类型、是否是抽象类、构造方法参数以及其他属性等。
   - 作用是，当容器要实例化Bean的时候，通过BeanDefinition保存的对象信息去实例化对象。
5. RootBeanDefinition类
   - RootBeanDefinition是一个可合并的bean definition。就是在BeanFactory运行期间，可以返回一个特定的bean。RootBeanDefinition可以作为一个重要的通用的bean definition 视图 
6. ChildBeanDefinition类
   - ChildBeanDefinition是BeanDefinition的实现，对RootBeanDefinition有一定的依赖关系，可以继承父类的设置。

- 具体实现

  ```java
  /**
  * 容器实现：主体逻辑
  **/
  public class IniteIOC {
  
      // 启动容器-实例化Bean-调用Bean
      public static void main(String[] args){
  
          // 1.初始化BeanFactory容器的默认实现
          DefaultListableBeanFactory beanRegistry = new DefaultListableBeanFactory();
  
          // 2.将Bean的详细信息以及绑定信息，写入容器中.此时Bean还没有实例化
          BeanFactory container = bindViaCode(beanRegistry);
  
          // 3.实例化Bean,根据接口BeanDefinition实例保存的Bean信息
          FXNewsProvider newsProvider = (FXNewsProvider)container.getBean("djNewsProvider");
  
          // 4.调用实例化对象的方法
          newsProvider.getAndPersistNews();
      }
  
  
      // 将Bean详细信息以及绑定信息,写入BeanFactory
      public static BeanFactory bindViaCode(BeanDefinitionRegistry registry) {
  
          // 1.新建Bean的详细信息，通过RootBeanDefinition新建一个Bean的详细信息对象.
          //   已经封装好,不用我们手动解析对象信息。
          AbstractBeanDefinition newsProvider = 
              new RootBeanDefinition(FXNewsProvider.class);
          AbstractBeanDefinition newsListener = 
              new RootBeanDefinition(DowJonesNewsListener.class);
          AbstractBeanDefinition newsPersister = 
              new RootBeanDefinition(DowJonesNewsPersister.class);
  
          // 2.将Bean详细信息注册到容器中,BeanDefinitionRegistry接口负责注册逻辑
          registry.registerBeanDefinition("djNewsProvider", newsProvider);
          registry.registerBeanDefinition("djListener", newsListener);
          registry.registerBeanDefinition("djPersister", newsPersister);
  
          // 3.将Bean之间的依赖绑定信息注册到容器中,BeanDefinitionRegistry接口负责注册逻辑
          // 3.1 构造方法注入方式-需要重载构造方法
          ConstructorArgumentValues argValues = new ConstructorArgumentValues();
          argValues.addIndexedArgumentValue(0, newsListener);
          argValues.addIndexedArgumentValue(1, newsPersister);
          newsProvider.setConstructorArgumentValues(argValues);
  
          // 3.2 setter方法注入方式-需要一个空的构造方法
          MutablePropertyValues propertyValues = new MutablePropertyValues();
          propertyValues.addPropertyValue(new PropertyValue("newsListener",newsListener));
          propertyValues.addPropertyValue(new PropertyValue("newsPersister",newsPersister));
          newsProvider.setPropertyValues(propertyValues);
  
          return (BeanFactory)registry;
      }
  }
  
  
  
  // Bean：接口IFXNewsListener
  public interface IFXNewsListener {}
  public class DowJonesNewsListener implements IFXNewsListener{}
  
  // Bean：接口IFXNewsPersister
  public interface IFXNewsPersister {}
  public class DowJonesNewsPersister implements IFXNewsPersister {}
  
  // Bean：类FXNewsProvider
  public class FXNewsProvider {
      // 依赖：持有IFXNewsListener引用
      private IFXNewsListener newsListener;
      // 依赖：持有IFXNewsPersister引用
      private IFXNewsPersister newsPersister;
      public FXNewsProvider(){}
  
      // 构造注入
      public FXNewsProvider(IFXNewsListener newsListener, IFXNewsPersister newsPersister) {
          this.newsListener = newsListener;
          this.newsPersister = newsPersister;
      }
  
      // setter注入属性newsListener
      public void setNewsListener(IFXNewsListener newsListener) {
          this.newsListener = newsListener;
      }
  	// setter注入属性newsPersister
      public void setNewsPersister(IFXNewsPersister newsPersister) {
          this.newsPersister = newsPersister;
      }
  	
      // 方法调用
      public void getAndPersistNews() {
          System.out.println("创建了一个实例!");
      }
  }
  ```

#### 2.2外部配置文件方式

Spring的IoC容器支持两种配置文件格式：Properties文件格式和XML文件格式。

- 实现步骤

  1. Spring需要根据不同的外部配置文件格式，给出相应的BeanDefinitionReader实现类。

  2. BeanDefinitionReader实现类，将相应的配置文件内容读取并映射到BeanDefinition。

     主要是解析文件格式、装配BeanDefinition等工作内容。

  3. 将映射后的BeanDefinition注册到一个BeanDefinitionRegistry，就完成了Bean的注册和加载。

     主要是负责保管BeanDefinition的信息。

  ```java
  // 1.声明一个BeanDefinitionRegistry注册类,通常为DefaultListableBeanFactory
  BeanDefinitionRegistry beanRegistry = DefaultListableBeanFactory;

  // 2.新建解析配置文件的BeanDefinitionReader类,reader读取类持有一个register注册类的引用
  BeanDefinitionReader beanDefinitionReader = new BeanDefinitionReaderImpl(beanRegistry);
  
  // 3.读取并且解析配置文件,将内容映射到BeanDefinition,并同时注册到register类中
  beanDefinitionReader.loadBeanDefinitions("配置文件路径");
  
  // 4.因为DefaultListableBeanFactory同时实现了register和beanfactory接口,所以可以当容器和注册类
  BeanFactory beanFactory = (BeanFactory)beanRegistry;
  ```
  
- BeanDefinitionReader接口

  1. PropertiesBeanDefinitionReader类用于Properties格式配置文件的加载。
  2. XmlBeanDefinitionReader类用于Properties格式配置文件的加载。

  ```java
  // 对于XML配置文件,可以直接使用XmlBeanFactory读取文件内容并返回一个BeanFactory类
  return new XmlBeanFactory(new ClassPathResource("../news-config.xml"))
  ```

#### 2.3注解方式

- 基本注解
  1. 可以使用@Autowired以及@Component对相关类进行Bean详细信息以及依赖信息的注册
  2. 需要提前开启组件扫描@ComponentScan

### 3.Spring容器的功能

#### 3.1功能实现流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_Spring容器的功能.png)

- 容器启动阶段（准备阶段）
  1. 加载Configuration MetaData配置信息。分为三种方式：直接编码、外部文件配置和注解。
  2. 分析配置信息。通过BeanDefinitionReader接口实现类，解析和分析配置信息，并将分析后的信息编组为相应的BeanDefinition。
  3. 注册信息。将保存了Bean信息的BeanDefinition，注册到相应的BeanDefinitionRegistry。
  4. 除了对Bean管理信息的收集，还包括一些验证性或者辅助性的工作。
- Bean实例化阶段
  1. 触发场景。当某个请求方通过容器的getBean方法明确地请求某个对象，或者因依赖关系容器需要隐式地调用getBean方法。
  2. 检查请求的Bean。检查请求实例化的Bean是否有已经实例化，如果有则直接返回。
  3. 实例化Bean。如果Bean尚未实例化，则会根据注册的BeanDefinition所提供的信息实例化被请求对象，并为其注入依赖。
  4. 回调接口。如果该对象实现了某些回调接口，也会根据回调接口的要求来装配它。
  5. 装配完毕。容器会立即将其返回请求方使用。

#### 3.2容器启动阶段

- 容器扩展机制

  1. Spring提供了一种叫做BeanFactoryPostProcessor的容器扩展机制。可以在容器实例化相应对象之前，对注册到容器的BeanDefinition所保存的信息做相应的修改。
  2. 相当于在容器实现的第一阶段最后加入一道工序，可以对已经保存的BeanDefinition进行修改，比如修改其中bean定义的某些属性，为bean定义增加其他信息等。

- BeanFactoryPostProcessor接口

  1. 一个容器可以拥有多个BeanFactoryPostProcessor，并按照指定顺序执行。
  2. 自定义实现BeanFactoryPostProcessor接口，需要同时实现Ordered接口，以保证执行的顺序。
  3. Spirng提供了BeanFactoryPostProcessor接口的默认实现类，常用的分别是CustomEditorConfigurer、PropertyPlaceholderConfigurer和PropertyOverrideConfigurer。
  4. 实现类CustomEditorConfigurer，用来注册自定义的PropertyEditor以补助容器中默认的PropertyEditor。为了处理配置文件中的数据类型与真正的业务对象所定义的数据类型转换。

- 应用方式

  1. 对于BeanFactory。

     ```java
     /**
     * 手动应用BeanFactoryPostProcessor到BeanFactory
     **/
     // 1.声明将被后处理的BeanFactory实例
     ConfigurableListableBeanFactory beanFactory = 
         new XmlBeanFactory(newClassPathResource("..."));
     // 2.声明要使用的BeanFactoryPostProcessor,并进行配置
     PropertyPlaceholderConfigurer propertyPostProcessor = new PropertyPlaceholderConfigurer();
     propertyPostProcessor.setLocation(new ClassPathResource("..."));
     // 3.执行后处理操作,应用到BeanFactory中
     propertyPostProcessor.postProcessBeanFactory(beanFactory);
     ```

  2. 对于ApplicationContext。

     ```java
     /**
     * 只要配置BeanFactoryPostProcessor实例,就可以被自动识别
     **/
     <beans>
     	<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
     		<property name="locations">
     			<list>
     				<value>conf/jdbc.properties</value>
     				<value>conf/mail.properties</value>
     			</list>
     		</property>
     	</bean>
     ...
     </beans>
     ```

##### 3.2.1PropertyPlaceholderConfigurer类

- 作用

  1. 对于一些需要频繁改动的配置，如数据库连接信息、邮件服务器，可以在XML配置文件中使用占位符(PlaceHolder)， 并将这些占位符所代表的资源单独配置到简单的properties文件中来加载。
  2. 简而言之，就是PropertyPlaceholderConfigurer会通过加载额外的配置文件，对已加载的配置文件进行完善，应用的手段就是占位符。

- 具体应用

  ```java
  // xml配置文件
  <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
  	<property name="url">
  		<value>${jdbc.url}</value>
  	</property>
  	<property name="driverClassName">
  		<value>${jdbc.driver}</value>
  	</property>
  	<property name="username">
  		<value>${jdbc.username}</value>
  	</property>
  	<property name="password">
  		<value>${jdbc.password}</value>
  	</property>
  </bean>
      
  // properties配置文件
  jdbc.url=jdbc:mysql://server/MAIN?useUnicode=true&characterEncoding=ms932&failOverReadOnly=false
  jdbc.driver=com.mysql.jdbc.Driver
  jdbc.username=your username
  jdbc.password=your password
      
  ```

  1. 当BeanFactory在第一阶段加载完成所有配置信息时，BeanFactory中保存的对象的属性信息还只是以占位符的形式存在，如${jdbc.url}。
  2. 当PropertyPlaceholderConfigurer作为BeanFactoryPostProcessor被应用时，会使用properties配置文件中的配置信息来替换相应BeanDefinition中占位符所表示的属性值。
  3. PropertyPlaceholderConfigurer不单会从其配置的properties文件中加载配置项，同时还会检查Java的System类中的Properties。
     - 通过setSystemPropertiesMode()或者setSystemPropertiesModeName()来控制是否加载或者覆盖System相应Properties的行为。
  4. PropertyPlaceholderConfigurer默认采用SYSTEM_PROPERTIES_ MODE_FALLBACK模式，如果properties文件中找不到相应配置项，则到System的Properties中查找。

##### 3.2.2PropertyOverrideConfigurer类

- 作用

  1. 可以通过PropertyOverrideConfigurer对容器中配置的，任何需要处理的bean定义的property信息进行覆盖替换，修改了最终的BeanDefinition信息。
  2. 简而言之，就是PropertyOverrideConfigurer的properties文件中的配置项，覆盖掉了原来XML中的bean定义的property信息。

- 具体应用

  ```java
  // xml配置文件
  <bean class="org.springframework.beans.factory.config.PropertyOverrideConfigurer">
      // 表示应用pool-adjustment.properties配置文件
  	<property name="location" value="pool-adjustment.properties"/>
  </bean>
  
  // properties配置文件
  # pool-adjustment.properties
  // 表示bean实例dataSource的属性maxActive,最终的结果是50   
  dataSource.minEvictableIdleTimeMillis=1000
  dataSource.maxActive=50
  
  ```

  

##### 3.2.3CustomEditorConfigurer类

- 作用

  1. 辅助性地将后期会用到的信息注册到容器，对BeanDefinition没有做任何变动。
  2. 容器从XML格式的文件中读取的都是字符串形式，Spring内部通过JavaBean的PropertyEditor来帮助进行String类型到其他类型的转换工作。
  3. CustomEditorConfigurer就是自定义的PropertyEditor接口的实例，注册到BeanFactory中。

- 默认的PropertyEditor接口

  1. Spring容器内部在做具体的类型转换的时候，会采用JavaBean框架内默认的PropertyEditor搜寻逻辑，从而继承了对原生类型以及java.lang.String.java.awt.Color和java.awt.Font等类型的转换支持。

  2. Spring框架还提供了自身实现的一些PropertyEditor，并且容器通常会默认加载使用。

     - StringArrayPropertyEditor

       将符合CSV 格式的字符串转换成String[]数组的形式，默认是以逗号分隔的字符串。

     - ClassEditor

       根据String类型的class名称，直接将其转换成相应的Class对象，相当于通过Class.forName(String)完成的功效。

     - FileEditor

       Spring提供的对应java.io.File类型的PropertyEditor。同属于对资源进行定位的PropertyEditor还有InputStreamEditor、URLEditor等。

     - LocaleEditor

       针对java.util.Locale类型的PropertyEditor。

     - PatternEditor

       针对Java SE 1.4之后才引入的java.util.regex.Pattern的PropertyEditor

- 自定义PropertyEditor接口

  1. 通常情况下，对于Date类型，不同的Locale、不同的系统在表现形式上存在不同的需求。
  2. 需要实现PropertyEditor接口，不过一般继承PropertyEditorSupport类，避免实现所有方法。
  3. 写方法setAsText(String)即如果仅仅是支持单向的从String到相应对象类型的转换，只要重写方法setAsText(String)即可。如果支持双向转换，也要重写getAsText()方法。

- 注册自定义的PropertyEditor接口（详细的再了解）

  1. 对于BeanFactory来说，直接用编码注入即可。
  2. 对于ApplicationContextL来说，需要在xml配置或者其他配置文件。


####3.3Bean的实例化阶段

- 实例化的时机

  当容器启动完成之后，容器现在仅仅拥有所有对象的BeanDefinition来保存实例化阶段将要用的必要信息。只有当请求方通过BeanFactory的getBean()方法来请求某个对象实例的时候，才有可能触发Bean实例化阶段的活动。

  BeanFactory的getBean法可以被客户端对象显式调用，也可以在容器内部隐式地被调用。

  1. 对于BeanFactory来说，对象实例化默认采用延迟初始化。

     当对象A被请求而需要第一次实例化的时候，如果它所依赖的对象B之前同样没有被实例化，那么容器会先实例化对象A所依赖的对象，再实例化A对象。

  2. ApplicationContext启动之后会实例化所有的bean定义。

     ApplicationContext在启动阶段的活动完成后，直接调用注册到该容器的所有bean定义的实例化方法getBean()。

     当得到容器引用的时候，容器内所有的对象已经全被实例化完成。

     这个容器实例化的动作体现在AbstractApplicationContext的refresh()方法。

- Bean实例化过程

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/5_Bean的实例化过程.png)

  1. 可以在org.springframework.beans.factory.support.AbstractBeanFactory类的代码中查看到getBean()方法的完整实现逻辑。
  2. 可以在其子类org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory的代码中看到createBean()方法的实现逻辑。

##### 3.3.1Bean的实例化与BeanWrapper

- 实例化方式-第一步

  1. 容器在内部实现的时候，采用“策略模式(Strategy Pattern)”来决定采用何种方式初始化bean实例。
  2. 通常，可以通过反射或者CGLIB动态字节码生成，来初始化相应的bean实例或者动态生成其子类。

- InstantiationStrategy接口-第一步

  1. org.springframework.beans.factory.support.InstantiationStrategy定义是实例化策略的抽象接口。

  2. SimpleInstantiationStrategy类实现了InstantiationStrategy接口，具备简单的对象实例化功能，通过反射来实例化对象实例，但不支持方法注入方式的对象实例化。

  3. CglibSubclassingInstantiationStrategy类继承了SimpleInstantiationStrategy类，可以用反射方式实例化对象。

     也可以通过CGLIB的动态字节码生成功能，动态生成某个类的子类，进而满足方法注入所需的对象实例化需求。

  4. 默认情况下，容器内部采用的是CglibSubclassingInstantiationStrategy。

- 执行实例化对象-第一步

  1. 容器只要根据相应bean定义的BeanDefintion取得实例化信息，结合CglibSubclassingInstantiationStrategy以及不同的bean定义类型，就可以返回实例化完成的对象实例。
  2. 实例化完对象后，以BeanWrapper对构造完成的对象实例进行包裹，返回相应的BeanWrapper实例。

- BeanWrapper接口-第二步

  1. 实现类org.springframework.beans.BeanWrapperImpl，作用是对某个bean进行“包裹”，然后对这个“包裹”的bean进行操作，比如设置或者获取bean的相应属性值。

  2. BeanWrapper继承了三个接口，PropertyAccessor接口、PropertyEditorRegistry接口和TypeConverter接口。

  3. PropertyAccessor接口作用是，以统一的方式对对象属性进行访问。

  4. PropertyEditorRegistry接口作用是，用来注册PropertyEditor接口。

     Spring会根据对象实例构造一个BeanWrapperImpl实例，再将之前注册的PropertyEditor接口实例复制到BeanWrapperImpl实例中，以便BeanWrapper转换类型、设置对象属性值。

  5. 使用BeanWrapper对bean实例操作很方便，可以免去直接使用Java反射API(Java Reflection API)操作对象实例的烦琐。

     ```java
     /**
     * 使用BeanWrapper操作对象
     **/
     // 1.通过反射实例化对象
     Object provider = Class.forName("package.name.FXNewsProvider").newInstance();
     Object listener = Class.forName("package.name.DowJonesNewsListener").newInstance();
     Object persister = Class.forName("package.name.DowJonesNewsPersister").newInstance();
     
     // 2.使用BeanWrapper操作对象,设置属性
     BeanWrapper newsProvider = new BeanWrapperImpl(provider);
     // 设置属性newsListener和newPersistener
     newsProvider.setPropertyValue("newsListener", listener);
     newsProvider.setPropertyValue("newPersistener", persister);
     
     assertTrue(newsProvider.getWrappedInstance() instanceof FXNewsProvider);
     assertSame(provider, newsProvider.getWrappedInstance());
     assertSame(listener, newsProvider.getPropertyValue("newsListener"));
     assertSame(persister, newsProvider.getPropertyValue("newPersistener"));
     ```

##### 3.3.2Aware相关接口

- 检查Aware接口-第三步

  1.  Aware 接口为 Spring 容器的核心接口，是一个具有标识作用的超级接口，实现了该接口的 bean 是具有被Spring 容器通知的能力，通知的方式是采用回调的方式 。

  2. 当对象实例化完成并且相关属性以及依赖设置完成之后，Spring容器会检查当前对象实例是否实现了一系列的以Aware命名结尾的接口定义。如果是，则将这些Aware接口定义中规定的依赖注入给当前对象实例。

     **Bean实例是什么时候实现了Aware实例的？TODO**

- Aware接口分类-第三步

  对BeanFactory类型的容器有以下几个Aware接口：

  1. BeanNameAware

     如果Spring容器检测到当前对象实例实现了该接口，会将该对象实例的bean定义对应的beanName设置到当前对象实例。

  2. BeanClassLoaderAware

     如果容器检测到当前对象实例实现了该接口，会将对应加载当前bean的Classloader注入当前对象实例。默认会使用加载org.springframework.util.ClassUtils类的Classloader。

  3. BeanFactoryAware

     如果对象声明实现了BeanFactoryAware接口，BeanFactory容器会将自身设置到当前对象实例。当前对象实例就拥有了一个BeanFactory容器的引用，并且可以对这个容器内允许访问的对象按照需要进行访问。

  对于ApplicationContext类型容器有一下几个Aware接口：

  1. ResourceLoaderAware 

     ApplicationContext 实现了Spring的ResourceLoader接口。

     当容器检测到当前对象实例实现了ResourceLoaderAware接口之后，会将当前ApplicationContext自身设置到对象实例。当前对象实例就拥有了其所在ApplicationContext容器的一个引用。

  2. ApplicationEventPublisherAware 

     当前ApplicationContext容器如果检测到当前实例化的对象实例声明了ApplicationEventPublisherAware接口，则会将自身注入当前对象。此时的ApplicationContext是可以作为ApplicationEventPublisher来使用的。

  3. MessageSourceAware

     ApplicationContext通过MessageSource接口提供国际化的信息支持，自身就实现了MessageSource接口。当检测到当前对象实例实现了MessageSourceAware接口，则会将自身注入当前对象实例。

  4. ApplicationContextAware

     如果ApplicationContext容器检测到当前对象实现ApplicationContextAware接口，则会将自身注入当前对象实例。

##### 3.3.3BeanPostProcessor接口

- 主要作用

  1. BeanPostProcessor是存在于对象实例化阶段，而BeanFactoryPostProcessor则是存在于容器启动阶段。
  2. BeanPostProcessor会处理容器内所有符合条件的实例化后的对象实例。

  ```java
  /**
  * BeanPostProcessor接口定义
  **/
  public interface BeanPostProcessor {
  	// 对应的实例化步骤是：BeanPostProcessor前置处理
  	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;
      // 对应的实例化步骤是：BeanPostProcessor后置处理
  	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
  }
  ```

- 应用场景

  1. 主要工作内容是，处理标记接口实现类，或者为当前对象提供代理实现，替换当前对象实例或者字节码增强当前对象实例等。Spring的AOP更多的是为对象生成相应的代理对像。

  2. ApplicationContext对应的Aware接口，是通过BeanPostProcessor前置处理的。

     ApplicationContext容器会检测到，之前注册到容器的ApplicationContextAwareProcessor(实现了BeanPostProcessor)。然后就会调用其postProcessBeforeInitialization()方法，检查并设置Aware相关依赖。

- 扩展-自定义BeanPostProcessor

  1. 应用场景
  
     IFXNewsListener实现类在注册Bean信息的时候，已经设置了系统保存的加密密码。IFXNewsListener进行实例化的时候，首先需要对系统中取得的密码进行解密，然后才能应用IFXNewsListener中的密码。采用BeanPostProcessor技术，对所有的IFXNewsListener的实现类进行统一的解密操作。
  
  ```java
  /**
  * 1.标注需要进行解密的实现类
  **/
  public interface PasswordDecodable {
      String getEncodedPassword();
      void setDecodedPassword(String password);
  }
  public class DowJonesNewsListener implements IFXNewsListener,PasswordDecodable {
      private String password;
      
      public String[] getAvailableNewsIds() {}    
      public FXNewsBean getNewsByPK(String newsId) {}
      public void postProcessIfNecessary(String newsId) {}
      
      public String getEncodedPassword() {
      	return this.password;
      }
      public void setDecodedPassword(String password) {
      	this.password = password;
      }
  }
  
  /**
  * 2.实现相应的BeanPostProcessor对符合条件的Bean实例进行处理
  **/
  public class PasswordDecodePostProcessor implements BeanPostProcessor {
      // BeanPostProcessor后置处理
      public Object postProcessAfterInitialization(Object object, String beanName) 
          throws BeansException {
      		return object;
      }
      
      // BeanPostProcessor前置处理
      public Object postProcessBeforeInitialization(Object object, String beanName)
      	throws BeansException {
          	// 对于需要解码的类,进行解码
              if(object instanceof PasswordDecodable) {
              	String encodedPassword = ((PasswordDecodable)object).getEncodedPassword();
              	String decodedPassword = decodePassword(encodedPassword);
              	((PasswordDecodable)object).setDecodedPassword(decodedPassword);
              }
          
              return object;
      }
      
      // 解码方法
      private String decodePassword(String encodedPassword) {
          // 实现解码逻辑
          return encodedPassword;
      }
      
  }
  
  /**
  * 3.将自定义的BeanPostProcessor注册到容器
  * 对于BeanFactory类型的容器来说，我们需要通过手工编码的方式将相应的BeanPostProcessor注册到容器
  * 对于ApplicationContext容器来说，直接将相应的BeanPostProcessor实现类通过通常的XML配置文件配置一下即可
  **/
  ```
  
    
  

##### 3.3.4InitializingBean和init-method

- InitializingBean接口-自定义的初始化方法
  1. org.springframework.beans.factory.InitializingBean是容器内部广泛使用的一个对象生命周期标识接口。
  2. 会接着检测当前对象是否实现了InitializingBean接口，如果是，则会调用其afterPropertiesSet()方法进一步调整对象实例的状态。
  3. 该接口在Spring容器中普遍使用，但是一般不让业务对象实现该接口，防止代码侵入。
- Bean的init-method属性-自定义的初始化方法
  1. 通过设置该属性，可以实现自定义的初始化方法。

##### 3.3.5DisposableBean与destroy-method

- DisposableBean接口-自定义的销毁方法方法
  1. 与InitializingBean接口类似，一般不让业务对象实现该接口，防止代码侵入。
- destroy-method属性-自定义的销毁方法方法
  1. 在BeanPostProcessor后置处理之后，检查该属性的值，为该实例注册一个用于对象销毁的回调(Callback)，为对象提供了自定义销毁的方法。
  2. 在对象实例初始化完成并注册了相关的回调方法之后，并不会马上执行。回调方法注册后，返回的对象实例即处于使用状态，只有该对象实例不再被使用的时候，才会执行相关的自定义销毁逻辑。
  3. 对于各个scope的实例来说，除了prototype类型的bena实例，销毁方法会在各个合适的时机被调用。因为prototype对象实例在容器实例化并返回给请求方之后，容器就不再管理这种类型对象实例的生命周期了。

#### 3.4Bean的Scope

- Scope的作用
  1. 容器在对象进入其相应的scope之前，生成并装配这些对象，在该对象不再处于这些scope的限定之后，容器通常会销毁这些对象。
  2. scope用来声明容器中的对象所应该处的限定场景或者说该对象的存活时间。
- singleton
  1. 标记为拥有singleton scope的对象定义，在Spring的IoC容器中只存在一个实例，所有对该对象的引用将共享这个实例。该实例从容器启动，并因为第一次被请求而初始化之后，将一直存活到容器退出。
  2. 标记为singleton的bean是由容器来保证这种类型的bean在同一个容器中只存在一个共享实例。单例Singleton模式则是保证在同一个Classloader中只存在一个这种类型的实例。
  3. 通常情况下，如果你不指定bean的scope，singleton便是容器默认的scope。

- prototype
  1. 容器在接到该类型对象的请求的时候，会每次都重新生成一个新的对象实例给请求方。之后bean的生命周期就交给了请求方，不再由容器进行管理。
  2. 对于那些请求方不能共享使用的对象类型，应该将其bean定义的scope设置为prototype。通常，声明为prototype的scope的bean定义类型，都是一些有状态的，比如保存每个顾客信息的对象。 类似于线程私有对象。
- request -- WEB
  1. XmlWebApplicationContext会为每个HTTP 请求创建一个全新的RequestProcessor对象供当前请求使用，当请求结束后，该对象实例的生命周期即告结束。request可以看作prototype的一种特例。
- session -- WEB
  1. Spring容器会为每个独立的session创建属于它们自己的全新的UserPreferences对象实例。
  2. 与request相比，除了拥有session scope的bean的实例具有比request scope的bean可能更长的存活时间，其
     他方面真是没什么差别。
- global session -- WEB
  1. global session只有应用在基于portlet的Web应用程序中才有意义。如果在普通的基于servlet的Web应用中使用了这个类型的scope，容器会将其作为普通的session类型的scope对待。


