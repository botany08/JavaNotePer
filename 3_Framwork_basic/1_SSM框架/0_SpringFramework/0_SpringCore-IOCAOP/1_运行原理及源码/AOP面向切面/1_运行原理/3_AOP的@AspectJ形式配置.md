##AOP的@AspectJ形式配置

### 1.切面注解@Aspect

- 利用注解@Aspect定义切面

  ```java
  // 定义切面,类注解
  @Aspect
  public class PerformanceTraceAspect {
      // 定义Pointcut,方法注解
      @Pointcut("execution(public void Foo.getName()) || execution(public void Foo.getProperty())")
      public void pointCutName() {}
  
      // 定义Advice,方法注解
      @Before("pointCutName()")
      public void beforeMethod() {
          System.out.println("方法开始准备...");
      }
  }
  ```

- 将Aspect定义织入目标对象类

  1. 编程方式织入--ProxyFactory

     ```java
     /**
     * 编程织入实现AOP
     * 1.织入器总共有三个,ProxyFactory,ProxyFactoryBean,AspectJProxyFactory
     * 2.ProxyFactoryBean作用是,生成代理对象这步不用显示调用,可以由IOC容器执行,结合在getBean()方法中.
     * 3.AspectJProxyFactory作用是,添加Advisor切面时,可以识别@AspectJ形式的切面类
     **/
     public class AspectAopDemo {
         public static void main(String[] args){
             // 1.初始化AspectJ织入器
             AspectJProxyFactory weaver = new AspectJProxyFactory();
             
             // 2.配置织入器
             // 设置基于类的代理
             weaver.setProxyTargetClass(true);
             // 指定目标对象
             weaver.setTarget(new Foo());
             // 指定切面类--相当于指定Advisor
             weaver.addAspect(PerformanceTraceAspect.class);
             
             // 3.生成代理对象
             Object proxy = weaver.getProxy();
             
             // 4.代理对象调用方法
             ((Foo)proxy).getName();
             ((Foo)proxy).getProperty();
         }
     }
     ```

     

  2. 自动代理织入--AutoProxyCreator

     ```java
     /**
     * 自动代理配置
     * 1.SpringAOP专门提供了一个自动代理类AnnotationAwareAspectJAutoProxyCreator用于@AspectJ形式.
     * 2.只需要定义切面,切点以及通知的Bean,该自动代理类就会自动织入.
     **/ 
     <bean class= 
         "org.springframework.aop.aspect.jannotation.AzmotationAwareAspectJAutoProxyCreator">
     	<property name= "proxyTargetClass" value="true"/>
     </bean>
     
     /**
     * 自动代理的使用
     * 1.直接通过getBean()方法获取代理就行,自动代理类节省了织入类的配置,FactoryBean节省了getProxy()方法.
     *   并且使用容器的getBean()代替.
     **/
     Applicationcontext ctx = new ClassPathXmlApplicationContext("...");
     Object proxy = ctx.getBean("target");
     ((Foo)proxy).method1();
     ((Foo)proxy).method2();
         
     ```

     

### 2.切点注解@Pointcut

####2.1@Pointcut基本形式

```java
@Aspect
public class PerformanceTraceAspect {
    // 定义Pointcut
    @Pointcut("execution(public void Foo.getProperty())")
    public void pointCutProperty() {}
    
    // 将PointcutSignature作为相应PointcutExpression的标志符
    @Pointcut("pointCutProperty()")
    public void pointCutName() {}
}
```

- @AspectJ形式的Pointcut声明包含两个部分

  1. PointcutExpression 表达式

     注解@Pointcut需要用在方法体上面。

     PointcutExpression是真正规定Pointcut匹配规则的地方，可以通过@Pointcut直接指定AspectJ形式的Pointcut表达式。

  2. PointcutSignature 方法体

     PointcutSignature所在的方法定义，除了返回类型必须是void之外，没有其他限制。

     可以将PointcutSignature作为相应PointcutExpression的标志符，在PointcutExpression的定义中取代重复的Pointcut表达式定义。

