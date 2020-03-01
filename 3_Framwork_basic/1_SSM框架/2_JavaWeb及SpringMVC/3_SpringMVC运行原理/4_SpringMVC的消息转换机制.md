## 对响应的处理流程

### 1.问题描述

1. MVC模式下，HandlerAdapter返回ModelAndView对象。再交由ViewResolver渲染成View视图，返回给客户端。
2. 微服务环境下，单体应用对请求进行处理，接口只需返回JSON对象。
3. SpringMVC对于微服务环境下的响应处理是如何进行的。



## SpringMVC的消息转换机制

### 1.Servlet规范中请求响应报文的封装

```java
/**
* Servlet请求封装对象
* 读取Http原始请求报文内容,请求报文封装成为一个ServletInputStream的输入流
**/
package javax.servlet;
public interface ServletRequest {
    public ServletInputStream getInputStream() throws IOException; 
}


/**
* Servlet响应封装
* 输出Http的响应报文内容,响应报文封装成一个ServletOutputStream的输出流
**/
package javax.servlet;
public interface ServletResponse {
    public ServletOutputStream getOutputStream() throws IOException;
}
```



### 2.SpringMVC对请求响应报文的封装

#### 2.1HttpInputMessage -- 请求报文对象

```java
package org.springframework.http;
import java.io.IOException;
import java.io.InputStream;

public interface HttpInputMessage extends HttpMessage {
    // 获取请求报文的输入流
	InputStream getBody() throws IOException;
}
```



#### 2.2HttpOutputMessage -- 响应报文对象

```java
package org.springframework.http;
import java.io.IOException;
import java.io.OutputStream;

public interface HttpOutputMessage extends HttpMessage {
    // 获取响应报文的输出流
	OutputStream getBody() throws IOException;
}
```



#### 2.3HttpMessageConverter  -- 消息转换器对象

```java
package org.springframework.http.converter;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

public interface HttpMessageConverter<T> {

	boolean canRead(Class<?> clazz, MediaType mediaType);

	boolean canWrite(Class<?> clazz, MediaType mediaType);

	List<MediaType> getSupportedMediaTypes();

    // 读取HttpInputMessage中的请求报文内容
	T read(Class<? extends T> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException;

    // 写入HttpOutputMessage响应报文
	void write(T t, MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException;

}
```



#### 2.4消息转换机制

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/11_消息转换机制.png)

#####2.4.1大体处理流程

```java
// 接口定义
@RequestMapping(value="/string", method=RequestMethod.POST)
public @ResponseBody String readString(@RequestBody String string) {
    return "Read string '" + string + "'";
}
```

1. 在进入`readString`方法前，根据`@RequestBody`注解选择适当的`HttpMessageConverter`实现类来将请求参数解析到`String`变量中。

   本质上，使用了`StringHttpMessageConverter`类，`canRead()`方法返回`true`，`read()`方法会从请求中读出请求参数，绑定到`readString()`方法的`String`变量中

2. 执行`readString`方法后，识别到返回值`@ResponseBody`注解，`canWrite()`方法返回`true` ，使用`StringHttpMessageConverter`的`write()`方法，将结果作为`String`值写入响应报文。

##### 2.4.2RequestResponseBodyMethodProcessor消息转换处理工具类

```java
/**
* RequestResponseBodyMethodProcessor实现了两个接口,方法参数解析和返回值处理两种功能
* 1.HandlerMethodArgumentResolver,将请求报文绑定到处理方法形参的策略接口
* 2.HandlerMethodReturnValueHandler,对处理方法返回值进行处理的策略接口
**/


public class RequestResponseBodyMethodProcessor {
    
    /**
    * 实现HandlerMethodArgumentResolver接口
    **/
    
    // 1.方法supportsParameter(),作用是判断方法参数是否有RequestBody注解
    @Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestBody.class);
	}
    
    // 2.方法resolveArgument(),作用是调用消息处理器,进行方法参数处理
    @Override
    public Object resolveArgument(MethodParameter parameter, 
                                  @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, 
                                  @Nullable WebDataBinderFactory binderFactory) throws Exception {

        // 方法参数判空
        parameter = parameter.nestedIfOptional();

        // 使用消息转换实现类读取请求报文
        Object arg = readWithMessageConverters(webRequest, 
                                               parameter, 
                                               parameter.getNestedGenericParameterType());
        String name = Conventions.getVariableNameForParameter(parameter);

        if (binderFactory != null) {
            WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);

            if (arg != null) {
                // 判断是否有@Validated验证注解,进行参数的验证
                validateIfApplicable(binder, parameter);

                // 抛错
                if (binder.getBindingResult().hasErrors() 
                    && isBindExceptionRequired(binder, parameter)) {
                    throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
                }
            }

            if (mavContainer != null) {
                mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name,
                                          binder.getBindingResult());
            }
        }

        return adaptArgumentIfNecessary(arg, parameter);
    }
    
    
    /**
    * 实现HandlerMethodReturnValueHandler接口
    **/
    // 1.方法supportsReturnType(),作用是判断返回参数或方法体是否有ResponseBody注解
    @Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return (AnnotatedElementUtils.hasAnnotation(
                returnType.getContainingClass(),ResponseBody.class) || 
                returnType.hasMethodAnnotation(ResponseBody.class));
	}
    
    // 2.handleReturnValue(),作用是调用消息处理器,进行返回值处理
    @Override
    public void handleReturnValue(@Nullable Object returnValue, 
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, 
                                  NativeWebRequest webRequest)
        throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        // 调用消息转换实现类，写入响应报文
        writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
    }
    
}
```

