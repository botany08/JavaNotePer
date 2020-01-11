## AOP实现机制

### 1.SpringAOP的发展概述

####1.1 静态AOP

- 实现方式
  1. 静态AOP，最初实现为AspectJ。
  2. 在Java代码编译期，相应的横切关注点以Aspect形式实现之后，会通过特定的编译器，将实现后的Aspect编译并织入到系统的静态类中。
- 优点和缺点
  1. 优点是，Aspect直接以Java字节码的形式编译到Java类中，Java虚拟机可以像通常一样加载Java类运行，不会对系统的运行造成负担。
  2. 缺点是，灵活性不够。如果横切关注点需要改变织入到系统的位置，就需要重新修改Aspect定义文件，然后使用编译器重新编译Aspect并重新织入到系统中。

#### 1.2动态AOP

- 实现方式

  1. 在运行期，通过Java语言提供的各种动态特性，来实现Aspect织入到当前系统的过程。
  2. AOP的织入过程在系统运行开始之后进行，而不是预先编译到系统类中，而且织入信息大都采用外部XML文件格式保存。
  3. 一般分为JDK动态代理实现和CGLib动态代理实现。SpringAOP默认情况下采用JDK动态代理实现。

- 优点和缺点

  1. 优点是，灵活性和易用性比较好。
  2. 缺点是，性能问题。动态AOP的实现产品大都在类加载或者系统运行期间，采用对系统字节码进行操作的方式来完成Aspec侄I」系统的织入，难免会造成一定的运行时性能损失。

- 动态代理

  1. 实现原理

     动态代理(Dynamic Proxy)机制，可以在运行期间，为相应的接口(Interface)动态生成对应的代理对象。

  2. 特点

     需要织入横切关注点逻辑的模块类都得实现相应的接口，因为动态代理机制只针对接口有效。

- 动态字节码增强

  1. 实现原理

     通过动态字节码增强技术, 为这些系统模块类生成相应的子类，而将横切逻辑加到这些子类中。让应用程序在执行期间使用的是这些动态生成的子类，从而达到将横切逻辑织入系统的目的。

  2. 特点

     如果需要扩展的类以及类中的实例方法等声明为final的话，则无法对其进行子类化的扩展。

     SpringAOP在无法采用动态代理机制进行AOP功能扩展的时候，会使用CGLIB库的动态字节码增强支持来实现AOP的功能扩展。

### 2.Joinpoint--连接点

- 在Spring AOP中，仅支持方法级别的Joinpoint。更确切地说，只支持方法执行(Method Execution)类型的Joinpoint。

###3.Pointcut--切点

#### 3.1Pointcut接口

- 基本定义

  1. Spring中以接口定义org. springframework. aop.Pointcut 作为其AOP框架中所有Pointcut的最顶层抽象。

  ```java
  /**
  * Pointcut接口定义
  * 1.将类型匹配和方法匹配分开定义,为了重用不同级别的匹配定义,或者强制让某个子类重写相应的方法定义.
  **/
  public interface Pointcut {
      // ClassFilter用于匹配,将被执行织入操作的对象
  	ClassFilter getClassFilter();
      
      // MethodMatcher用于匹配,将被执行织入操作的方法
  	MethodMatcher getMethodMatcher();
      
      // 如果Pointcut类型为TruePointcut,默认会对系统中的所有对象,以及对象上所有被支持的Joinpoint进行匹配.
  	Pointcut TRUE = TruePointcut.INSTANCE;
  }
  
  /**
  * ClassFilter接口定义
  * 1.作用是,对Joinpoint所处的对象进行Class级别的类型匹配.
  **/
  public interface ClassFilter {
      // 当织入的目标对象的Class类型与Pointcut所规定的类型相符时，matches方法将会返回true.
      // 否则返回false,即意味着不会对这个类型的目标对象进行织入操作.
  	boolean matches(Class<?> clazz);
  	
      // 当Pointcut中返回的ClassFilter为该类型实例时，将会对系统中所有的目标类以及实例进行匹配.
  	ClassFilter TRUE = TrueClassFilter.INSTANCE;
  }
  
  /**
  * MethodMatcher接口定义
  * 1.对Joinpoint所处的对象进行Method级别的方法匹配.
  * 2.在对对象具体方法进行拦截的时候，可以忽略每次方法执行的时候调用者传入的参数, 也可以每次都检查这些方法调用
  *   参数, 以强化拦截条件.
  **/
  public interface MethodMatcher {
      // 忽略每次方法执行的传入参数,暂时称为方法A
  	boolean matches(Method method, Class<?> targetClass);
  	
      /**
      * 1.当返回false时,表示不会考虑具体Joinpoint的方法参数,称为StaticMethodMatcher,只会执行方法A.
      * 2.当返回true时,表示每次都对方法调用的参数进行匹配检查,称为DynamicMethodMatche,会先执行方法A,再执行
      *   方法B
      **/
  	boolean isRuntime();
  	
      // 每次都检查方法执行的传入参数,暂时称为方法B
  	boolean matches(Method method, Class<?> targetClass, Object... args);
  	
      // 当Pointcut中返回的MethodMatcher为该类型实例时，将会对目标对象中的所有方法进行匹配.
  	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;
  }
  ```