- PointcutExpression 表达式包含两个部分

  1. Pointcut标志符(Pointcut Designator)。 如 execution

     标志符表明该Pointcut将以什么样的行为来匹配表达式。

  2. 表达式匹配模式。如  (public void Foo.getProperty())

     在Pointcut标志符之内可以指定具体的匹配模式。

#### 2.2Pointcut表达式的标志符

- execution

  作用：匹配拥有指定方法签名的Joinpoint，接受一个方法名

  ```java
  /** 
  * 表达式
  * 1.方法的返回类型、方法名以及参数部分的匹配模式必须指定,其他可以省略
  * 2.可以在execution的表达式中使用两种通配符，即 * 和 ..
  * 3.*可以用于任何部分的匹配模式中,匹配相邻的多个字符，即一个Word
  * 4. ..通配符可以在两个位置使用,一个是包名一个是参数.
  **/
  
  // 完整的表达式：匹配Foo类的doSomething(String)方法
  execution(public void Foo.doSomething(String))
  // 简略的表达式：匹配doSomething(String)方法,不分类
  execution (void doSomething(String))
      
  // 使用 * 通配符    
  // 表示任意返回值,任意方法名,参数为String    
  execution (* *(String))
  // 表示任何返回值,任意方法名,参数只有一个但类型不限    
  execution (* *(*))    
      
  // 使用 .. 通配符   
  // 表示返回值为空,任意方法名,参数为0到多个且类型不限
  execution (void *.doSomething (..))
      
  // 只能指定到cn.spring21这一层下的所有类型 
  execution (void cn.spring21.*.doSomething (*))
  // 可以匹配cn.spring21包以及子包下的所有类型,    
  execution (void cn.spring21..*.doSomething (*))
      
      
  ```

- within

  作用：只接受类型声明，匹配指定类型下所有的方法。

  ```java
  // 匹配MockTarget类中的所有方法声明,可以使用 * 和 .. 两种通配符.
  within(cn.spring21.aop.target.MockTarget)
  ```

- this和target

  1. SpringAOP中的this和target标志符语义，this指代的是目标对象的代理对象，target指代的就是目标对象。
  2. 通常，this和target标志符都是在Pointcut表达式中与其他标志符结合使用，以进一步加强匹配的限定规则。

  ```java
  // 表示代理对象实现了接口TargetFoo
  execution(void cn.spring21.*.doSomething(*)) && this(TargetFoo)
      
  // 表示目标对象实现了接口TargetFoo
  execution(void cn.spring21.*.doSomething(*)) && target(TargetFoo)    
  ```

- args

  作用：匹配拥有指定参数类型、指定参数数量的方法级Joinpoint，不限制类型。

  1. 使用execution标志符可以直接明确指定方法参数类型。
  2. args标志符会在运行期间动态检查参数的类型。

  ```java
  // 匹配方法参数为User的方法,不限制类型
  args(cn.spring21.unveilspring.domain.User)
      
  // 与execution结合使用,匹配方法名为doSomething且方法参数为User
  execution(void cn.spring21.*.doSomething(*)) &&  args(cn.spring21.unveilspring.domain.User)  
  ```

#### 2.3@AspectJ形式的Pointcut的本质

- 基本原理

  1. @AspectJ形式声明的所有Pointcut表达式，在SpringAOP内部都会通过解析，转化为具体的Pointcut对象。
  2. @AspectJ形式声明的这些Pointcut表达式，会转化成一个专门面向AspectJ的Pointcut实现。
  3. org.springframework.aop.aspectj.AspectJExpressionPointcut代表SpringAOP中面向AspectJ的Pointcut具体实现.

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/14_AspectJ的Poincut实现.png)

