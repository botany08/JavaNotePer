## Filter简介

### 1.责任链模式

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/17_责任链模式的实现.png)

- 主要角色

  1. 抽象处理者(Handler)角色

     定义出一个处理请求的接口。如果需要，接口可以定义出一个方法以设定和返回对下家的引用

  2. 具体处理者(ConcreteHandler)角色

     具体处理者接到请求后，可以选择将请求处理掉，或者将请求传给下家。具体处理者持有对下家的引用，在需要的时候可以访问下家。

- 实例

  ```java
  /**
  * 抽象处理角色
  **/
  public abstract class StaffMember {
      // 持有后继责任对象
      protected StaffMember nextStaff;
  
      // 获取后继责任对象
      public StaffMember getNextStaff() {
          return nextStaff;
      }
  
      // 设置后继责任对象
      public void setNextStaff(StaffMember nextStaff) {
          this.nextStaff = nextStaff;
      }
  
      // 统一处理请求的方法
      public abstract void handleRequest();
  }
  
  /**
  * 具体处理者角色
  **/
  // 助理
  public class Assistant extends StaffMember {
  
      // 如果有后继责任对象,则流转给下一个.如果没有,则本身处理请求.
      @Override
      public void handleRequest() {
          if(getNextStaff() != null) {
              System.out.println("助理流转请求到下一个处理对象");
              getNextStaff().handleRequest();
          } else {
              System.out.println("助理处理完成请求!");
          }
      }
  }
  // 经理
  public class Manager extends StaffMember {
      @Override
      public void handleRequest() {
          if(getNextStaff() != null) {
              System.out.println("经理流转请求到下一个处理对象.");
              getNextStaff().handleRequest();
          } else {
              System.out.println("经理处理完成请求!");
          }
      }
  }
  
  /**
  * 客户端调用
  **/
  public class ChainRespoDemo {
      public static void main(String[] args){
          // 1.组装责任链
          StaffMember staffMemberA = new Assistant();
          StaffMember staffMemberB = new Manager();
          staffMemberA.setNextStaff(staffMemberB);
  
          // 2.处理请求
          staffMemberA.handleRequest();
      }
  }
  ```



### 2.Filter的实现

- 基本定义

  Filter过滤器会拦截所有的Servlet/JSP请求，当Filter处理完成后，才会将请求流转下去。

- Tomcat保证Filter的优先执行

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/18_Tomcat保证Filter的优先执行.png)

  1. Tomcat存在类似Spring的对象管理功能。Tomcat容器初始化时，会把所有Filter对象都注入到FilterChain中，所以再次执行过滤器链，就会经过新增的过滤器。

  

  - SpringMVC中过滤器拦截器及AOP的执行
    1. DispatcherServlet本质还是一个Servlet，而Filter会在Servlet前执行。至于SpringMVC的其他组件，更是在DispatcherServlet之后，所以Filter在整体都在SpringMVC之前执行。
    2. SpringMVC的拦截器是在SpringMVC各个组件之间起作用的。
    3. AOP则是在同一个组件中要执行某个方法时才起作用。
    4. 执行顺序为：Filter(框架外)  >   Interceptor(框架内组件)   >   AOP(组件内方法间)

  

### 3.自定义实现Filter

- 执行流程
  1. 定义责任链上的对象，如Filter。用于执行任务
  2. 定义责任链主体对象，如FilterChain。用于组装责任链，执行责任链
  3. 客户端通过FilterChain调用责任链，FilterChain调用第一个A，A执行完后再将使用权返回给FilterChain。FilterChain再继续调用B，依次类推。等到执行完最后一个责任对象时，FilterChain执行回调函数，从里到外依次调用，直至第一个A。