- Poincut的分类

  - 在MethodMatcher类型的基础上，Pointcut可以分为两类，即StaticMethodMatcherPointcut 和 DynamicMethodMatcherPointcut。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/5_Pointcut接口的分类.png)

#### 3.2常用的Pointcut实现类

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/6_Poinct接口实现类.png"  />

- NameMatchMethodPointcut

  1. 定义

     最简单的Pointcut实现，属于StaticMethodMatcherPointcut的子类。

  2. 作用

     可以根据自身指定的一组方法名称与Joinpoint处的方法的方法名称进行匹配。

     无法对重载(Overload)的方法名进行匹配，因为仅对方法名进行匹配，不会考虑参数相关信息。

  ```java
  /**
  * NameMatchMethodPointcut设置匹配方法名
  **/
  public class pointcutAopDemo {
      public static void main(String[] args){
          NameMatchMethodPointcut nmmp = new NameMatchMethodPointcut();
          // 匹配一个方法名
          nmmp.setMappedName("matches");
          // 匹配多个方法名
          nmmp.setMappedNames(new String[]{"matches","isRuntime"});
          // 只能使用通配符*进行匹配,不能用正则表达式
          nmmp.setMappedName("mat*es");   
      }
  }
  ```

- JdkRegexpMethodPointcut

  1. 定义

     属于StaticMethodMatcherPointcut的子类，继承于抽象类AbstractRegexpMethodPointcut。

  2. 作用

     AbstractRegexpMethodPointcut声明了pattern和patterns属性，可以指定一个或者多个正则表达式的匹配模式。
     
     JdkRegexpMethodPointcut适用于JDK标准正则表达式。

  ```java
  /**
  * JdkRegexpMethodPointcut设置匹配方法名
  * 1.使用正则表达式来匹配相应的Joinpoint所处的方法时，正则表达式的匹配模式必须以匹配整个方法签名的形式指定,
  *   就是方法名要带有包名+类名
  **/
  public class pointcutAopDemo {
      public static void main(String[] args){
          JdkRegexpMethodPointcut jrmp = new JdkRegexpMethodPointcut();
          // 设置一个方法匹配
          jrmp.setPattern("com.joker.springBasic.springAOP.stateName");
          // 设置多个方法匹配
          jrmp.setPatterns(new String[]{"*.stateName","*.countPlay"});
      }
  }
  ```

  

- AnnotationMatchingPointcut

  1. 定义

     直接实现Poincut接口

  2. 作用

     根据目标对象中是否存在指定类型的注解来匹配Joinpoint。

     注解可以是类注解，也可以是方法注解。可以同时匹配一个类注解和一个方法注解，来确定一个方法。

     ```java
     public class pointcutAopDemo {
         public static void main(String[] args){
             // 类注解@Controller,匹配所有带此注解的类
             AnnotationMatchingPointcut ampa = new AnnotationMatchingPointcut(Controller.class);
             // 方法注解@Autowired,匹配所有带此注解的方法
             AnnotationMatchingPointcut ampb = new AnnotationMatchingPointcut(Autowired.class);
             // 类注解@Controller和方法注解方法注解@Autowired,匹配带这两个注解的所有的类中方法
             AnnotationMatchingPointcut ampc = 
                 new AnnotationMatchingPointcut(Controller.class,Autowired.class);
         }
     }
     ```

     

- ComposablePointcut

  1. 定义

     ComposablePointcut是Spring AOP提供的可以进行Pointcut逻辑运算的Pointcut实现，可以进行Pointcut之间的“并”以及“交”运算。

     ```java
     // 初始化两个pointcut对象
     ComposablePointcut pointcutl = new ComposablePointcut (classFilterl,methodMatcherl)；
     ComposablePointcut pointcut2 = ...;
     
     // 进行逻辑运算,可以得出新的pointcut对象
     ComposablePointcut unitedPoincut = pointcutl.union(pointcut2);
     ComposablePointcut intersectionPointcut = pointcutl.intersection(unitedPoincut);
     ```

  2. Pointcuts工具类

     只能进行Pointcut与Pointcut之间的逻辑组合运算

     ```java
     // 初始化两个pointcut对象
     Pointcut pointcutl = ...;
     Pointcut pointcut2 = ...;
     
     // 进行逻辑运算,可以得出新的pointcut对象
     Pointcut unitedPoincut = Pointcuts.union(pointcutl,pointcut2);
     Pointcut intersectionPointcut = Pointcuts.intersection(pointcutl,pointcut2);
     ```

     

