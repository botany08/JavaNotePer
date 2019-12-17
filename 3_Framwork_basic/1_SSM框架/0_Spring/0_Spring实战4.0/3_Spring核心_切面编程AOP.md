## 切面编程AOP

###1.面向切面编程基础概念
####1.1AOP基础概念

- AOP的定义

  在运行时，动态地将一段代码(或方法)切入到目标类的指定方法、指定位置上的编程，就叫做面向切面。主要作用是把横切关注点与业务逻辑相分离。

- 术语

  1. 横切关注点(cross-cutting concern)

     在软件开发中，散布于应用中多处的功能被称为横切关注点。比如日志服务，监控服务。

  2. 切面(Aspect)

     横切关注点可以被模块化为特殊的类，这些类被称为切面。切面是通知和切点的结合。

     切面是一个工作单位，定义了在业务逻辑代码中要插入的方法，要插入的时机以及要插入的位置(切点)。

  3. 连接点(Join point)

     连接点是，在具体业务类执行过程中，切面中的通知插入进来的一个点。可以是调用方法时、抛出异常时、甚至修改一个字段时。

     在Spring中，连接点只提供了方法调用。

  4. 切点(Poincut)

     切点是多个连接点合在一起的集合。比如一个接口有多个实现类，切点可以定义为接口的某一个方法调用，连接点就是具体实现类的一个方法调用，一个切点就包含了多个连接点。

     在Spring中，切点一般使用AspectJ的切点表达式来定义切点。

  5. 通知(Advice)

     切面的工作被称为通知，通知定义了切面是什么以及何时使用。

     通知相当于，要在业务逻辑代码中插入的一个方法以及插入的时机(注解)。

  6. 织入(Weaving)

     织入是把切面应用到目标对象，并创建切面新的代理对象的过程。切面插入到业务逻辑代码中的动作，称为织入。在目标对象的生命周期中，都可以织入切面。

     - 编译期。切面在目标类编译时被织入，需要特殊的编译器，比如AspectJ的织入编译器。
     - 类加载期。切面在目标类加载到JVM时被织入。需要特殊的类加载器，可以在目标类被引入应用之前增强该目标类的字节码。
     - 运行期。切面在应用运行的某个时刻被织入。一般情况下，在织入切面时，AOP容器会为目标对象(切面)动态地创建一个代理对象。Spring AOP就是以这种方式织入切面的。

  7. 引入(Introduction)

     引入允许我们向现有的类添加新方法或属性。比如在切面中的通知方法，对目标类的状态进行更改，再将更改后的数据传入到目标类，这样目标类就拥有了新的状态。

- Spring切面中5种类型的通知

  1. 前置通知（Before）：在目标方法被调用之前调用通知功能。
  2. 后置通知（After）：在目标方法完成之后调用通知，此时不会关心方法的输出是什么。
  3. 返回通知（After-returning）：在目标方法成功执行之后调用通知。
  4. 异常通知（After-throwing）：在目标方法抛出异常后调用通知。
  5. 环绕通知（Around）：通知包裹了被通知的方法，在被通知的方法调用之前和调用之后执行自定义的行为。

####1.2Spring对AOP的支持　

- Spring提供了4种类型的AOP支持

  1. 基于代理的经典SpringAOP。

     **Spring AOP构建在动态代理基础之上**，Spring经典的AOP看起来就显得非常笨重和过于复杂，一般不使用。

  2. 纯POJO切面。

     SpringAOP的变体。术需要XML配置，是声明式地将对象转换为切面的简便方式。

  3. @AspectJ注解驱动的切面。

     SpringAOP的变体。Spring借鉴了AspectJ的切面，以提供注解驱动的AOP。本质上依然是Spring基于代理的AOP，但是编程模型几乎与编写成熟的AspectJ注解切面完全一致。

  4. 注入式AspectJ切面(适用于Spring各版本)。

     AOP需求超过了简单的方法调用(如构造器或属性拦截)，则可以使用AspectJ切面。

- SpringAOP框架的关键

  1. Spring通知是Java编写的。

     定义通知所应用的切点通常会使用注解或在Spring配置文件里采用XML来编写，现在也可以用AspectJ的切点表达式来定义切点。

  2. Spring在运行时才实例化切面对象。

     Spring会为切面创建一个代理类，此代理类会监控切面中切点匹配到的bean。当客户端调用目标bean的时候，具体的Bean才会注入代理类中，代理类进行通知方法的调用。这就是Spring应用的动态代理。

  3. Spring只支持方法级别的连接点。

     因为Spring基于动态代理，所以Spring只支持方法连接点。Spring缺少对字段连接点的支持，无法让我们创建细粒度的通知，例如拦截对象字段的修改。

###2.通过切点来选择连接点

在Spring AOP中，要使用AspectJ的切点表达式语言来定义切点。　

####2.1编写切点

