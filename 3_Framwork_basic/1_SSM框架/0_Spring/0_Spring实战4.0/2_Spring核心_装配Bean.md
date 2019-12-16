##装配Bean

###1.Spring配置的三种方式

- 装配的概念

  创建应用对象之间协作关系的行为通常称为装配(wiring)，这也是依赖注入(DI)的本质。

- 装配的三种机制

  1. 隐式的bean发现机制和自动装配。
  2. 在Java中进行显式配置。
  3. 在XML中进行显式配置。

- SpringIOC的工作方式

  1. 声明bean
  2. 构造器注入和Setter方法注入
  3. 装配bean
  4. 控制bean的创建和销毁
  
- 依赖注入的两种方式

  1. 构造注入，通过构造函数注入对象引用。
  2. 设值注入，通过普通方法注入对象引用。
  3. 对强依赖使用构造器注入，而对可选性的依赖使用属性注入。

###2.自动化装配bean

- 自动化装配分为两个部分
  1. 组件扫描(component scanning)：注册bean。Spring会自动发现应用上下文中所创建的bean。
  2. 自动装配(autowiring)：依赖注入。Spring自动注入bean之间的依赖。
  3. 组件扫描和自动装配都是通过注解实现的。

####2.1组件扫描-注册bean

- 启用组件扫描-注解@ComponentScan
  
  1. 组件扫描的范围，一般为基础包以及子包。按照默认规则，会以配置类所在的包作为基础包(base package)来扫描组件。可以在注解ComponentScan中定义Value属性配置单个基础包。以及可以在basePackages属性中配置多个基础包。
  2. 单纯启用组件扫描的应用场景，所有的对象都是独立的，彼此之间没有任何依赖。

  ```java
   // 指定该类为配置类
  @Configuration
  // 启用组件扫描。
  @ComponentScan 
  public class AutoConfig {}
  
  /** 
  * 组件扫描范围配置
  **/
  // 1.默认情况，启用组件扫描。
  @ComponentScan 
  // 2.value属性配置单个基础包
  @ComponentScan("autoIOC")
  // 3-1.basePackages属性配置单个或多个基础包
  @ComponentScan(basePackages={"autoIOC","javaIOC"})
  // 3-2.basePackages属性配置类,类所在的包将作为基础包
  @ComponentScan(basePackages={Student.class,Ball.class})
  
  ```

- 声明组件类(bean)-注解@Component
  

  1. BeanID的问题。默认情况下，Spring会根据类名为其指定一个ID，将类名的第一个字母变为小写。可在注解ComponentScan中配置自定义ID。
  2. 注解@Named(不推荐使用)。该注解是Java依赖注入规范中的注解，不是Spring特有的。Spring支持将@Named作为@Component注解的替代方案。两者之间有一些细微的差异，在大多数场景中是可以互相替换的。

  ```java
  // 配置自定义ID
  @Component("ball")
  public class Ball {
      public String ballName(){
          String name = "这是一个篮球.";
          return name;
      }
  }
  ```


- 初始化注解上下文AnnotationConfigApplicationContext

  1. AnnotationConfigApplicationContext该上下文只能使用配置类。如果要同时使用XML可以使用其他上下文，然后再XML中启用组件扫描

  ```java
  // 1.注解上下文
  public class RootIOC {
      public static void main(String[] args){
          //1.加载配置文件，启动Spring容器
          AnnotationConfigApplicationContext context
                  = new AnnotationConfigApplicationContext(AutoConfig.class);
          //2.使用Spring容器实例化bean对象
          Ball ball = (Ball) context.getBean("ball");
          //3.调用方法
          System.out.println(ball.ballName());
      }
  }
  
  // 2.xml文件上下文,并在xml文件中启用组件扫描
  public class RootIOC {
      public static void main(String[] args){
          //1.加载配置文件，启动Spring容器
          ApplicationContext context =
                  new ClassPathXmlApplicationContext("classpath:ApplicationContext.xml");
          //2.使用Spring容器实例化bean对象
          Ball ball = (Ball) context.getBean("ball");
          //3.调用方法
          System.out.println(ball.ballName());
      }
  }
  
  // xml文件启用组件扫描,然后会扫描到@Configuration注解类,这就可以实现注解类和配置文件一起使用.
  <context:component-scan base-package="com.joker.springBasic"/>
  ```

####2.2自动装配-依赖注入

- 依赖注入分为两种
  1. 构造注入。基于构造函数，注入依赖的引用。
  2. 设值注入。基于setter()方法，注入依赖的引用。