- ControlFlowPointcut

  因为ControlFlowPointcut类型的Pointcut需要在运行期间检查程序的调用栈，而且每次方法调用都需要检查，所以性能比较差，基本上不使用这个Pointcut实现。

#### 3.3扩展的Poincut

- SpringAOP已经提供了相应的扩展抽象类支持，只需要继承相应的抽象父类，然后实现或者覆写相应方法逻辑即可。

###4.Advice--通知

#### 4.1Advice接口

- 作用

  1. Advice实现了将被织入到Pointcut规定的Joinpoint处的横切逻辑。

- 分类

  1. Advice按照其自身实例(instance)能否在目标对象类的所有实例中共享这一标准，即per-class类型的Advice和per-instance类型的Advice。

  2. per-class类型的Advice接口层次图

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_Advice接口层次图.png)

#### 4.2per-class类型的Advice

- 定义

  1. per-class类型的Advice是指，该类型的Advice的实例可以在目标对象类的所有实例之间共享。
  2. 通常只是提供方法拦截的功能，不会为目标对象类保存任何状态或者添加新的特性。

- BeforeAdvice

  1. BeforeAdvice所实现的横切逻辑将在相应的Joinpoint之前执行，在BeforeAdvice执行完成之后，程序执行流程将从Joinpoint处继续执行。
  2. 在Spring 中实现BeforeAdvice , 我们通常只需要实现org.spring framework.aop.MethodBeforeAdvice接口即可。
  3. 使用BeforeAdvice进行整个系统的某些资源初始化或者其他一些准备性的工作。

  ```java
  // BeforeAdvice接口本身没有定义方法,可以新建接口继承,扩展BeforeAdvice接口.
  public interface MethodBeforeAdvice extends BeforeAdvice {
  	void before(Method method, Object[] args, Object target) throws Throwable;
  }
  ```

- ThrowsAdvice

  1. ThrowsAdvice通常用于对系统中特定的异常情况进行监控，以统一的方式对所发生的异常进行处理。
  2. Spring中以接口定义org.springframework.aop ThrowsAdvice对应通常AOP概念中的AfterThrowingAdvice。
  3. 可以根据将要拦截的Throwable的不同类型，在同一个ThrowsAdvice中实现多个afterThrowing方法。

  ```java
  public class ExceptionBarrierThrowsAdvice implements ThrowsAdvice {
      public void afterThrowing(Throwable t) {
      	//普通异常处理逻辑
      }
      
      public void afterThrowing(RuntimeException e) {
      	//运行时异常处理逻辑
      }
      
      public void afterThrowing(Method m,Object[] args,Object target,ApplicationException e) {
      	//处理应用程序生成的异常
      }
  }
  ```

- AfterReturningAdvice

  1. 当Joinpoint的方法正常返回时，会执行AfterReturningAdvice，一般不用来处理资源清理。可以访问当前Joinpoint的方法返回值、方法、方法参数以及所在的目标对象，但是不能修改。
  2. org. springframework. aop.AfterReturningAdvice接口定义了Spring 的AfterReturningAdvice。

  ```java
  /**
  * AfterAdvice接口定义,一般不定义方法以便扩展
  **/
  public interface AfterAdvice extends Advice {}
  
  // 扩展的接口
  public interface AfterReturningAdvice extends AfterAdvice {
  	void afterReturning(Object returnvalue, Method method, Object[] args, Object target);
  }
  
  // 实现接口
  public class TaskExecutionAfterReturningAdvice implements AfterReturningAdvice { 
      .....
  }    
  ```

- AroundAdvice--环绕通知

  1. AroundAdvice相当于BeforeAdvice和AfterAdvice的集合，可以用来做资源准备，也可以用来做资源的清理工作。
  2. 在SpringAop中的实现接口是直接使用AOP Alliance的标准接口，org.aopalliance.intercept. Methodinterceptor。

  ```java
  /**
  * MethodInterceptor接口定义
  **/
  public interface MethodInterceptor extends Interceptor {
  	Object invoke(MethodInvocation invocation) throws Throwable;
  }
  
  /**
  * 接口的实现
  * 调用Methodinvocation的proceed()方法,可以让程序执行继续沿着调用链传播,最后返回proceed()方法的值。
  **/
  public class AroudAdviceDemo implements MethodInterceptor {
      public Object invoke(MethodInvocation invocation) throws Throwable {
          // beforeAdvice
          System.out.println("beforeAdvice");
  
          // 跳转到joinpoint方法继续执行
          Object returnValue = invocation.proceed();
          
          // afterAdvice
          System.out.println("afterAdvice");
          
          return returnValue;
      }
  }
  ```

  

#### 4.3per-instance类型的Advice--不重要

