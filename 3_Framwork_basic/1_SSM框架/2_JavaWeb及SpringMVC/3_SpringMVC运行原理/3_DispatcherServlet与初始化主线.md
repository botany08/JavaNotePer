##DispatcherServlet与初始化主线

### 1.DispatcherServlet的体系结构

#### 1.1运行主线

##### 1.1.1Servlet规范的两大主线

- 初始化主线--init()方法
  1. 在整个系统启动时运行，且只运行一次。
  2. 对整个应用程序进行初始化操作，对容器`WebApplicationContext`的初始化、组件和外部资源的初始化等。
- Http请求处理主线--service()方法
  1. 在整个系统运行的过程中处于侦听模式，侦听并处理所有的Web请求。
  2. 在service及其相关方法中，是对Http请求的处理流程。

##### 1.1.2初始化主线

- 负责对SpringMVC的运行要素进行初始化。

##### 1.1.3Http请求处理主线

- 负责对SpringMVC中的组件进行逻辑调度完成对Http请求的处理。

#### 1.2DispatcherServlet的继承结构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_DI类继承结构.png)

##### 1.2.1HttpServletBean

- 定义
  1. `HttpServletBean`是`Spring`对于`Servlet`最低层次的抽象。
- 作用
  1. `Spring`会将这个`Servlet`视作是一个`Spring`的`bean`，并将`init-param`中的值作为`bean`的属性注入进来。
  2. `HttpServletBean`重写`Servlet`的`init()`方法，将当前的`Servlet`类转化为一个`BeanWrapper`。从而能够以`Spring`的方式来对`init-param`的值进行注入。

##### 1.2.2FrameworkServlet

- 定义
  1. 通过`FrameworkServlet`真正初始化了一个`Spring`的容器`WebApplicationContext`，并引入到Servlet对象之中。
- 作用
  1. `FrameworkServlet`调用其内部的方法`initWebApplicationContext()`对`Spring`容器`WebApplicationContext`进行初始化。
  2. 通过`getWebApplicationContext()`方法暴露容器的引用，以供子类调用。
  3. 继承自`FrameworkServlet`的`DispatcherServlet`，也就直接拥有了与`WebApplicationContext`进行通信的能力。

#### 1.3数据结构

通过实例变量的方式，定义DI所拥有的数据结构。

- 配置参数 —— 控制SpringMVC组件的初始化行为方式
- 核心组件 —— SpringMVC的核心逻辑处理组件

### 2.SpringMVC的运行体系

#### 2.1体系组成

1. `SpringMVC`的整个运行体系，是由`DispatcherServlet`、组件和容器这三者共同构成的。
2. 继承结构，表示`DispatcherServlet`与`Spring`容器`WebApplicationContext`之间的关系。
3. 数据结构，表示`DispatcherServlet`与组件之间的关系。
4. `DispatcherServlet`是逻辑处理的调度中心，组件则是被调度的操作对象。而容器是协助`DispatcherServlet`更好地对组件进行管理。

#### 2.2各个元素的动态关系

1. DispatcherServlet和容器 —— DispatcherServlet对容器进行初始化
2. 容器和组件 —— 容器对组件进行全局管理
3. DispatcherServlet和组件 —— DispatcherServlet对组件进行逻辑调用

### 3.DispatcherServlet的初始化主线

#### 3.1初始化主线的基本过程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_DI的初始化过程.png)

1. 驱动要素，`servlet`中的`init`方法
2. 执行次序，`HttpServletBean` -> `FrameworkServlet` -> `DispatcherServlet`
3. 操作对象，`Spring`容器（`WebApplicationContext`）和组件

#### 3.2WebApplicationContext的初始化

#####3.2.1DispatcherServlet负责对容器WebApplicationContext进行初始化

```xml
<servlet>  
    <servlet-name>dispatcher</servlet-name>  
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>  
    <!-- 指定配置文件位置 -->
    <init-param>  
        <param-name>contextConfigLocation</param-name>  
        <param-value>classpath:web/applicationContext-dispatcherServlet.xml</param-value>  
    </init-param>  
    <!-- 默认启动加载 -->
    <load-on-startup>1</load-on-startup>  
</servlet>  
          
<servlet-mapping>  
    <servlet-name>dispatcher</servlet-name>  
    <url-pattern>/**</url-pattern>  
</servlet-mapping>  
```

- 在默认情况下，这个初始化过程是由web.xml中的入口程序配置所驱动
  1. 在`Tomcat`启动时默认加载`DispatcherServlet`类，并实例化。
  2. `DispatcherServlet`在初始化时会加载位置在`/WEB-INF/[servlet-name]-servlet.xml`的配置文件作为`SpringMVC`的核心配置。
  3. 在读取SpringMVC配置文件时，默认名称为 `/WEB-INF/ dispatcher-servlet.xml`，可以通过指定`contextConfigLocation`选项来自定义SpringMVC核心配置文件的位置。