- 可以实现自动装配的注解

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/13_自动装配的注解.png)

- @Required

  1. 应用范围。只能应用在设值注入。
  2. 匹配机制。通过类型进行bean匹配。

  ```java
  // Ball类
  @Component
  public class Ball {
      public String ballName(){
          String name = "这是一个篮球.";
          return name;
      }
  }
  
  // Teacher类,将自动注入Ball类
  @Component
  public class Teacher {
      private Ball ball;
      @Required
      private void setBall(Ball ball) {
          this.ball = ball;
      }
  
      public void play() {
          System.out.println("老师开始打球了."+ball.ballName());
      }
  }
  ```

- @Autowired和@Resource

  1. 应用范围。两个注解都可以用于构造注入，设值注入以及属性上。
  2. 匹配机制。@Autowired通过类型进行匹配。@Resource则是通过名称进行匹配。
  3. 区别。@Autowired是Spring特有的注解。@Resource是Java依赖注入规范中的注解。

  ```java
  @Component
  public class Classmate {
      @Resource // 名称匹配
      private Ball ball;
      
      @Autowired  // 类型匹配
      private Ball ball;
  
      public void play() {
          System.out.println("同学要开始打球了."+ball.ballName());
      } 
  }
  ```

- @Qualifier

  1. 应用场景。当@Autowired进行bean查找时，如果有多个结果，@Qualifier则进行BeanID名称限定。实质是@Autowired确定类型，@Qualifier确定名称。

  ```java
  // Student类,自动注入Ball类
  @Component("student")
  public class Student {
  
      @Autowired  // 类型匹配
      @Qualifier("ball") // 名称匹配
      private Ball ball;
  
      public void play() {
          System.out.println("学生要开始打球了."+ball.ballName());
      }
  
  }
  ```

  2. 自定义注解。如果@Autowired和@Qualifier还不能确定一个Bean时，则可以使用自定义的注解继承@Qualifier，进行Bean的限定。

  ```java
  // 自定义注解Person
  @Target({ElementType.CONSTRUCTOR,ElementType.FIELD,
          ElementType.METHOD,ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Qualifier
  public @interface Person {
  }
  
  // 1.将自定义注解绑定在Bean上
  @Component("ball")
  @Person
  public class Ball {
      public String ballName(){
          String name = "这是一个篮球.";
          return name;
      }
  }
  
  // 2.使用自定义注解
  @Component
  public class Student {
      @Autowired
      @Person
      private Ball ball;
  
      public void play() {
          System.out.println("学生要开始打球了."+ball.ballName());
      }
  
  }
  ```

- @Primary-标记首先实例化的Bean

  1. 当自动匹配到多个Bean的时候，可以在Bean上标记首选。
  2. @Primary能够与@Component组合用在组件扫描的bean上，也可以与@Bean组合用在Java配置的bean声明中，使用XML配置bean<bean>元素有一个primary属性用来指定首选的bean。

  ```java
  // 1. 与@Component组合使用-自动装配
  @Component
  @Primary
  public class Ball {
      public String ballName(){
          String name = "这是一个篮球.";
          return name;
      }
  }
  
  // 2.与@Bean组合使用-Java配置
  @Configuration
  @ComponentScan
  public class AutoConfig {
   
      @Bean
      @Primary
      public Student getStudent(){
          return new Student();
      }
  }
  
  // 3.xml文件配置
  <bean id="student" class="com.joker.springBasic.springIOC.autoIOC.Student" primary="true"/>
  ```

#### 2.3Bean的创建和销毁

- @PostConstruct 和 @PreDestroy  
  1. @PostConstruct 注解表示在Bean创建前执行的方法。
  2. @PreDestroy 注解表示Bean销毁前执行的方法。

```java
@Component
public class Ball {
    public String ballName(){
        String name = "这是一个篮球.";
        return name;
    }

    @PostConstruct
    private void init() {
        System.out.println("Ball初始化...");
    }

    @PreDestroy
    private void destory() {
        System.out.println("Ball被销毁");
    }
}
```

###3.通过Java代码装配bean

####3.1创建配置类并声明Bean-@Configuration和@Bean

- 创建配置类
  1. 注解@Configuration的作用是，指定一个配置类。
- 声明注册Bean
  1. ID名称。默认情况下，Bean的ID为注解的方法名。可以通过name属性修改beanID名。