- 基本定义
  1. per-instance类型的Advice不会在目标类所有对象实例之间共享，而是会为不同的实例对象保存它们各自的状态以及相关逻辑。
  2. 在SpringAOP中，Introduction就是唯一的一种per-instance型Advice。
- Introduction接口
  1. Introduction可以在不改动目标类定义的情况下，为目标类添加新的属性以及行为。
  2. 在Spring中，为目标对象添加新的属性和行为必须声明相应的接口以及相应的实现。再通过特定的拦截器将新的接口定义以及实现类中的逻辑附加到目标对象之上。最后目标对象（确切地说是目标对象的代理对象）就拥有了新的状态和行为。
  3. 有两个默认实现Delegatinglntroductionlnterceptor和DelegatePerTargetObjectlntroductionlnterceptor。

###5.Aspect--切面

#### 5.1基本定义

- Spring中的Aspect定义

  1. Spring中最初没有完全明确的Aspect的概念。
  2. Advisor代表Spring中的Aspect，与正常的Aspect不同，Advisor通常只持有一个Pointcut和一个Advice。
  3. 理论上，Aspect定义中可以有多个Pointcut和多个Advice，可以认为Advisor是一种特殊的Aspect。

- 结构体系

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_Advisor结构体系.png)

  1. org.springframework.aop.PointcutAdvisor：可以使用任何类型的Pointcut，使用任何类型的Advice。
  2. org.springframework.aop.introductionAdvisor：只能用于类级别的拦截，只能使用Introduction型的Advice。

#### 5.2PointcutAdvisor接口

- 结构体系

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_PointcutAdvisor结构.png)

  1. PointcutAdvisor接口可以定义一个任何类型的Pointcut和一个任何类型的Advice。
  2. 默认实现类有DefaultPointcutAdvisor、NameMatchMethodPointcutAdvisor和RegexpMethodPointcutAdvisor。

- DefaultPointcutAdvisor

  1. 定义

     最通用的PointcutAdvisor实现，除了不能为其指定Introduction类型的Advice之外，任何类型的Pointcut和任何类型的Advice都可以使用。

     ```java
     public class DefaultPointcutAdvisor extends AbstractGenericPointcutAdvisor 
         implements Serializable {
     	// 持有一个Pointcut实例
     	private Pointcut pointcut = Pointcut.TRUE;
     	
         // 构造方法一
     	public DefaultPointcutAdvisor() {
     	}
     	
         // 构造方法二：指定一个Advice
     	public DefaultPointcutAdvisor(Advice advice) {
     		this(Pointcut.TRUE, advice);
     	}
         
     	// 构造方法三：指定一个Pointcut和一个Advice
     	public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
     		this.pointcut = pointcut;
     		setAdvice(advice);
     	}
     
     
     	public void setPointcut(Pointcut pointcut) {
     		this.pointcut = (pointcut != null ? pointcut : Pointcut.TRUE);
     	}
     
     	@Override
     	public Pointcut getPointcut() {
     		return this.pointcut;
     	}
     
     	@Override
     	public String toString() {
     		return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice [" 
                 + getAdvice() + "]";
     	}
     }
     ```

- NameMatchMethodPointcutAdvisor

  1. 定义

     相当于细化的DefaultPointcutAdvisor，限定了自身可以使用的Pointcut类型为NameMatchMethodPointcut,并且外部不可更改。

     ```java
     public class NameMatchMethodPointcutAdvisor extends AbstractGenericPointcutAdvisor {
     	
         // 限定了Pointcut类型为NameMatchMethodPointcut
     	private final NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
     	
         // 构造方法一
     	public NameMatchMethodPointcutAdvisor() {
     	}
     	// 构造方法二：指定一个Advice
     	public NameMatchMethodPointcutAdvisor(Advice advice) {
     		setAdvice(advice);
     	}
     	// Pointcut设置类匹配
     	public void setClassFilter(ClassFilter classFilter) {
     		this.pointcut.setClassFilter(classFilter);
     	}
     	// 设置匹配方法
     	public void setMappedName(String mappedName) {
     		this.pointcut.setMappedName(mappedName);
     	}
     	// 设置匹配方法
     	public void setMappedNames(String... mappedNames) {
     		this.pointcut.setMappedNames(mappedNames);
     	}
     	// 添加匹配方法
     	public NameMatchMethodPointcut addMethodName(String name) {
     		return this.pointcut.addMethodName(name);
     	}
     
     	@Override
     	public Pointcut getPointcut() {
     		return this.pointcut;
     	}
     
     }
     ```

     