- Pointcut表达式的转换流程

  1. AspectJProxyFactory手动织入器对象，或者AnnotationAwareAspectJAutoProxyCreator自动代理对象，可以通过反射获取注解@Pointcut的Pointcut定义。Spring根据Pointcut定义构造AspectJExpressionPointcut实例，在该实例中持有Pointcut表达式。
  2. 处理Pointcut匹配。AspectJExpressionPointcut实例依旧通过ClassFilter和MethodMatcher进行具体Joinpoint的匹配工作。特别的是，AspectJExpressionPointcut会委托AspectJ类库中的PointcutParser来解析Pointcut表达式。返回一个PointcutExpression对象，再由该对象和Spring框架中的类进行交互匹配。

- AspectJExpressionPointcut对象的使用

  1. 使用注解@Pointcut后，Spring框架会自动处理并生成一个AspectJExpressionPointcut实例。

  2. 代码声明使用，和普通类使用一样。

     ```java
     AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
     pointcut.setExpression("execution(* someMethodName(..))");
     ```

###3.通知Advice

#### 3.1Advice的定义

- 基本使用

  1. 使用相关的注解标注Aspect定义类中的普通方法。
  2. 注解全部都是方法级别的，注解需要带有@Pointcut标注的方法，以便确定通知的切入点。

- 通知注解

  | 注解名称        | 作用                                               |
  | --------------- | -------------------------------------------------- |
  | @Before         | 在方法之前执行                                     |
  | @After          | 在方法之后执行，不管方法是否正常返回               |
  | @AfterThrowing  | 在方法抛出异常后执行，可以指定异常类型             |
  | @AfterReturning | 在方法正常返回后执行，可以访问其返回值             |
  | @Around         | 环绕通知，在方法前后执行，相当于拦截器类型的Advice |

#### 3.2BeforeAdvice

- 基本使用

  ```java
  /**
  * BeforeAdvice实例
  * 1.value值不能为空,需要指定pointcut,可以是pointcut表达式或者pointcut方法名
  **/
  @Aspect
  public class PerformanceTraceAspect {
      // 定义Pointcut
      @Pointcut("execution(public void Foo.getProperty())")
      public void pointCutProperty() {}
  
      // 定义Advice--使用pointcut表达式
      @Before("execution(public void Foo.getProperty())")
      public void beforeMethodA() {
          System.out.println("方法开始准备...");
      }
  
      // 定义Advice--使用pointcut方法名
      @Before("pointCutProperty()")
      public void beforeMethodB() {
          System.out.println("方法开始准备...");
      }  
  }
  ```

- 在Advice中访问目标对象的方法参数

  ```java
  /**
  * 访问目标对象的方法参数
  * 1.通过org.aspectj.lang.JoinPoint对象.在定义通知方法时,第一个参数为JoinPoint.
  * 2.通过args/this/target等标志符.标志符绑定目标方法的参数名称,在通知方法中传入对应的参数名称.
  **/
  @Aspect
  public class PerformanceTraceAspect {
      // 定义Pointcut
      @Pointcut("execution(public void Foo.getProperty())")
      public void pointCutProperty() {}
  
      // 1.通过JoinPoint对象
      @Before("pointCutProperty()")
      public void beforeMethodB(JoinPoint joinpoint) {
          // getArgs()方法,访问目标方法参数值
          // getThis()方法,取得当前代理对象
          // getTarget()方法,取得当前目标对象
          // getSignature()方法,取得目标方法的方法签名
          System.out.println("方法开始准备...");
      }  
      
      // 2.通过标志符绑定,在表达式中绑定目标方法的参数名称,在通知方法中增加此参数.
      @Before("execution(public void Foo.getProperty()) && args(taskName)")
      public void beforeMethodB(String taskName) {
  
          System.out.println("方法开始准备...");
      }  
      
      // 2.通过标志符绑定,结合Pointcut
      @Pointcut("execution(public void Foo.getProperty()) && args(taskName)")
      public void pointCutProperty(String taskName) {}
      
      @Before("pointCutProperty(taskName)")
      public void beforeMethodB(String taskName) {
          System.out.println("方法开始准备...");
      }  
  }
  ```

