## JDK动态代理

### 1.静态代理

- 代理的基本定义

  1.  代理是一种模式，提供了对目标对象的间接访问方式，即通过代理访问目标对象。 
  2.  便于在目标实现的基础上增加额外的功能操作，前拦截，后拦截等，以满足自身的业务需求。 

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_静态代理示意图.png)

- 静态代理的定义

  1. 静态代理是针对类的代理， 一个代理类只能对一个业务接口的实现类进行包装 。
  2. 代理类可以添加额外的处理逻辑，但是不能节省多个接口代理情况下的重复代码。
  
  
  
- 静态代理的实现

  1.  编写一个代理类，实现与目标对象相同的接口，并在内部维护一个目标对象的引用。 
  2.  通过构造器塞入目标对象，在代理对象中调用目标对象的同名方法，并添加前拦截，后拦截等所需的业务功能。 
  
  
  
- 静态代理实例
  
  ```java
  /**
   * Calculator接口
   */
  public interface Calculator {
  	int add(int a, int b);
  	int subtract(int a, int b);
  }
  
  /**
   * 目标对象实现类，实现Calculator接口
   */
  public class CalculatorImpl implements Calculator {
  	//加
  	public int add(int a, int b) {
  		int result = a + b;
  		return result;
  	}
  	//减
  	public int subtract(int a, int b) {
  		int result = a - b;
  		return result;
  	}
  }
  
  /**
   * 代理对象实现类，实现Calculator接口
   */
  public class CalculatorProxy implements Calculator {
      //代理对象内部维护一个目标对象引用
  	private Calculator target;
          
      //构造方法，通过目标对象创建一个代理对象
  	public CalculatorProxy(Calculator target) {
  		this.target = target;
  	}
  
      //调用目标对象的add，并在前后打印日志
  	@Override
  	public int add(int a, int b) {
  		System.out.println("add方法开始...");
  		int result = target.add(a, b);
  		System.out.println("add方法结束...");
  		return result;
  	}
  
      //调用目标对象的subtract，并在前后打印日志
  	@Override
  	public int subtract(int a, int b) {
  		System.out.println("subtract方法开始...");
  		int result = target.subtract(a, b);
  		System.out.println("subtract方法结束...");
  		return result;
  	}
  }
  
  /**
  * 使用代理对象完成数据处理,并且打印日志
  **/
  public class Test {
  	public static void main(String[] args) {
  		//把目标对象通过构造器塞入代理对象
  		Calculator calculator = new CalculatorProxy(new CalculatorImpl());
  		//代理对象调用目标对象方法完成计算，并在前后打印日志
  		calculator.add(1, 2);
  		calculator.subtract(2, 1);
  	}
  } 
  ```
  
  
  

### 2.动态代理

- 接口和具体类的区别

  1.  接口Class对象没有构造方法 ，不能直接创建对象。 实现类Class对象有构造方法 ，可以直接创建对象。

  2.  实现类Class对象，比接口Class对象， 多出了从Object继承来的方法。

     

- 静态代理和动态代理的实现流程

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/2_静态代理和动态代理的实现流程.png)

  1. 静态代理，代理类通过实现类的Class，反射生成具体实例，从而获取代理实例。
  2. 动态代理，代理类通过Proxy类，从接口Class获取代理类的Class，从而反射生成代理实例。

#### 2.1 获取接口代理对象-Proxy类

- 获取代理类的Class对象

  1. `Proxy.getProxyClass()`，用来返回代理类的`Class`对象。  只要传入目标类实现的接口的`Class`对象，`getProxyClass()`方法即可返回代理Class对象，而不用实际编写代理类。 
  2.  通过给`Proxy.getProxyClass()`传入类加载器和接口`Class`对象 ，得到一个该接口代理类的Class对象。 包含接口的方法信息 ， 又包含构造器`$Proxy0(InvocationHandler)`，还有一些特有的方法以及从`Object`继承的方法。 
  3. 代理类的Class对象只有一个有参的构造器 `com.sun.proxy.$Proxy0(java.lang.reflect.InvocationHandler)`，需要一个`InvocationHandler`实例类，构造生成代理类实例。

  ```java
  /**
  * getProxyClass()方法定义
  * 1.参数loader表示,类加载器
  * 2.参数interfaces表示,接口的Class对象
  **/
  public static Class<?> getProxyClass(ClassLoader loader,Class<?>... interfaces) {...}
  
  /**
  * 获取代理类的Class对象
  **/
  public class ProxyTest {
  	public static void main(String[] args) {
  		
  		// 参数1：Calculator的类加载器（当初把Calculator加载进内存的类加载器）
  		// 参数2：代理对象需要和目标对象实现相同接口Calculator
  		Class calculatorProxyClazz = Proxy.getProxyClass(
              Calculator.class.getClassLoader(), Calculator.class);
          
  		// 以Calculator实现类的Class对象作对比，看看代理Class是什么类型
           System.out.println(CalculatorImpl.class.getName());
  		System.out.println(calculatorProxyClazz.getName());
          
  		//打印代理Class对象的构造器
  		Constructor[] constructors = calculatorProxyClazz.getConstructors();
  		System.out.println("----构造器----");
  		printClassInfo(constructors);
          
  		//打印代理Class对象的方法
  		Method[] methods = calculatorProxyClazz.getMethods();
  		System.out.println("----方法----");
  		printClassInfo(methods);
  	}
  
  	public static void printClassInfo(Executable[] targets) {
  		for (Executable target : targets) {
  			// 构造器/方法名称
  			String name = target.getName();
  			StringBuilder sBuilder = new StringBuilder(name);
  			// 拼接左括号
  			sBuilder.append('(');
  			Class[] clazzParams = target.getParameterTypes();
  			// 拼接参数
  			for (Class clazzParam : clazzParams) {
  				sBuilder.append(clazzParam.getName()).append(',');
  			}
  			//删除最后一个参数的逗号
  			if (clazzParams != null && clazzParams.length != 0) {
  				sBuilder.deleteCharAt(sBuilder.length() - 1);
  			}
  			//拼接右括号
  			sBuilder.append(')');
  			//打印 构造器/方法
  			System.out.println(sBuilder.toString());
  		}
  	}
  }
  
  /**
  * 输出：
  * ----接口实现类名称----
  * com.joker.springBasic.springAOP.dymicproxy.CalculatorImpl
  * ----接口代理类名称----
  * com.sun.proxy.$Proxy0
  * ----构造器----
  * com.sun.proxy.$Proxy0(java.lang.reflect.InvocationHandler)
  * ----方法----
  * equals(java.lang.Object)
  * toString()
  * hashCode()
  * getName()
  **/
  ```