#####2.4.3请求消息转换流程

1. `RequestMappingHandlerMapping` 处理请求映射的，处理 `@RequestMapping` 跟请求地址之间的关系。

2. `RequestMappingHandlerAdapter` 是请求处理的适配器，也就是请求之后处理具体逻辑的执行，关系到哪个类的哪个方法以及转换器等工作。

3. 最终将方法参数交给`HandlerMethodArgumentResolver`接口进行处理，该接口实现类有`HttpEntityMethodProcessor`类和`RequestResponseBodyMethodProcessor`类。

   - `HttpEntityMethodProcessor`类，处理没有带`@RequestBody`注解的方法参数，最终转换成`HttpEntity`实体类。
   - `RequestResponseBodyMethodProcessor`类，处理带`@RequestBody`注解的方法参数，遍历注册的消息转换器，转换成相应的格式进行处理。

4. 此外还有其他消息处理工具类，暂时不深究。

   

##### 2.4.4响应消息转换流程

1. `RequestMappingHandlerAdapter` 同时也是返回值处理的适配器。
2. 最终将方法参数交给`HandlerMethodArgumentResolver`接口进行处理，该接口实现类有`HttpEntityMethodProcessor`类和`RequestResponseBodyMethodProcessor`类。
   - `HttpEntityMethodProcessor`类，处理没有带`@ResponseBody`注解的返回值，最终写入输出流。
   - `RequestResponseBodyMethodProcessor`类，处理带`@ResponseBody注解的方法参数，遍历注册的消息转换器，转换成相应的格式进行处理，写入输出流。
3. 此外还有其他消息处理工具类，暂时不深究。



### 3.SpringMVC初始化的消息转换器

#### 3.1标签 mvc:annotation-driven 作用

```xml
<mvc:annotation-driven>
         <!-- 载入json处理类，将结果转换为json字符串 -->
         <mvc:message-converters>
             <bean class="org.springframework.http.converter.StringHttpMessageConverter"/>
             <bean class="com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter">
                 <property name="supportedMediaTypes">
                     <list>
                         <value>text/html;charset=UTF-8</value>
                         <value>application/json;charset=UTF-8</value>
                         <value>application/xml;charset=UTF-8</value>
                     </list>
                 </property>
             </bean>
         </mvc:message-converters>
     </mvc:annotation-driven>
```

1. 会自动注册`RequestMappingHandlerMapping`、`RequestMappingHandlerAdapter`、`ExceptionHandlerExceptionResolver`三个bean支持使用了像`@RquestMapping`、`@ExceptionHandler`等等的注解的`controller`方法去处理请求。
2. 支持使用了ConversionService的实例对表单参数进行类型转换。
3. 支持使用`@NumberFormat`、`@NumberFormat`注解对数据类型进行格式化。
4. 支持使用`@Valid`对`JavaBean`进行`JSR-303`验证。
5. 支持使用`@RequestBody`、`@ResponseBody`。

####3.2默认注册的七个消息转换器

1. ByteArrayHttpMessageConverter 
2. StringHttpMessageConverter 
3. ResourceHttpMessageConverter 
4. SourceHttpMessageConverter 
5. XmlAwareFormHttpMessageConverter,解析XML数据
6. Jaxb2RootElementHttpMessageConverter 
7. MappingJacksonHttpMessageConverter,解析JSON数据

#### 3.3使用步骤

1. 定义一个消息转换器，使用自带的或者自定义都可以。
2. 向`SpringMVC`注册消息转换器，可以通过xml和配置类两种方式。
3. 使用`@RequestBody`或者`@ResponseBody`注解，使用消息转换器。

```java
/**
* 配置类注册
**/
@Configuration
public class HttpCoverConf extends WebMvcConfigurationSupport {
    //1.定义消息转换器，使用已有的或者自定义都可以。
    @Bean
    public ByteArrayHttpMessageConverter getByteConverter(){
        return new ByteArrayHttpMessageConverter();
    }

    //2.注册消息转换器,会覆盖掉原有的消息转换器，需要调用父类注册才不会覆盖。
    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(getByteConverter());
        super.configureMessageConverters(converters);
    }

    //3.注册消息转换器，不会覆盖掉原有，仅仅是往已有的消息转换器链表中新添加一个。
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(getByteConverter());
        super.extendMessageConverters(converters);
    }
}
```



#### 3.4自定义的消息转换器

```java
/**
* 添加处理Long丢失精度的消息处理,在消息转换中将long类型转换为String
**/
@Configuration
public class SupportRestConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SupportRestConfig.class);

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        List<HttpMessageConverter<?>> needClear = new ArrayList<>();
        MappingJackson2HttpMessageConverter jacksonConverter = null;
        for (HttpMessageConverter<?> converter : converters) {
            // 只修改Json的消息转换器,解决Long精度丢失问题
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                jacksonConverter = (MappingJackson2HttpMessageConverter) converter;
                ObjectMapper objectMapper = jacksonConverter.getObjectMapper();
                
                // 添加处理
                SimpleModule module = new SimpleModule();
                module.addSerializer(Long.class, ToStringSerializer.instance);
                module.addSerializer(Long.TYPE, ToStringSerializer.instance);
                
                // 注册
                objectMapper.registerModule(module);
                break;
            }

        }
        converters.clear();
        // 只添加一个Json消息处理
        converters.add(jacksonConverter);
    }
}
```