#### 3.4AfterThrowingAdvice

- 基本使用

  ```java
  /**
  * @AfterThrowing实例
  * 1.属性pointcut,可以为pointcut表达式,或pointcut方法
  * 2.属性throwing,为advice方法定义的异常参数名称. 也可以不定义任何异常,不访问任何异常.
  * 3.当目标方法抛出异常时,执行advice方法.
  **/
  @Aspect
  public class PerformanceTraceAspect {
      // 定义Pointcut
      @Pointcut("execution(public void Foo.getProperty())")
      public void pointCutProperty() {}
  
      // 定义ThrowingAdvice
      @AfterThrowing(pointcut = "pointCutProperty()",throwing = "e")
      public void throwingAdvice(RuntimeException e) {
          System.out.println("抛出异常:"+e.getMessage());
      }
  }
  ```

#### 3.5AfterReturningAdvice

- 基本使用

  ```java
  /**
  * @AfterReturning实例
  * 1.可以访问方法正常执行后的返回值.
  * 2.属性returning绑定方法的返回值,返回值具体类型定义在advice的方法参数.
  **/
  @Aspect
  public class PerformanceTraceAspect {
      // 定义Pointcut
      @Pointcut("execution(public void Foo.getProperty())")
      public void pointCutProperty() {}
  
      // 定义AfterReturningAdvice
      @AfterReturning(pointcut = "pointCutProperty()",returning = "flag")
      public void returnAdvice(Boolean flag) {
          System.out.println("方法返回值:");
      }
  }
  ```

#### 3.6AfterAdvice

- 基本使用

  ```java
  /**
  * @After实例
  * 1.目标方法正常执行与否,该advice方法都会执行.
  **/
  @Aspect
  public class PerformanceTraceAspect {
      // 定义Pointcut
      @Pointcut("execution(public void Foo.getProperty())")
      public void pointCutProperty() {}
  
      // 定义AfterAdvice
      @Before("pointCutProperty()")
      public void afterAdvice() {
          System.out.println("方法执行完毕...");
      }
  }
  
  ```

#### 3.7AroundAdvice

- 基本使用

  ```java
  /**
  * @Around实例
  * 1.advice方法的第一个参数必须是,ProceedingJointPoint对象.
  * 2.可以修改目标方法的参数,先获取方法的参数,修改完成后再在proceed()方法中传入方法参数.
  * 3.proceed()方法前的代码,相当于BeforeAdvice.方法后的代码,相当于AfterAdvice.
  **/
  @Aspect
  public class PerformanceTraceAspect {
      // 定义Pointcut
      @Pointcut("execution(public void Foo.getProperty())")
      public void pointCutProperty() {}
  
      // 定义AroundAdvice
      @Around("execution(public void Foo.getProperty()) && args(taskName)")
      public void aroundAdvice(ProceedingJoinPoint joinPoint,String taskName) throws Throwable {
         
          // 处理目标方法参数
          String handTaskName = taskName+"已处理";
          // 将处理完成的参数,传回到目标方法.
          joinPoint.proceed(new Object[]{handTaskName});
      }
  }
  ```

#### 3.8Advice的执行顺序

- 当这些Advice都声明在同一个Aspect内的时候。
  1. 在同一个Aspect定义中，Advice的执行顺序由其在Aspect中的声明顺序决定。最先声明的拥有最高的优先级。
  2. 对于BeforeAdvice来说，拥有最高优先级的最先运行。对于AfterReturngingAdvice来说，拥有最高优先级的则是最后运行。
- 当这些Advice声明在不同的Aspect内的时候
  1. 让相应的Aspect定义实现Ordered接口，返回对应的执行顺序，较小的值拥有较高的优先级。