#### 2.2 **动态代理底层调用逻辑** 

- 基本调用逻辑

  1. 静态代理，往代理对象的构造器传入目标对象，然后代理对象调用目标对象的同名方法。 

  2. 动态代理，constructor反射创建代理对象时，需要传入InvocationHandler 实例，然后代理对象调用目标对象的同名方法。

- 动态代理调用实例

  ```java
  public class DynmicProxyDemo {
      public static void main(String[] args) throws Exception{
          // 1.获取接口代理对象的Class信息
          Class calculatorProxy = Proxy.getProxyClass(
              Calculator.class.getClassLoader(),Calculator.class);
  
          // 2.获取代理对象的构造方法
          Constructor constructor = calculatorProxy.getConstructor(InvocationHandler.class);
  
          // 3.反射创建代理实例
          Calculator calculatorProxyImpl = (Calculator) constructor.newInstance(
              new InvocationHandler() {
              // proxy表示代理对象本身
              // method表示本次被调用代理对象的方法
              // args表示本次被调用代理对象的方法参数
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                  return 1;
              }
          });
  
          // 调用方法
          System.out.println(calculatorProxyImpl.getCount());
      }
  }
  ```

  1. Calculator接口，通过Proxy.getProxyClass()获取代理对象的Class信息，Class<$Proxy0>
  2. Class<$Proxy0>，通过反射获取构造方法，Constructor实例
  3. Constructor实例，通过newInstance()方法，获取Calculator接口实例，需传入一个InvocationHandler实例。
  4. 当Calculator接口实例调用具体实现类方法时，委托给InvocationHandler实例的invoke()方法进行调用。具体调用哪个实现类，是由InvocationHandler实例决定的。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_动态代理底层逻辑.png)

-  InvocationHandler接口的作用

  1.  JVM创建接口的代理对象时，不必考虑方法实现，只要造一个空壳的代理对象。
  2.  后期代理对象可以自定义实现方法。

####2.3 代理对象调用目标对象

- 代理对象的Class信息

  1. 带有一个构造器 -- 实例化代理对象
  2. 接口和Object类的所有方法，方法体为空，方法调用InvocationHandler的invoke()方法。 -- 用于调用目标对象

- 封装实例化代理对象的方法

  将代理对象和接口的联系，转换为代理对象和实现类的联系。

  ```java
  public class TargetObjectProxyDemo {
      public static void main(String[] args) throws Throwable {
          CalculatorImpl target = new CalculatorImpl();
          //传入目标对象目的：1.根据它实现的接口生成代理对象 2.代理对象调用目标对象方法
          Calculator calculatorProxy = (Calculator) getProxy(target);
          // 调用方法
          calculatorProxy.getName();
      }
  
      // 封装实例化代理对象的方法,传入目标对象
      private static Object getProxy(final Object target) throws Exception {
          // 1.获取代理对象的Class信息
          Class proxyClazz = Proxy.getProxyClass(
              target.getClass().getClassLoader(), target.getClass().getInterfaces());
          // 2.获取代理对象的构造方法
          Constructor constructor = proxyClazz.getConstructor(InvocationHandler.class);
          // 3.使用目标对象,自定义实现invoke()方法
          Object proxy = constructor.newInstance(new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                  System.out.println(method.getName() + "方法开始执行...");
                  Object result = method.invoke(target, args);
                  System.out.println(result);
                  System.out.println(method.getName() + "方法执行结束...");
                  return result;
              }
          });
          return proxy;
      }
  }
  ```