- AspectJ的切点表达式语言

  | AspectJ指示器 | 描述                                                         |
  | ------------- | ------------------------------------------------------------ |
  | arg()         | 限制连接点匹配参数为指定类型的执行方法                       |
  | @args()       | 限制连接点匹配参数由指定注解标注的执行方法                   |
  | execution()   | 用于匹配是连接点的执行方法                                   |
  | this()        | 限制连接点匹配AOP代理的bean引用为指定类型的类                |
  | target        | 限制连接点匹配目标对象为指定类型的类                         |
  | @target()     | 限制连接点匹配特定的执行对象，这些对象对应的类要具有指定类型的注解 |
  | within()      | 限制连接点匹配指定的类型                                     |
  | @within()     | 限制连接点匹配指定注解所标注的类型(当使用SpringAOP时，方法定义在由指定的注解所标注的类里) |
  | @annotation   | 限定匹配带有指定注解的连接点                                 |

  1. 在Spring中尝试使用AspectJ其他指示器时，将会抛出IllegalArgument-Exception异常。
  2. 只有execution指示器是实际执行匹配的，而其他的指示器都是用来限制匹配的。

- 基本格式

  使用AspectJ切点表达式来定义一个切点。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/16_基本的切点表达式.png)

  ```java
  /**
  * 使用多个指示器,within()表示目标bean被限定在包concert中
  * 1. 可以使用 && || ！ 三个运算符，也可以说使用 and or not 三个单词来替代.
  **/
  execution(* concert.Performance.perform(..) && within(concert.*))
      
      
  /**
  * 通过beanID直接指定目标bean
  **/
  execution(* concert.Performance.perform(..) && bean('worker'))    
  ```

###3.使用注解创建切面　

- 使用AOP的基本步骤
  1. 开启AspectJ自动代理-@EnableAspectJAutoProxy
  2. 创建一个切面@AspectJ（定义切点@Pointcut，定义通知）
  3. 将切面类注册成Bean

####3.1创建普通的切面

- 定义切点

  @Pointcut注解能够在一个@AspectJ切面内定义可重用的切点。

- 定义通知

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/17_定义通知的五个注解.png)
  
- 切面实例
  
  ```java
  /**
  * 初始化上下文,调用目标Bean
  **/
  public class RootAOP {
      public static void main(String[] args){
          // 加载配置文件，启动Spring容器
          ApplicationContext context =
                  new AnnotationConfigApplicationContext(AopDemoConfig.class);
          
          // 采用基于接口的JDK动态代理
          Car car = (Car) context.getBean("motor");
          car.stateName();
          
          // 采用基于类的CGLIB代理
          Motor motor = (Motor) context.getBean("motor");
          motor
      }
  }
  
  /**
  * 配置文件
  * Spring AOP代理方式分为两种
  * 1.使用JDK动态代理，如果被代理的目标实现了至少一个接口，则会使用JDK动态代理,所有该目标类型实现的接口都将被
  *   代理。Bean进行实例化时,用接口接收实例。
  * 2.通过CGLIB来为目标对象创建代理，若该目标对象没有实现任何接口，则创建一个CGLIB代理，创建的代理类是目标类的
  *   子类。Bean进行实例化时,用具体类接收实例。
  **/
  @Configuration
  @ComponentScan
  // proxyTargetClass默认为false,JDK动态代理，设置为true则是CGLib代理。
  @EnableAspectJAutoProxy(proxyTargetClass = false)
  public class AopDemoConfig {
  }
  
  /**
  * 切面类
  **/
  @Aspect
  @Component
  public class AudienceAspect {
      @Pointcut("execution(* com.joker.springBasic.springAOP.Car.stateName(..))")
      public void stateNamePoint(){}
  
  
      @Before("stateNamePoint()")
      public void prepare() {
          System.out.println("准备启动车辆!");
      }
  
      @After("stateNamePoint()")
      public void stop() {
          System.out.println("关闭车辆!");
      }
  }
  
  // 业务逻辑Bean
  public interface Car {
      void stateName() ;
  }
  
  @Component
  public class Motor implements Car {
      public voi stateName() {
          System.out.println("这是一辆摩托车");
      }
  }
  ```

####3.2创建环绕通知

- 基本定义

  环绕通知是把目标Bean方法完全包装起来，相当于前置通知和后置通知的集合。

- 前提条件

  1. 使用@Around注解通知方法。
  2. 接受ProceedingJoinPoint作为参数，并调用ProceedingJoinPoint的proceed()方法。该方法的作用是跳转到目标Bean的方法中。
  3. 使用环绕通知的时候，通知方法会阻塞对目标Bean方法的调用，需要proceed()方法来进行跳转。

  ```java
  // 切面类
  @Aspect
  public class AudienceAspect {
  
      @Pointcut("execution(* com.joker.springBasic.springAOP.Car.stateName(..))")
      public void stateNamePoint(){}
  
      @Around("stateNamePoint()")
      public void aroudstate(ProceedingJoinPoint pjp) {
          try {
              System.out.println("准备启动车辆!");
              pjp.proceed();
              System.out.println("关闭车辆!");
          } catch (Throwable t) {
              t.printStackTrace();
          }
  
      }
  }
  ```

  

####3.3处理通知中的参数-重要但暂时不了解　

####3.4通过注解引入新功能-重要但暂时不了解　　　
###4.注入AspectJ切面-不重要省略　