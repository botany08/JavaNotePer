## Struts2与SpringMVC区别

###1. 类级别和方法级别  

1. `Struts2`是类级别的拦截， 一个类对应一个`Request`上下文。`SpringMVC`是方法级别的拦截，一个方法对应一个`Request`上下文，而方法同时又跟一个`url`对应。
2. 从架构本身上`SpringMVC`就容易实现`RestfulUrl`,而`struts2`的架构实现起来要费劲。因为`Struts2`中`Action`的一个方法可以对应一个`url`，而其类属性却被所有方法共享，就无法用注解或其他方式标识其所属方法。

### 2.resquest和respose的区别

1. `SpringMVC`的方法之间基本上独立的，独享`request`/`response`数据，请求数据通过参数获取，处理结果通过`ModelMap`交回给框架，方法之间不会共享变量。
2. `Struts2`方法之间是独立的，但其所有`Action`变量是共享的.每次来了请求就创建一个`Action`，一个`Action`对象对应一个`request`上下文。

### 3.struts2比较耗费内存

- 由于`Struts2`针对每个`request`进行封装，把`request`，`session`等`servlet`生命周期的变量封装成一个一个`Map`，供给每个`Action`使用并保证线程安全，所以是比较耗费内存的。

### 4.struts2配置文件量大

- 拦截器实现机制上，`Struts2`自己实现`interceptor`机制，`SpringMVC`用的是独立的`AOP`方式，导致`Struts2`的配置文件量比`SpringMVC`大。

### 5.入口不一样

- `SpringMVC`的入口是`servlet`，而`Struts2`是`filter`，导致了二者的机制不同，主要是`servlet`和`filter`的区别。

### 6.SpringMVC集成Ajax

- `SpringMVC`集成了`Ajax`，使用注解`@ResponseBody`可以实现返回`json`。`Struts2`拦截器集成了`Ajax`，在`Action`中处理时一般必须安装插件或者自己写代码集成进去，使用起来也相对不方便。

### 7.SpringMVC支持验证

- `SpringMVC`验证支持`JSR303`，处理起来相对更加灵活方便，而Struts2验证比较繁琐。

### 8.SpringMVC和Spring集成效果好

- SpringMVC和Spring是无缝的，项目的管理和安全上也比Struts2高。