- Proxy.newProxyInstance()方法--直接获取代理对象实例

  ```java
  /**
  * 通过Proxy.newProxyInstance()方法实现动态代理
  **/
  public class SimpleProxyDemo {
  
      public static void main(String[] args) throws Exception{
          // 声明一个实现类
          CalculatorImpl calculator = new CalculatorImpl();
          // 动态代理生成一个接口实例
          Calculator cal = (Calculator) getProxy(calculator);
          // 调用方法
          cal.getName();
      }
  
      // 通过newProxyInstance方法返回一个接口实例
      private static Object getProxy(final Object target) throws Exception {
          // 获取代理对象的Class信息,实例化代理对象,并通过目标对象自定义方法实现
          Object proxy = Proxy.newProxyInstance(
              target.getClass().getClassLoader(), target.getClass().getInterfaces(),
                  new InvocationHandler(){
                      @Override
                      public Object invoke(Object proxy, Method method, Object[] args) 
                          throws Throwable {
                          System.out.println(method.getName() + "方法开始执行...");
                          Object result = method.invoke(target, args);
                          System.out.println(result);
                          System.out.println(method.getName() + "方法执行结束...");
                          return result;
                      }
                  }
          );
          return proxy;
      }
  }
  ```

#### 2.4日志打印的实例

```java
/**
* 通知接口
**/
public interface Advice {
	void beforeMethod(Method method);
	void afterMethod(Method method);
}

/**
* 日志打印--实现通知接口
**/
public class MyLogger implements Advice {

	public void beforeMethod(Method method) {
		System.out.println(method.getName() + "方法执行开始...");
	}

	public void afterMethod(Method method) {
		System.out.println(method.getName() + "方法执行结束...");
	}
}

/**
* 封装日志打印的动作,并嵌入到invoke()方法中
**/
public class ProxyTest {
	public static void main(String[] args) throws Throwable {
		CalculatorImpl target = new CalculatorImpl();
		Calculator calculatorProxy = (Calculator) getProxy(target, new MyLogger());
		calculatorProxy.add(1, 2);
		calculatorProxy.subtract(2, 1);
	}
	
    // Advice参数为通知接口,直接使用接口添加到方法调用逻辑
	private static Object getProxy(final Object target, Advice logger) throws Exception {
		// 代理对象的方法最终都会被JVM导向它的invoke方法
		Object proxy = Proxy.newProxyInstance(
				target.getClass().getClassLoader(),target.getClass().getInterfaces(), 
				(proxy1, method, args) -> {
					logger.beforeMethod(method);
					Object result = method.invoke(target, args);
					System.out.println(result);
					logger.afterMethod(method);
					return result;
				}
		);
		return proxy;
	}
}
```

###3.CGLib动态代理

#### 3.1JDK动态代理和CGLib动态代理的区别

- 实现原理
  1. JDK动态代理是利用反射机制生成一个实现接口的代理类(匿名类)，在调用具体方法前调用InvokeHandler来处理。 
  2. cglib动态代理是利用asm开源包，对代理对象类的class文件加载进来，通过修改其字节码生成子类来处理。 
- 应用场景
  1. JDK动态代理只能对，实现了接口的类生成代理。如果类没有实现接口，则不能使用。
  2. CGLib动态代理是针对类实现代理，无论类有无实现接口都可以。如果类没有实现接口，则只能用CGLib动态代理。主要是对指定的类生成一个子类，覆盖其中的方法因为是继承，所以该类或方法最好不要声明成final。

#### 3.2CGLib动态代理实例

- 环境搭建

  导入 包asm-7.0.jar 和 包cglib-3.3.0.jar

  ```java
  /**
  * Cglib动态代理，实现MethodInterceptor接口
  * Cglib是通过直接继承被代理类，并委托回调函数来做具体的事情.
  **/
  public class CglibProxyDemo implements MethodInterceptor {
      // 目标对象
      private Object target;
  
      // 实现拦截方法-回调函数
      public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy)
          throws Throwable {
          System.out.println("Cglib动态代理，监听开始！");
          Object invoke = method.invoke(target, objects);//方法执行，参数：target 目标对象 arr参数数组
          System.out.println(invoke.toString());
          System.out.println("Cglib动态代理，监听结束！");
          return invoke;
      }
  
  
      // 获取代理对象
      public Object getCglibProxy(Object objectTarget){
          // 初始化目标对象
          this.target = objectTarget;
  
          Enhancer enhancer = new Enhancer();
          // 设置父类,因为Cglib是针对指定的类生成一个子类，所以需要指定父类
          enhancer.setSuperclass(objectTarget.getClass());
          // 设置回调,拦截方法
          enhancer.setCallback(this);
  
          // 创建并返回代理对象
          Object result = enhancer.create();
          return result;
      }
  
  
      // 使用CGLib动态代理
      public static void main(String[] args){
          // 1.实例化CglibProxyDemo对象
          CglibProxyDemo cglibProxyDemo = new CglibProxyDemo();
          // 2.获取代理对象
          Box box = (Box) cglibProxyDemo.getCglibProxy(new Box());
          // 3.调用方法
          box.getColor();
      }
  }
  ```

  