- RegexpMethodPointcutAdvisor

  1. 定义

     RegexpMethodPointcutAdvisor限定了Pointcut的类型，默认为JdkRegexpMethodPointcut。

     ```java
     public class RegexpMethodPointcutAdvisor extends AbstractGenericPointcutAdvisor {
     
     	private String[] patterns;
     
     	private AbstractRegexpMethodPointcut pointcut;
     
     	private final Object pointcutMonitor = new SerializableMonitor();
     
     	// 构造方法一
     	public RegexpMethodPointcutAdvisor() {
     	}
     	// 构造方法二：指定一个Advice
     	public RegexpMethodPointcutAdvisor(Advice advice) {
     		setAdvice(advice);
     	}
     	// 构造方法三：指定一个方法匹配pattern和一个Advice
     	public RegexpMethodPointcutAdvisor(String pattern, Advice advice) {
     		setPattern(pattern);
     		setAdvice(advice);
     	}
     
     	// 构造方法四：指定一个方法匹配pattern数组和一个Advice
     	public RegexpMethodPointcutAdvisor(String[] patterns, Advice advice) {
     		setPatterns(patterns);
     		setAdvice(advice);
     	}
     
     
     	public void setPattern(String pattern) {
     		setPatterns(pattern);
     	}
     
     	public void setPatterns(String... patterns) {
     		this.patterns = patterns;
     	}
     
     	
         // 设置默认的Pointcut为JdkRegexpMethodPointcut
     	@Override
     	public Pointcut getPointcut() {
     		synchronized (this.pointcutMonitor) {
     			if (this.pointcut == null) {
     				this.pointcut = createPointcut();
     				this.pointcut.setPatterns(this.patterns);
     			}
     			return pointcut;
     		}
     	}
     
     	protected AbstractRegexpMethodPointcut createPointcut() {
     		return new JdkRegexpMethodPointcut();
     	}
     
     	@Override
     	public String toString() {
     		return getClass().getName() + ": advice [" + getAdvice() +
     				"], pointcut patterns " + ObjectUtils.nullSafeToString(this.patterns);
     	}
     
     
     	private static class SerializableMonitor implements Serializable {
     	}
     
     }
     ```

####5.3introductionAdvisor接口--不重要

- 工ntroductionAdvisor纯粹就是为Introduction 而生的，比较少使用

#### 5.4Ordered的作用

- 应用背景
  1. 当其中的某些Advisor的Pointcut匹配了同一个Joinpoint的时候，就会在这同一个Joinpoint处执行多个Advice的横切逻辑。
  2. 如果这些Advisor所关联的Advice之间没有很强的优先级依赖关系，那么谁先执行，谁后执行都不会造成任何影响。如果Advice逻辑存在优先顺序依赖的话，就需要进行设置。
  3. Spring在处理同一Joinpoint处的多个Advisor的时候，实际上会按照指定的顺序和优先级来执行，顺序号决定优先级，顺序号越小，优先级越高，优先级排在前面的，将被优先执行。
- 指定Advisor顺序
  1. 在Spring框架中，可以通过让相应的Advisor以及其他顺序紧要的bean实现org.springframework.core.Ordered接口来明确指定相应顺序号。

###6.Spring AOP的织入

#### 6.1织入接口ProxyFactory

- 基本定义

  1. 在Spring AOP 中,使用类org.springframework.aop.framework.ProxyFactory作为织入器。
  2. ProxyFactory并非SpringAOP中唯一可用的织入器，而是最基本的一个织入器实现。
  3. Spring AOP是基于代理模式的AOP实现，织入过程完成后，会返回织入了横切逻辑的目标对象的代理对象。

- 织入的基本逻辑

  1. 指定要对其进行织入的目标对象。

     可以通过ProxyFactory的构造方法直接传入，也可以在ProxyFactory实例化后，通过相应的setter方法进行设置。

  2. 指定将要应用到目标对象的Aspect，Spring中为Advisor接口。

     可以直接指定Advice，对于Introduction之外的Advice类型，ProxyFactory内部就会为这些Advice构造相应的Advisor。

  3. SpringAOP在使用代理模式实现AOP的过程中采用了动态代理和CGLIB两种机制，在使用ProxyFactory对目标类进行代理的时候，会通过ProxyFactory的某些行为控制属性对这两种情况进行区分。

- 动态代理实例--基于接口的代理

  如果目标类实现了至少一个接口，不管有没有通过ProxyFactory的setlnterfaces ()方法明确指定要对特定的接口类型进行代理，只要不将ProxyFactory的optimize和proxyTargetclass两个属性的值设置为true，ProxyFactory都会按照面向接口进行代理。

  ```java
  // 目标接口
  public interface Task {
      void executeTask();
  }
  
  // 目标类
  public class MockTask implements Task {
      public void executeTask() {
          System.out.println("Task Execute!");
      }
  }
  
  // Advice类
  public class AroudAdviceDemo implements MethodInterceptor {
      public Object invoke(MethodInvocation invocation) throws Throwable {
  
          System.out.println("环绕通知----开始");
          Object returnObj = invocation.proceed();
          System.out.println("环绕通知----结束");
  
          return returnObj;
      }
  }
  
  /**
  * JDK动态代理实现AOP实例
  **/
  public class ProxyFactoryDemo {
      public static void main(String[] args){
          // 初始化目标对象
          MockTask mockTask = new MockTask();
  
          // 1.初始化织入器,构造参数为目标对象
          ProxyFactory weaver = new ProxyFactory(mockTask);
  
          // 2.织入器设置代理接口类型--可以省略
          weaver.setInterfaces(new Class[]{Task.class});
  
          // 初始化Advisor,设置匹配方法和Advice
          NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor();
          advisor.setMappedName("executeTask");
          advisor.setAdvice(new AroudAdviceDemo());
  
          // 3.织入器指定切面
          weaver.addAdvisor(advisor);
  
          // 4.生成代理对象
          Task task = (Task) weaver.getProxy();
  
          // 5.代理对象执行方法
          task.executeTask();
  
      }
  }
  ```

  