```java
/**
* Filter -- 责任对象,位于责任链上的对象
**/
public interface FilterDemo {
    void doFilter(RequestDemo request, ResponseDemo response, FilterChainDemo chain);
}


class HTMLFilter implements FilterDemo {
    @Override
    public void doFilter(RequestDemo request, ResponseDemo response, FilterChainDemo chain) {
        request.doSomething("HTMLFilter Request");
        chain.doFilter(request, response);
        response.doSomething("HTMLFilter Response");
    }
}

class SensitiveFilter implements FilterDemo {
    @Override
    public void doFilter(RequestDemo request, ResponseDemo response, FilterChainDemo chain) {
        request.doSomething("SensitiveFilter Request");
        chain.doFilter(request, response);
        response.doSomething("Sensitive Response");
    }
}

/**
* FilterChain -- 责任链对象,用于执行责任链的主体
**/
public class FilterChainDemo {

    // 标识当前执行到第几个过滤器
    private int index = 0;
    // 所有已注册的过滤器
    private List<FilterDemo> filters = new ArrayList<FilterDemo>();

    // 1.组装过滤器链
    public FilterChainDemo addFilter(FilterDemo filter) {
        filters.add(filter);
        // return this，返回当前对象，即可形成链式调用
        return this;
    }

    // 2.执行过滤器
    public void doFilter(RequestDemo request, ResponseDemo response) {
        // 所有过滤器执行完毕，return
        if (index == filters.size()) {
            return;
        }
        // 得到过滤器
        FilterDemo filter = filters.get(index);
        // 自增操作不能和下面doFilter互换
        index++;
        // 执行过滤器
        filter.doFilter(request, response, this);
    }
}

/**
* 客户端对象
**/
public class SelfdefineFilterDemo {
    public static void main(String[] args){
        // 1.封装请求对象
        RequestDemo request = new RequestDemo();
        ResponseDemo response = new ResponseDemo();

        // 2.初始化过滤器链
        FilterChainDemo filterChain = new FilterChainDemo();

        // 3.注册过滤器
        filterChain.addFilter(new HTMLFilter()).addFilter(new SensitiveFilter());

        // 4.执行过滤器
        filterChain.doFilter(request,response);

    }
}

class RequestDemo {
    public void doSomething(String job) {
        System.out.println("do "+ job);
    }

}
class ResponseDemo {
    public void doSomething(String job) {
        System.out.println("do "+ job);
    }
}
```

###4.Filter+动态代理解决全站编码问题

```java
public class CharacterEncodingFilter implements Filter {
    public void destroy() {}
    public void init(FilterConfig config) throws ServletException {}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
        throws ServletException, IOException {

		// 响应编码设置,后面Servlet拿到的Response对象都是已经设置过编码的
		res.setContentType("text/html;charset=utf-8");

		/*
		 * 请求编码设置思路：
		 *
		 * 1.特别注意，这里的request/response类型是ServletRequest/ServletResponse，我们要强转成Http相关的
		 * 2.Filter传给后面Servlet的Request对象肯定不能是原先的，不然request.getParameter()还是会乱码
		 *   这里使用动态代理生成代理对象，所以后面Servlet拿到的其实是代理Request
		 * 3.对于Get、Post请求，解决乱码的方式是不同的，所以代理对象内部必须有针对两者的判断
		 * */

		// 1.强转req/res, req加final防止引用类型的指针值发生变化,保证多线程的时候不会指向另外对象.
		final HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		// 2.使用Proxy.newProxyInstance()创建Request代理对象
		Object proxyRequest = Proxy.newProxyInstance(
            // 类加载器
            this.getClass().getClassLoader(),
            // 代理对象要和目标对象实现相同接口
            req.getClass().getInterfaces(), 
            new InvocationHandler() { // InvocationHandler，采用匿名内部类的方式
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) 
                        throws Throwable {
						// 由于乱码的根源在于getParameter()，所以我们只盯着这个方法
						if (!"getParameter".equalsIgnoreCase(method.getName())) {
							return method.invoke(request, args);
						}

						// 3.判断是Get还是Post
						if ("GET".equalsIgnoreCase(request.getMethod())) {
							// 按默认编码ISO-8859-1取出
							String value = (String) method.invoke(request, args);
							// 按IOS-8859-1得到字节，再按UTF-8转成中文
							value = new String(value.getBytes("ISO-8859-1"), "UTF-8");
							// 返回正确的中文
							return value;
						} else {
							// 目标方法前设置编码
							request.setCharacterEncoding("UTF-8");
							Object value = method.invoke(request, args);
							return value;
						}
					}
				});
        
        // 3.把代理request对象传给Servlet
        chain.doFilter((HttpServletRequest) proxyRequest, res);
	}
}
```

  

