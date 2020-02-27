## 对响应的处理流程

### 1.问题描述

1. MVC模式下，HandlerAdapter返回ModelAndView对象。再交由ViewResolver渲染成View视图，返回给客户端。
2. 微服务环境下，单体应用对请求进行处理，接口只需返回JSON对象。
3. SpringMVC对于微服务环境下的响应处理是如何进行的。