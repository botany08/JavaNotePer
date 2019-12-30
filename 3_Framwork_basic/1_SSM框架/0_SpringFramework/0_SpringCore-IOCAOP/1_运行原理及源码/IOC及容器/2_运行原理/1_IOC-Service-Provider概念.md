##IOC Service Provider

### 1.基础概念

#### 1.1定义

IoC Service Provider是一个抽象出来的概念，可以指代任何将IoC场景中的业务对象绑定到一起的实现方式。

#### 1.2作用

- 业务对象的构建管理。
  1. 在IoC场景中，业务对象无需关心所依赖的对象如何构建如何取得。
  2. IoC Service Provider需要将对象的构建逻辑从客户端对象那里剥离出来，以免这部分逻辑污染业务对象的实现。
- 业务对象间的依赖绑定。
  1. IoC Service Provider通过结合之前构建和管理的所有业务对象，以及各个业务对象间可以识别的依赖关系，将这些对象所依赖的对象注入绑定，从而保证每个业务对象在使用的时候，可以处于就绪状态。

### 2.对象间依赖关系的管理

#### 2.1直接编码方式

- 具体实现

  1. 在容器启动之前，可以通过程序编码的方式将被注入对象和依赖对象注册到容器中，并明确它们相互之间的依赖注入关系。
  2. 直接编码是最基本的实现，基本上都是各个IOC框架或容器各自实现。

  ```java
  // 1.初始化IOC容器
  IoContainer container = ...;
  // 2.构建各个业务对象,也就是注册Bean.
  container.register(FXNewsProvider.class,new FXNewsProvider());
  container.register(IFXNewsListener.class,new DowJonesNewsListener());
  ...
  // 3.将存在依赖关系的对象,绑定在一起,也就是依赖注入.    
  container.bind(IFXNewsListenerCallable.class, container.get(IFXNewsListener.class));
  ...
  // 4.通过容器获取已经实例化好的Bean    
  FXNewsProvider newsProvider = (FXNewsProvider)container.get(FXNewsProvider.class);
  newProvider.getAndPersistNews();
  
  ```

####2.2配置文件方式

- 具体实现

  1. 普通文本文件、properties文件、XML文件等，都可以成为管理依赖注入关系的载体。最主要的是通过XML文件进行配置。

  ```java
  /**
  * IOC容器的执行流程
  **/
  // 1.IOC容器读取配置文件中业务对象的注册以及依赖信息.
  container.readConfigurationFiles(...);
  // 2.通过容器获取已经实例化好的Bean
  FXNewsProvider newsProvider = (FXNewsProvider)container.getBean("newsProvider");
  newsProvider.getAndPersistNews();
  
  /**
  * XML配置文件
  **/
  // 注册类FXNewsProvider,ID为newsProvider
  <bean id="newsProvider" class="..FXNewsProvider">
      // 声明依赖关系
      <property name="newsListener">
      	<ref bean="djNewsListener"/>
      </property>
      <property name="newPersistener">
      	<ref bean="djNewsPersister"/>
  	</property>
  </bean>
  
  // 注册类DowJonesNewsListener,ID为djNewsListener    
  <bean id="djNewsListener" class="..impl.DowJonesNewsListener"/>
  // 注册类DowJonesNewsPersister,ID为djNewsPersister
  <bean id="djNewsPersister" class="..impl.DowJonesNewsPersister"/>
      
      
      
  ```

#### 2.3元数据方式

- 具体实现

  1.  元数据是指用来描述数据的数据，更通俗一点，就是描述代码间关系，或者代码与其他资源(例如数据库表)之间内在联系的数据。 
  2.  Java就是通过注解来表示元数据的。 

  ```java
  /**
  * IOC容器的执行流程
  **/
  // 1.IOC容器扫描整个Java代码获取注解信息,并建立Bean的注册信息以及依赖信息
  container.readConfigurationFiles(...);
  // 2.通过容器获取已经实例化好的Bean
  FXNewsProvider newsProvider = (FXNewsProvider)container.getBean("newsProvider");
  newsProvider.getAndPersistNews();
  ```

  