- CGLib代理实例--基于类的代理

  基于类代理的三种情况：

  1. 如果目标类没有实现任何接口，默认情况下，ProxyFactory对目标类进行基于类的代理，即使用CGLIB。

  2. 如果ProxyFactory的proxyTargetClass属性值被设置为true, ProxyFactory^7采用基于类的代理。
  3. 如果ProxyFactory的optimize属性值被设置为true, ProxyFactory会采用基于类的代理

  ```java
  public class ProxyFactoryCglibDemo {
      public static void main(String[] args){
          // 1.初始化织入器
          ProxyFactory weaver = new ProxyFactory(new DockTask());
  
          // 初始化Advisor
          NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor();
          advisor.setMappedName("executeTask");
          advisor.setAdvice(new AroudAdviceDemo());
  
          // 2.织入器指定advisor
          weaver.addAdvisor(advisor);
  
          // 3.生成代理对象
          DockTask dockTaskProxy = (DockTask) weaver.getProxy();
  
          dockTaskProxy.executeTask();
      }
  }
  ```

#### 6.2ProxyFactory的本质

##### 6.2.1AopProxy的定义

- AopProxy结构图

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/10_ProxyFactory结构图.png)

- AopProxy接口的定义

  ```java
  // AopProxy接口的作用是,获取目标对象的代理对象
  public interface AopProxy {
  	Object getProxy();
  	Object getProxy(ClassLoader classLoader);
  }
  ```

  1. AopProxy有Cglib2AopProxy和JdkDynamicAopProxy两种实现。
  2. JdkDynamicAopProxy动态代理，需要通过InvocationHandler提供调用拦截，所以同时实现了InvocationHandler接口。
  3. AopProxy的实例化过程采用工厂模式，通过接口AopProxyFactory进行生产AopProxy的实例。

- AopProxyFactory接口的定义

  ```java
  /**
  * AopProxyFactory接口定义
  **/
  public interface AopProxyFactory {
  	// 根据AdvisedSupport信息创建一个AopProxy实例
  	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;
  }
  
  /**
  * 实现类DefaultAopProxyFactory
  **/
  public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {
  
  	@Override
  	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
           // 满足三种情况,返回CglibAopProxy实例
  		if (config.isOptimize() || config.isProxyTargetClass() 
              || hasNoUserSuppliedProxyInterfaces(config)) {
  			Class<?> targetClass = config.getTargetClass();
  			if (targetClass == null) {
  				throw new AopConfigException("TargetSource cannot determine target class: " +
  						"Either an interface or a target is required for proxy creation.");
  			}
  			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
  				return new JdkDynamicAopProxy(config);
  			}
  			return new ObjenesisCglibAopProxy(config);
  		} // 否则返回JdkDynamicAopProxy实例
  		else {
  			return new JdkDynamicAopProxy(config);
  		}
  	}
  
  	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
  		Class<?>[] ifcs = config.getProxiedInterfaces();
  		return (ifcs.length == 0 || 
                  (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
  	}
  
  }
  ```

#####6.2.2AdvisedSupport类的定义

- 结构图

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/11_AdvisedSupport结构图.png)

  1. AdvisedSupport 所承载的信息可以划分为两类。
  2. ProxyConfig记载生成代理对象的控制信息，Advised记载生成代理对象所需要的必要信息，如相关目标类、Advice、Advisor等。

- ProxyConfig类

  定义了5个boolean型的属性，分别控制在生成代理对象的时候，应该采取哪些行为措施。

  1. proxyTargetClass

     如果proxyTargetClass属性设置为true,贝iJProxyFactory将会使用CGLIB对目标对象进行代理，默认值为false。

  2. optimize

     该属性的主要用于告知代理对象是否需要采取进一步的优化措施，当该属性为true时，ProxyFactory会使用CGLIB进行代理对象的生成。默认为false。

  3. opaque

     该属性用于控制生成的代理对象是否可以强制转型为Advised，可以通过Advised查询代理对象的一些状态。默认情况下，Spring AOP框架返回的代理对象都可以强制转型为Advised,以查询代理对象的相关信息。

  4. exposeProxy

     设置exposeProxy，可以让Spring AOP框架在生成代理对象时，将当前代理对象绑定到ThreadLocal。

  5. frozen

     如果将frozen设置为true,那么一旦针对代理对象生成的各项信息配置完成，则不容许更改。