```java
@Configuration
public class JavaBeanConfig {

    @Bean
    public XiaoMi getXiaoMi() {
        return new XiaoMi();
    }

    @Bean(name = "jack")
    public Jack getJack() {
        return new Jack();
    }
}
```

- 初始化上下文AnnotationConfigApplicationContext

  ```java
  public class RootIOC {
      public static void main(String[] args){
          //1.加载配置文件，启动Spring容器
          AnnotationConfigApplicationContext context
                  = new AnnotationConfigApplicationContext(JavaBeanConfig.class);
          //2.使用Spring容器实例化bean对象
          XiaoMi xiaoMi = (XiaoMi) context.getBean("getXiaoMi");
          //3.调用方法
          System.out.println(xiaoMi.getName());
      }
  }
  ```

####3.3依赖注入-注解@Bean

- 基本步骤
  1. 构造注入。在持有对象引用的类中，声明带有引用参数的构造函数。在配置类中，通过构造函数注入引用。
  2. 设值注入。在持有对象引用的类中，声明带有引用参数的方法。在配置类中，通过设值方法注入引用。

```java
// JavaBean
public class Jack {

    private XiaoMi xiaoMi;

    public Jack() {
        super();
    }

    // 构造注入
    public Jack(XiaoMi xiaoMi) {
        this.xiaoMi = xiaoMi;
    }

    // 设值注入
    public void setXiaomi(XiaoMi xiaomi) {
        this.xiaoMi = xiaomi;
    }

    public void have() {
        System.out.println("Jack有一个手机."+xiaoMi.getName());
    }
}

// 配置类
@Configuration
public class JavaBeanConfig {

    @Bean(name = "xiaoMi")
    public XiaoMi getXiaoMi() {
        return new XiaoMi();
    }

    // 构造注入的方式
    @Bean(name = "jackC")
    public Jack getJackContruct() {
        return new Jack(getXiaoMi());
    }

    // 设值注入的方式
    @Bean(name = "jackM")
    public Jack getJackMethod() {
        Jack jack = new Jack();
        jack.setXiaomi(getXiaoMi());
        return jack;
    }
}
```

###4.通过XML装配bean(简略)
####4.1创建XML配置文件并声明Bean

```java
// Car类
public class Car {
    public String getName() {
        return new String("车的品牌是大众.");
    }
}
// Driver类
public class Driver {
    private Car car;

    public void trip() {
        System.out.println("司机开车旅游去."+car.getName());
    }
}

// 在xml文件中注册bean
<bean id="dirver" class="com.joker.springBasic.springIOC.xmlIOC.Driver" />
<bean id="car" class="com.joker.springBasic.springIOC.xmlIOC.Car" />
    
// 初始化上下文并启动ClassPathXmlApplicationContext    
public class RootIOC {
    public static void main(String[] args){
        //1.加载配置文件，启动Spring容器
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:ApplicationContext.xml");
        //2.使用Spring容器实例化bean对象
        Car car = (Car) context.getBean("car");
        //3.调用方法
        System.out.println(car.getName());
    }
}    

```

####4.2依赖注入

```java
// JavaBean
public class Driver {
    private Car car;
    public Driver(){}

    // 构造注入
    public Driver(Car car) {
        this.car = car;
    }

    // 设值注入
    public void setCar(Car car) {
        this.car = car;
    }

    public void trip() {
        System.out.println("司机开车旅游去."+car.getName());
    }
}

// xml配置文件
// 构造注入
<bean id="dirver" class="com.joker.springBasic.springIOC.xmlIOC.Driver" >
    <constructor-arg ref="car"/>
</bean>
    
// 设值注入
<bean id="dirver" class="com.joker.springBasic.springIOC.xmlIOC.Driver" >
	<property name="car" ref="car"/>
</bean>    
```

###5.导入和混合配置

- 三种配置方式的结合
  1. 可以将JavaConfig的组件扫描和自动装配和/或XML配置混合在一起
  2. 自动装配的时候会考虑到Spring容器中所有的bean，不管是在JavaConfig或XML中声明的还是通过组件扫描获取到的。

####5.1在JavaConfig中引用XML配置

- 在配置类中引入其他非XML注册的Bean-注解@Import

  ```java
  @Configuration
  @Import(XiaoMi.class)
  public class JavaBeanConfig {
  
      // 构造注入的方式
      @Bean(name = "jackC")
      public Jack getJackContruct(XiaoMi xiaoMi) {
          return new Jack(xiaoMi);
      }
  
      // 设值注入的方式
      @Bean(name = "jackM")
      public Jack getJackMethod(XiaoMi xiaoMi) {
          Jack jack = new Jack();
          jack.setXiaomi(xiaoMi);
          return jack;
      }
  }
  ```

