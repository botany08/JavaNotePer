## SpringMVC基本原理

### 1.MVC模式分析

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_MVC模式结构.png)

#### 1.1主要模块

- M-Model 模型：主要是业务逻辑，主要是实体类，包括service+dao+entity。
- V-View 视图：主要是界面的展示，包括html+jsp。
- C-Controller 控制器：接收请求—>调用模型—>根据结果派发页面

#### 1.2框架(SpringMVC和Struts2)特点及区别

- 框架本身并不通过某种手段来干预或者控制浏览器发送Http请求的行为方式。服务器后端程序和前端通过HTTP请求进行交互。

- 页面(View层)和请求处理类(Controller)之间的映射关系通过某一种配置形式维系起来。

  在SpringMVC中，使用了Annotation注解。在Struts2中，默认采取XML配置文件。不过无论是哪一种配置形式，隐藏在其背后的都是对于请求映射关系的定义。

- Controller层的设计差异是不同MVC框架之间最主要的差异。

  SpringMVC使用方法参数来对请求的数据进行映射。而Struts2使用Controller类内部的属性来进行数据请求的映射。

- 浏览器和服务器的交互关系。

  在MVC模型中，浏览器端和服务器端的交互关系非常明确。无论采取什么样的框架，总是以一个明确的URL作为中心，辅之以参数请求。在后端可以接收到完整的HTTP请求，关键就是处理HTTP请求。

### 2.SpringMVC框架

####2.1 出现背景及应用

##### 2.1.1出现背景

1. `SpringMVC`是一个MVC的开源框架,`SpringMVC`就相当于是Struts2加上Spring的整合。
2. `SpringMVC`是`Spring`的一个后续产品，就是在原有基础上，又提供了web应用的MVC模块，可以简单的把`SpringMVC`理解为是Spring的一个模块（类似AOP，IOC这样的模块）,与之相关的包有spring-web和spring-webmvc。
3. `SpringMVC`和`Spring`无缝集成的意思是SpringMVC就是Spring的一个子模块，不需要同Spring进行整合。

##### 2.1.2主要应用

1. SpringMVC作为一个表现层框架，主要是解决Web开发领域中表现层中的三大问题。URL到框架的映射、Http请求参数绑定、Http响应的生成和输出
2. 学习一个框架，首要的是要先领会它的设计思想。从抽象、从全局上来审视这个框架。其中最具有参考价值的，就是这个框架所定义的核心接口。核心接口定义了框架的骨架，也在最抽象的意义上表达了框架的设计思想。

#### 2.2基本工作原理图

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/2_SpringMVC处理流程.png)

#### 2.3处理流程

##### 2.3.1DI调用处理器映射器HandlerMapping – 找到处理器Handler

1. 用户发送请求到前端控制器`DispatcherServlet`, 前端控制器收到请求后自己不进行处理，而是委托给其他的解析器进行处理，作为统一访问点，进行全局的流程控制。

2. 初始化DI。从web.xml中获取`DispatcherServlet`的名称，默认地址在Web-INF下面。初始化工程的配置文件，默认名称为：[name]-servlet.xml

   ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/3_DI配置文件.png)

3. `DispatcherServlet`收到请求调用处理器映射器`HandlerMapping`。

4. 处理器映射器找到具体的处理器(可以根据xml配置、注解进行查找)，`HandlerMapping`通过策略将请求映射为`HandlerExecutionChain` 对象(包含一个`Handler`处理器 [页面控制器] 对象、多个`HandlerInterceptor`拦截器对象)，应用**策略模式**，实现策略接口，可以实现不同的策略。

##### 2.3.2DI调用处理器适配器 – 调用具体处理器，返回一个`ModelAndView`

1. `DispatcherServlet`调用`HandlerAdapter`处理器适配器。
2. `HandlerAdapter` 将会把处理器包装为适配器，从而支持多种类型的处理器，即**适配器模式**。从而支持很多类型的处理器, 将会根据适配的结果调用真正的处理器的功能处理方法(`Controller`也叫后端控制器)。
3. 调用具体的java类，使用`@Controller`表明控制器，使用`@RequestMapping`表明控制器的访问地址和访问`Method`，紧跟的方法为访问地址所调用的方法。
4. `Controller`执行完成返回`ModelAndView`。
5. `HandlerAdapter`将`controller`执行结果`ModelAndView`返回给`DispatcherServlet`。

##### 2.3.3DI将`ModelAndView`传给视图解析器，返回一个view

1. `DispatcherServlet`将`ModelAndView`传给`ViewReslover`视图解析器。
2. `ViewResolver`将把逻辑视图名解析为具体的`View`，通过这种策略模式，很容易更换其他视图技术，最后返回具体的`View`。

##### 2.3.4DI渲染视图，响应用户跳到view视图

1. `DispatcherServlet`根据`Model`获取参数数据，此处的`Model`实际是一个Map数据结构，进行渲染视图(即将模型数据填充至视图中)生成渲染后的view。

   ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_视图处理器.png)

2. `DispatcherServlet`响应用户，返回控制权给`DispatcherServlet`，最后再把view视图展示给用户。

#### 2.4框架组件介绍

##### 2.4.1前端控制器DispatcherServlet（不需要工程师开发）,由框架提供

1. 作用：接收请求，响应结果，相当于转发器，中央处理器。有了`DispatcherServlet`减少了其它组件之间的耦合度。
2. `DispatcherServlet`分别与浏览器，`HandlerMapping`/`HandlerAdapter`/`ViewResolver`进行交互，完成整个访问流程。

##### 2.4.2处理器映射器HandlerMapping(不需要工程师开发),由框架提供

1. 作用：根据请求的`url`查找`Handler`处理器。
2. `HandlerMapping`负责根据用户请求找到`Handler`即处理器，`springmvc`提供了不同的映射器实现不同的映射方式，例如：配置文件方式，实现接口方式，注解方式等。

##### 2.4.3处理器适配器HandlerAdapter

1. 作用：按照特定规则(`HandlerAdapter`要求的规则)去执行`Handler`。
2. 通过`HandlerAdapter`对处理器进行执行(适配器模式)，通过扩展适配器可以对更多类型的处理器进行执行。

##### 2.4.4处理器Handler(需要工程师开发)  --  后端控制器

1. 作用：`Handler`是继`DispatcherServlet`前端控制器的后端控制器，在`DispatcherServlet`的控制下`Handler`对具体的用户请求进行处理。
2. 编写`Handler`时按照`HandlerAdapter`的要求去做，这样适配器才可以去正确执行`Handler`。

##### 2.4.5视图解析器View resolver(不需要工程师开发)，由框架提供

1. 作用：进行视图解析，根据逻辑视图名解析成真正的视图(view)
2. `ViewResolver`负责将处理结果生成View视图，`ViewResolver`首先根据逻辑视图名解析成物理视图名即具体的页面地址，再生成`View`视图对象，最后对`View`进行渲染将处理结果通过页面展示给用户。 `springmvc`框架提供了很多的`View`视图类型，包括：`jstlView`、`freemarkerView`、`pdfView`等。
3. 一般情况下需要通过页面标签或页面模版技术将模型数据通过页面展示给用户，需要由工程师根据业务需求开发具体的页面。

##### 2.4.6视图View(需要工程师开发jsp)

1. 作用：View是一个接口，实现类支持不同的`View`类型（`jsp`、`freemarker`、`pdf`...）