- Advised接口

  1. 可以使用Advised接口访问相应代理对象所持有的Advisor，进行添加Advisor、移除Advisor等相关动作。即使代理对象已经生成完毕，也可对其进行这些操作。

##### 6.2.3ProxyFactory类

- 继承结构

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/12_ProxyFactory继承机构.png)

  1. ProxyFactory继承了AdvisedSupport类，可以设置生成代理对象所需要的相关信息。
  2. ProxyFactory继承了ProxyCreatorSupport类，此类持有一个AopProxyFactory实例，可以生成AopProxy实例。因此可以通过ProxyFactory取得AopProxy实例，再通过AopProxy.getProxy()方法，生成代理对象。

#### 6.3ProxyFactoryBean织入器类

- ProxyCreatorSupport的实现

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/13_ProxyCreatorSupport的实现.png)

  1. 使用ProxyFactor能够独立于Spring的IoC容器之外来使用Spring的AOP支持。
  2. 使用ProxyFactoryBean可以将SpringAOP与Spring的IoC容器支持相结合，通过结合Spring的IoC容器可以在容器中对Pointcut和Advice等进行管理。

- ProxyFactoryBean类的定义

  ```java
  public class ProxyFactoryBean extends ProxyCreatorSupport
  		implements FactoryBean<Object>, BeanClassLoaderAware, BeanFactoryAware {...}
  ```

  1. ProxyFactoryBean实现了FactoryBean接口，此接口在IOC容器中用来生产其他Bean的接口。ProxyFactoryBean本质上是一个用来生产Proxy的FactoryBean。
  2. 如果容器中某个对象依赖于ProxyFactoryBean，将会使用到ProxyFactoryBean的getObject()方法所返回的代理对象。只需在ProxyFactoryBean的getObjec ()方法中，通过父类的createAopProxy()取得相应的AopProxy，然后通过调用 "return AopProxy.getProxy()" 就可以返回代理对象。
  3. 通过ProxyFactoryBean可以直接通过getObject()方法返回代理对象，就可以将生产代理对象的责任交给IOC容器执行。而ProxyFacotry需要在代码中显式调用ProxyFactory.getProxy()方法才能返回代理对象。

- ProxyFactoryBean.getObject()方法

  ```java
  /**
  * getObject()方法的实现
  * 1.如果singleton为true.ProxyFactoryBean在第一次生成代理对象之后，会通过内部实例变量singletoninstance
  *   (Object类型)缓存生成的代理对象,所有的请求都会返回这一个缓存实例.
  * 2.如果singleton为false.ProxyFactoryBean^次都会重新检测各项设置,再根据最新的环境数据，返回一个新的代理对
  *   象,这种方式在生成代理对象的性能上会比较差.
  **/
  public Object getObject() throws BeansException {
      initializeAdvisorChain();
      // 返回的对象是singleton的scope
      if (isSingleton()) {
          return getSingletonInstance();
      }
      // 返回的对象是prototype的scope
      else {
          if (this.targetName == null) {
              logger.warn(
                  "Using non-singleton proxies with singleton targets is often undesirable. " +
                  "Enable prototype proxies by setting the 'targetName' property.");
          }
          return newPrototypeInstance();
      }
  }
  ```

- ProxyFactoryBean的使用

  1. 在生成目标对象的代理对象的时候，可以指定使用基于接口的代理还是基于类的代理方式。

  2. ProxyFactoryBean和ProxyFactory都继承于ProxyCreatorSupport类，都拥有父类的属性。

  3. ProxyFactoryBean拥有特别的属性如下：

     | 属性名           | 作用                                              |
     | ---------------- | ------------------------------------------------- |
     | proxyinterfaces  | 配置要代理的接口类型                              |
     | interceptorNames | 指定多个要织入目标对象的Advice、拦截器以及Advisor |
     | singleton        | 配置返回的代理对象的scope                         |

#### 6.4自动代理机制

##### 6.4.1自动代理的基本原理

- Bean的实例化
  1. Spring AOP的自动代理的实现建立在IoC容器的BeanPostProcessor概念之上。通过BeanPostProcessor，可以在遍历容器中所有bean的基础上，对遍历到的bean进行一些操作。
  2. 提供一个BeanPostProcessor，当对象实例化的时候，为其生成代理对象并返回，而不是实例化后的目标对象本身, 从而达到代理对象自动生成的目的。
  3. 前提是检查当前bean定义是否符合拦截条件。拦截条件指的是，切面上是否存在目标对象的连接点。如果存在，则该目标对象就需要生成代理对象。