- 在配置类中引入XML配置文件-注解@ImportResource

  ```java
  @Configuration
  @ImportResource("classpath:ApplicationContext.xml")
  public class JavaBeanConfig {
  
      // 构造注入的方式
      @Bean(name = "jackC")
      public Jack getJackContruct(XiaoMi xiaoMi) {
          return new Jack(xiaoMi);
      }
  
      // 设值注入的方式
      @Bean(name = "jackM")
      public Jack getJackMethod(XiaoMi xiaoMi) {
          Jack jack = new Jack();
          jack.setXiaomi(xiaoMi);
          return jack;
      }
  }
  ```

####5.2在XML配置中引用JavaConfig-省略不重要

## 装配的高级特性

###1.环境与profile

####1.1配置profile bean-注解@Profile

- 应用范围

  1. @Profile可以应用在类上，也可以应用到方法上。
  2. 注册三个相同ID的Bean，通过@Profile可以控制具体实例化哪个Bean

  ```java
  // 配置文件
  @Configuration
  @ComponentScan
  public class profileConfig {
  
      @Bean(name = "phone")
      @Profile("dev")
      public Phone xiaomi() {
          Phone phone = new Phone();
          phone.setModel("XiaoMi");
          return phone;
      }
  
      @Bean(name = "phone")
      @Profile("sit")
      public Phone oppo() {
          Phone phone = new Phone();
          phone.setModel("Oppo");
          return phone;
      }
  
      @Bean(name = "phone")
      @Profile("prod")
      public Phone huawei() {
          Phone phone = new Phone();
          phone.setModel("Huawei");
          return phone;
      }
  
  }
  
  // 实例化的类
  public class Phone {
      private String model;
  
      public void setModel(String model) {
          this.model = model;
      }
  
      public String getModel() {
          return model;
      }
  }
  ```

####1.2激活profile

- 相关属性

  1. Spring在确定哪个profile处于激活状态时，需要依赖两个独立的属性：spring.profiles.active和spring.profiles.default
  2. 如果设置了spring.profiles.active属性的话，那么它的值就会用来确定哪个profile是激活的。但如果没有设置spring.profiles.active属性的话，那Spring将会查找spring.profiles.default的值。

- 设置属性的方式

  1. 作为DispatcherServlet的初始化参数.
  2. 作为Web应用的上下文参数.
  3. 作为环境变量.
  4. 作为JVM的系统属性.
  5. 在集成测试类上，使用@ActiveProfiles注解设置.

- 在web.xml中配置DispatcherServlet的初始化参数

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/14_webxml配置参数.png)

  

###2.条件化的bean-@Conditional

- 定义及作用

  1. 在配置文件中，@Conditional注解可以用到带有@Bean注解的方法上。
  2. 如果给定的条件计算结果为true，就会创建这个bean，否则的话，这个bean会被忽略。

- 基本使用

  1. @Conditional的参数为一个实现了Condition接口的类型，需要实现方法matches()。

  ```java
  /**
  * 参数ConditionContext的作用
  * 1.借助getRegistry()返回的BeanDefinitionRegistry检查bean定义。
  * 2.借助getBeanFactory()返回的ConfigurableListableBeanFactory检查bean是否存在，甚至探查bean的属性。
  * 3.借助getEnvironment()返回的Environment检查环境变量是否存在以及它的值是什么。
  * 4.读取并探查getResourceLoader()返回的ResourceLoader所加载的资源。
  * 5.借助getClassLoader()返回的ClassLoader加载并检查类是否存在。
  *
  * 参数AnnotatedTypeMetadata作用
  * 1.检查带有@Bean注解的方法上还有什么其他的注解
  **/
  public class ProfileCondition implements Condition {
      public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
          return false;
      }
  }
  
  
  // 配置类-根据ProfileCondition类进行条件判断,有选择的实例化对象
  @Configuration
  public class ConditionalConfig {
  
      @Bean(name = "phone")
      @Conditional(ProfileCondition.class)
      public Phone xiaomi() {
          Phone phone = new Phone();
          phone.setModel("XiaoMi");
          return phone;
      }
  
      @Bean(name = "phone")
      @Conditional(ProfileCondition.class)
      public Phone oppo() {
          Phone phone = new Phone();
          phone.setModel("Oppo");
          return phone;
      }
  }
  ```