##### 3.2.2容器WebApplicationContext读取SpringMVC的核心配置文件进行组件的实例化

- 在`[servlet-name]-servlet.xml`中配置组件实例化的Bean

```xml
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">  
    <property name="prefix" value="/" />  
    <property name="suffix" value=".jsp" />  
</bean>  
```

#### 3.3独立的WebApplicationContext体系

##### 3.3.1web.xml配置文件中的容器加载

```xml
<!-- ContextLoaderListener初始化传统的Spring容器,称之为Root WebApplicationContext-->
<context-param>  
    <param-name>contextConfigLocation</param-name>  
    <param-value>classpath:context/applicationContext-*.xml</param-value>  
</context-param>  
      
<listener>  
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>  
</listener>  
      
<!-- DispatcherServlet所初始化的容器,是SpringMVC WebApplicationContext -->  
<servlet>  
    <servlet-name>dispatcher</servlet-name>  
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>  
    <init-param>  
        <param-name>contextConfigLocation</param-name>  
        <param-value>classpath:web/applicationContext-dispatcherServlet.xml</param-value>  
    </init-param>  
    <load-on-startup>1</load-on-startup>  
</servlet>  
          
<servlet-mapping>  
    <servlet-name>dispatcher</servlet-name>  
    <url-pattern>/</url-pattern>  
</servlet-mapping>  
```

1. 在`DispatcherServlet`初始化的过程中所构建的`WebApplicationContext`独立于`Spring`自身的所构建的其他`WebApplicationContext`体系而存在。
2. `SpringMVC WebApplicationContext`能够感知到`Root WebApplicationContext`的存在，并且将其作为`parent`容器。
3. `Spring`正是使用这种`Parent-Child`的容器关系来对不同的编程层次进行划分。`SpringMVC`中所定义的一切组件能够无缝地与`Root WebApplicationContext`中的组件整合。

#### 3.4组件默认行为的指定

##### 3.4.1组件初始化的两个过程

- 在`FrameworkServlet`完成了对于`WebApplicationContext`和组件的初始化

- `DispatcherServlet`初始化组件，是从容器`WebApplicationContext`中读取组件的实现类，并缓存于`DispatcherServlet`内部的过程。

  `DispatcherServlet`中对于组件的初始化过程实际上是应用程序在`WebApplicationContext`中选择和查找组件实现类的过程，也是指定组件在`SpringMVC`中的默认行为方式的过程。

  ```java
  // DispatcherServlet初始化组件方法
  public class DispatcherServlet {
      private void initMultipartResolver(ApplicationContext context) {  
          try {  
              // 通过容器获取实例
              this.multipartResolver = 
                  context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);  
              if (logger.isDebugEnabled()) {  
                  logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");  
              }  
          } catch (NoSuchBeanDefinitionException ex) {  
              // Default is no multipart resolver.  
              this.multipartResolver = null;  
              if (logger.isDebugEnabled()) {  
                  logger.debug(
                      "Unable to locate MultipartResolver with name '" + 
                      MULTIPART_RESOLVER_BEAN_NAME +  "': no multipart request handling provided");  
              }  
          }  
      }  
  }
  ```



#####3.4.2DispatcherServlet指定SpringMVC默认组件的策略

- 名称查找，据bean的名字在容器中查找相应的实现类

- 自动搜索，自动搜索容器中所有某个特定组件（接口）的所有实现类

- 默认配置，根据一个默认的配置文件指定进行实现类加载

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/10_默认配置文件.png)

```java
/**
* 查找HandlerAdapter接口的策略
**/
public class DispatcherServlet {
    private void initHandlerAdapters(ApplicationContext context) {  
        this.handlerAdapters = null;  

        if (this.detectAllHandlerAdapters) {  
            // 自动搜索特定接口的所有实现类 
            Map<String, HandlerAdapter> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, 
                                                               true, false);  
            
            if (!matchingBeans.isEmpty()) {  
                this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());  
                OrderComparator.sort(this.handlerAdapters);  
            }  
        }  
        else {  
            try {  
                // 通过名称HANDLER_ADAPTER_BEAN_NAME查找
                HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class); 
                this.handlerAdapters = Collections.singletonList(ha);  
            }  
            catch (NoSuchBeanDefinitionException ex) {  
                ....
            }  
        }  

        // 默认配置
        if (this.handlerAdapters == null) {
            // 获取默认的配置文件并读取
            this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);  
            if (logger.isDebugEnabled()) {  
                logger.debug("No HandlerAdapters found in servlet '" 
                             + getServletName() + "': using default");  
            }  
        }  
    } 

}

```