##### 6.4.2自动代理实现类

- 实现类
  1. 实现类都实现了接口InstantiationAwareBeanPostProcessor，该接口继承了接口BeanPostProcessor。两个接口不一样，在IOC容器中InstantiationAwareBeanPostProcessor接口的类会直接通过，该接口的逻辑构造实例对象返回，而不会走正常的对象实例化流程。
  2. 总共有两个实现类，BeanNameAutoProxyCreator和DefaultAdvisorAutoProxyCreator。
- BeanNameAutoProxyCreator类
  1. 使用BeanNameAutoProxyCreator，可以通过指定一组容器内的目标对象对应的beanName，将指定的一组拦截器应用到这些目标对象之上。
  2. 属性beanNames，可以指定要对容器中的哪些bean自动生成代理对象。
  3. 属性interceptor­Names，可以指定将要应用到目标对象的拦截器、Advice或者Advisor等。
- DefaultAdvisorAutoProxyCreator类
  1. DefaultAdvisorAutoProxyCreator，只需要在ApplicationContext的配置文件中注册该类的bean定义。
  2. DefaultAdvisorAutoProxyCreator注册到容器后，就会自动搜寻容器内的所有Advisor，然后根据各个Advisor所提供的拦截信息，为符合条件的容器中的目标对象生成相应的代理对象。只对Advisor有效，对Pointcut和Advice无效。

#### 6.5TargetSource

##### 6.5.1基本定义

```java
/**
* TargetSource接口定义
**/
public interface TargetSource extends TargetClassAware {
	@Override
	Class<?> getTargetClass();
    
	boolean isStatic();
    // 返回目标对象
	Object getTarget() throws Exception;
	void releaseTarget(Object target) throws Exception;
}
```

- 应用背景

  1. ProxyFactoryBean和ProxyFactory在设置目标对象时，可以通过setTarget()以及setTargetResource()来指定目标对象。
  2. Targetsource接口相当于是目标对象的容器，封装了一次目标对象。
- 作用

  1. 在通常情况下，通过setTarget()或setTargetName()等方法设置目标对象，框架内部都会通过一个Targetsource实现类对这个设置的目标对象进行封装。
  2. 框架内部会以统一的方式处理调用链终点的目标对象。
- 特点

  1. Targetsource接口的特点是，每次的方法调用都会触发TargetSource的getTarget()。

  2. getTarget ()方法将从相应的Targetsource实现类中取得具体的目标对象，以便控制每次方法调用作用到的具体对象实例。
- Targetsource获取目标对象的方式
  1. 维护一个目标对象池，每次从TargetSource取得的目标对象都从这个目标对象池中取得。
  2. 一个TargetSource实现类持有多个目标对象实例，每次方法调用，返回相应的目标对象实例。
  3. 让TargetSource只持有一个目标对象实例，每次的方法调用就都会针对这一个目标对象实例。这也是ProxyFactoryBean和ProxyFactory的默认实现方式。

##### 6.5.1Targetsource实现类

- 设置方法
  1. 所有的TargetSource都可以通过ProxyFactoryBean的setTargetSource ()方法进行设置。

- SingletonTargetSource 

  1. 在通过ProxyFactoryBean的setTarget()设置完目标对象之后，ProxyFactoryBean内部会自行使用一个SingletonTargetSource对设置的目标对象进行封装。
  2. SingletonTargetSource内部只持有一个目标对象，当每次方法调用到达时，SingletonTargetSource都会返回这同一个目标对象。

- PrototypeTargetSource

  1. 每次方法调用到达调用链终点，并即将调用目标对象上的方法的时候，PrototypeTargetSource都会返回一个新的目标对象实例供调用。
  2. 目标对象要求。目标对象的bean定义声明的scope必须为prototype，通过targetBeanName属性指定目标对象的bean定义名称，而不是引用。

- HotSwappableTargetSource

  1. 可以让在应用程序运行的时候，根据某种特定条件，动态地替换目标对象类的具体实现。

  2. 使用HotSwappableTargetSource的swap方法，可以用新的目标对象实例将旧的目标对象实例替换掉。

     ```java
     public Object swap(Object newTarget);
     ```

  3. 使用HotSwappableTargetSource，需要在构造时，就提供一个默认的目标对象实例。

- CommonsPoolTargetSource

  1. 实现返回有限数目的目标对象实例。可以提供一个目标对象的对象池，然后让某个Targetsource实现每次都从这个目标对象池中去取得目标对象。内部使用现有的Jakarta Commons Pool提供对象池支持。

- ThreadLocalTargetSource

  1. 主要是为不同的线程调用提供不同的目标对象。
  2. 可以保证各自线程上对目标对象的调用，可以被分配到当前线程对应的那个目标对象实例上。



