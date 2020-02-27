## 完整的Web请求过程

### 1.核心类和接口

核心接口定义了框架的骨架，也在最抽象的意义上表达了框架的设计思想。

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/5_MVC接口处理流程.png)

###2.DispatcherServlet核心类

`DispatcherServlet`一个标准的servlet,主要作用是接受和转发web请求到内部框架处理单元。

#### 2.1DI → HandlerMapping处理器映射器

1. 当`DispatcherServlet`接收到`web`请求后，由标准`Servlet`类处理方法`doGet`或`doPost`处理。经过几次转发后，最终注册在`DispatcherServlet`类中的一个 `List`循环中被遍历，该List是由`HandlerMapping`实现类组成的。

2. 以该`web` 请求的`HttpServletRequest`对象为参数，依次调用`HandlerMapping`接口的`getHandler`方法，返回首个不为 `null` 的调用结果。会得到一个`HandlerExecutionChain`对象，就是`SpringMVC`对`URl`映射处理的结果。

   ```java
   public interface HandlerMapping {
       HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
   }
   ```

3. `HandlerMapping`接口的`getHandler`方法参数是`HttpServletRequest`，表示`HandlerMapping`的实现类可以利用`HttpServletRequest`中的所有信息，来处理生成`HandlerExecutionChain`对象。包括，请求头、url 路径、`cookie`、`session`、参数等等，最常用的是`url`路径。

#### 2.2DI → HandlerAdapter适配器

```java
public DispatcherServlet {
    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		...
	}
}
```

1. 在`DispatcherServlet`类中，`HandlerExecutionChain`中的`handler`对象会被作为参数传递进去，用来获取`HandlerAdapter`对象。
2. 获取到`HandlerAdapter`实现类，然后调用其`handler`方法处理`handler`对象，并返回`ModelAndView`对象(包含视图和数据)给`DispatcherServlet`类。

####2.3DI处理基本流程

1. 在SpringMVC调用中，HandlerExecutionChain中封装handler对象，就是@Controller注解标识类的一个实例。
2. 根据类级别和方法级别的@RequestMapping注解，由默认注册的DefaultAnnotationHandlerMapping生成HandlerExecutionChain对象。
3. 再由AnnotationMethodHandlerAdapter来执行这个HandlerExecutionChain对象。
4. 生成最终的ModelAndView对象后，传给具体的View对象，用render方法渲染视图。

### 3.HandlerMapping接口

- 核心方法`getHandler()`

  ```java
  // 作用：处理请求对象，并生成HandlerExecutionChain返回
  public interface HandlerMapping {
      HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
  }
  ```

### 4.HandlerExecutionChain返回类

```java
public class HandlerExecutionChain {
    // 执行对象
	private final Object handler;
	// 拦截器数组
	private HandlerInterceptor[] interceptors;
	// 拦截器集合
	private List<HandlerInterceptor> interceptorList;
    
    ......
}
```

#### 4.1定义

- `HandlerExecutionChain`类是一个执行链的封装，由一个实质的执行对象以及一堆拦截器组成。

#### 4.2执行流程

1. 在HandlerAdapter接口真正调用HandlerExecutionChain的handler对象前，HandlerInterceptor数组将会被遍历，其preHandle方法会被依次调用，然后才调用handler对象。
2. 在handler对象被调用后，就生成了需要的响应数据。在将处理结果写到HttpServletResponse对象之前，拦截器的postHandle方法会被依次调用。
3. 视图渲染完成后，最后拦截器的afterCompletion方法会被依次调用，整个web请求的处理过程就结束了。

### 5.HandlerInterceptor拦截器接口

```java
public interface HandlerInterceptor {
    // 1.预处理接口--在调用handler对象前
    default boolean preHandle(HttpServletRequest request, 
                              HttpServletResponse response, Object handler) throws Exception {
		return true;
	}
    
    // 2.主逻辑处理接口--在调用handler对象后,未返回response前
    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
	}
    
    // 3.善后接口--在返回response后
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                 Object handler,@Nullable Exception ex) throws Exception {
	}
}
```

### 6.HandlerAdapter适配器接口

```java
public interface HandlerAdapter {
    // 接收HandlerExecutionChain中的Handler对象，处理后并返回结果，DI会取结果为true的HandlerAdapter。
    boolean supports(Object handler);
    
    // 接收HttpServletRequest/Respose和Handler,并返回一个ModelAndView对象。
    // ModelAndView对象由一个Map数据模型和View对象组成，核心是View对象。
    @Nullable
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, 
                        Object handler) throws Exception;
    
    // 获取最后修改次数
    long getLastModified(HttpServletRequest request, Object handler);
}
```

### 7.View接口

```java
public interface View {
    String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";
    String PATH_VARIABLES = View.class.getName() + ".pathVariables";
    String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";
    
    @Nullable
	default String getContentType() {
		return null;
	}
    
    void render(@Nullable Map<String, ?> model, HttpServletRequest request, 
                HttpServletResponse response) throws Exception;
}
```

1. 所有的数据，最后会作为一个`Map`对象传递到`View`实现类中的`render`方法。调用`render`方法，就完成了视图到响应的渲染。
2. 从`ModelAndView`到真正的`View`实现类有一个解析的过程，`ModelAndView`中可以有真正的视图对象，也可以只是有一个视图的名字，`SpringMVC`会负责将视图名称解析为真正的视图对象。