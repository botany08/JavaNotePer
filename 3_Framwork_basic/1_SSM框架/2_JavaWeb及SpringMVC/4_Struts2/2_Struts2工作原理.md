## Struts2工作原理

###1.工作基本流程

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/2_工作流程图.png" style="zoom: 67%;" />

1. 客户端传入一个请求对象，在Tomcat经过一系列的原生Filter。
2. 请求进入Struts2的核心Fileter，FilterDispatcher分发器。
3. FilterDispatcher解析请求对象，询问ActionMapper决定是否调用Action。
4. 如果ActionMapper决定调用某个Action，FilterDispatcher把请求的处理交给ActionProxy。
5. ActionProxy通过ConfigurationManager读取框架的配置文件，找到请求对应的Action类，是从struts.xml配置中读取。
6. ActionProxy创建一个ActionInvocation的实例,用来调用Action。
7. ActionInvocation实例使用命名模式来调用，在调用Action的过程前后，涉及到相关拦截器的调用。
8. Action执行完毕，ActionInvocation负责根据struts.xml中的配置找到对应的返回结果。
9. 返回结果通常是一个JSP或者FreeMarker的模版，可以使用Struts2框架的标签进行渲染，在这个过程中需要涉及到ActionMapper。