###3.bean的作用域

- 作用域分类

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/15_Bean作用域.png)

- 设置作用域
  1. 在默认情况下，Spring应用上下文中所有bean都是作为以单例（singleton）的形式创建的。
  2. 如果选择其他的作用域，要使用@Scope注解，它可以与@Component或@Bean一起使用。
  3. 如果你使用XML来配置bean的话，可以使用<bean>元素的scope属性来设置作用域。

####3.1使用会话和请求作用域-scope属性proxyMode

```java
@Configuration
public class ScopeConfig {
    // 设置作用域的值和代理模式
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION,
    proxyMode = ScopedProxyMode.INTERFACES)
    public Chair chair() {
        return new Chair();
    }
}
```

- 注解@Scope的属性proxyMode

  1. 应用背景

     假设A的作用域是会话域，B的作用域是单例域。Spring在把A注入到B中的时候，并不会真正注入一个实例，因为A对于每个用户都有一个实例。而是将A的代理注入到B中，等到真正调用的时候再将真正的实例注入进去。

     如果A是接口，则采用基于接口的代理，proxyMode为ScopedProxyMode.INTERFACES。如果A是类，则采用基于类的CGLib代理，proxyMode为ScopedProxyMode.TARGET_CLASS。

  2. 作用

     这个属性解决了将会话或请求作用域的bean注入到单例bean中所遇到的问题。

####3.2在XML中声明作用域代理-不重要省略

###4.运行时值注入

- 应用背景

  对于类的属性值，采用运行时动态生成的方式，称为运行时值注入。

- 运行时求值的两种方式

  1. 属性占位符
  2. Spring表达式语言（SpEL）

####4.1注入外部的值

- 利用Environment和@PropertySource

  ```java
  // @PropertySource指定引入的属性配置文件
  @Configuration
  @PropertySource("classpath:envattr.properties")
  public class JavaBeanConfig {
  
      @Autowired
      private Environment environment;
  
      @Bean(name = "hero")
      public Hero hero() {
          String name = environment.getProperty("name");
  
          return new Hero(name);
      }
  }
  
  // 初始化上下文，实例化Bean
  public class RootIOC {
      public static void main(String[] args){
          //1.加载配置文件，启动Spring容器
          ApplicationContext context =
                  new AnnotationConfigApplicationContext(JavaBeanConfig.class);
          //2.使用Spring容器实例化bean对象
          Hero hero = (Hero) context.getBean("hero");
          //3.调用方法
          hero.getName();
      }
  }
  ```

  

- 属性占位符

  1. 基本格式

     在Spring装配中，占位符的形式为使用“${ ... }”包装的属性名称。

  2. 使用条件

     - 使用占位符，需要配置一个PropertySourcesPlaceholderConfigurer实例，能够基于Spring Environment及其属性源来解析占位符。

     - 依旧要通过注解@PropertySource引入配置文件。

     - 使用的时候，通过@Value解析占位符的值。
     - 解析外部属性能够将值的处理推迟到运行时，关注点在于根据名称解析来自于Spring Environment和属性源的属性。

  ```java
  // 配置类
  @Configuration
  @ComponentScan
  @PropertySource("classpath:envattr.properties")
  public class JavaBeanConfig {
      @Bean
      public PropertySourcesPlaceholderConfigurer getProperty() {
          return new PropertySourcesPlaceholderConfigurer();
      }
  }
  
  // Bean利用@Value解析占位符
  @Component
  public class Hero {
  
      private String name;
  
      public Hero(@Value("${name}") String name) {
          this.name = name;
      }
  
      public void getName() {
          System.out.println("my name is "+name);
      }
  }
  
  // 初始化上下文
  public class RootIOC {
      public static void main(String[] args){
          //1.加载配置文件，启动Spring容器
          ApplicationContext context =
                  new AnnotationConfigApplicationContext(JavaBeanConfig.class);
          //2.使用Spring容器实例化bean对象
          Hero hero = (Hero) context.getBean("hero");
          //3.调用方法
          hero.getName();
      }
  }
  ```

  

####4.2使用Spring表达式语言进行装配

- 基本格式及应用条件
  1. SpEL表达式要放到“#{ ... }”之中。
  2. 通过@Value注解应用表达式，将表达式的值注入属性或者构造器参数中。
- 基本使用
  1. 表示常量值
  2. 引用bean、属性和方法
  3. 在表达式中使用类型
  4. 对值进行算术、关系和逻辑运算
  5. 正则表达式匹配