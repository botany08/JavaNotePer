## 简单实现Servlet容器

### 1.Servlet容器的工作原理

servlet 容器有3 个基本任务，对于每个请求，servlet容器会为其完成以下3个操作：

1. 创建一个request 对象，用可能会在调用的Servlet中使用到的信息填充该request 对象，如参数、头、cookie、查询字符串、URI 等。
2. 创建一个调用Serviet 的response对象，用来向Web 客户端发送响应。
3. 调用Servlet 的service()方法，将request对象和response对象作为参数传入。Servlet从request对象中读取信息，并通过response对象发送响应信息。

### 2.Tomcat的Servlet容器Catalina 

- 主要模块

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Catalina容器的主要模块.png)

  1. 连接器。

     连接器负责将一个请求与容器相关联。工作包括为它接收到的每个HTTP请求创建一个request对象和一个response对象。然后将具体的处理过程交给servlet容器。

  2. Servlet容器

     容器从连接器中接收到request 对象和response 对象，井负责调用相应的Servlet 的service()方法。



### 3.Servlet容器实现

- HttpServer1类-服务器类

  ```java
  package com.lin.servletcontainer;
  
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import java.net.InetAddress;
  import java.net.ServerSocket;
  import java.net.Socket;
  
  /**
   * 类 <code>{类名称}</code>{此类功能描述}
   *
   * @author zangbao.lin
   * @version 2020/1/13
   * @since JDK 1.8
   */
  
  public class HttpServer1 {
  
      // 关闭服务器命令
      private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
  
      // 服务器状态,是否关闭
      private boolean shutdown = false;
  
      // 服务器启动方法
      public static void main(String[] args) {
          HttpServer1 server = new HttpServer1();
          server.await();
      }
  
      // 服务器监听8080端口
      public void await() {
          // 监听端口
          ServerSocket serverSocket = null;
          int port = 8080;
          try {
              serverSocket =  new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
          }
          catch (IOException e) {
              e.printStackTrace();
              System.exit(1);
          }
  
  
          // 当服务器状态为开启
          while (!shutdown) {
              Socket socket = null;
              InputStream input = null;
              OutputStream output = null;
              try {
                  socket = serverSocket.accept();
                  input = socket.getInputStream();
                  output = socket.getOutputStream();
  
                  // 封装Request对象
                  Request request = new Request(input);
                  request.parse();
  
                  // 封装Response对象
                  Response response = new Response(output);
                  response.setRequest(request);
  
                  /**
                   * servlet,交由ServletProcessor1处理
                   * 静态资源,交由StaticResourceProcessor处理
                   */
                  if (request.getUri().startsWith("/servlet/")) {
                      ServletProcessor1 processor = new ServletProcessor1();
                      processor.process(request, response);
                  }
                  else {
                      StaticResourceProcessor processor = new StaticResourceProcessor();
                      processor.process(request, response);
                  }
  
                  // 关闭socket
                  socket.close();
                  // 更新服务器状态
                  shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
              }
              catch (Exception e) {
                  e.printStackTrace();
                  System.exit(1);
              }
          }
      }
  }
  
  ```

- Request 类 -- 封装请求

  ```java
  package com.lin.servletcontainer;
  
  import javax.servlet.*;
  import java.io.BufferedReader;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.UnsupportedEncodingException;
  import java.util.Enumeration;
  import java.util.Locale;
  import java.util.Map;
  
  /**
   * 类 <code>{类名称}</code>{此类功能描述}
   *
   * @author zangbao.lin
   * @version 2020/1/10
   * @since JDK 1.8
   */
  
  public class Request implements ServletRequest {
  
  
      private InputStream input;
      private String uri;
  
      public Request(InputStream input) {
          this.input = input;
      }
  
      public String getUri() {
          return uri;
      }
  
      public void parse() {
          // 请求报文字符串
          StringBuffer request = new StringBuffer(2048);
          int i;
          byte[] buffer = new byte[2048];
  
          try {
              // 将请求输入流读取到缓冲字节数组
              i = input.read(buffer);
          }
          catch (IOException e) {
              e.printStackTrace();
              i = -1;
          }
  
          for (int j=0; j<i; j++) {
              // 缓冲字节数组转换成字符串
              request.append((char) buffer[j]);
          }
  
          // 打印请求报文
          System.out.print(request.toString());
  
          // 解析URI地址
          uri = parseUri(request.toString());
      }
  
  
      // 解析报文,获取URL地址
      private String parseUri(String requestString) {
          int index1, index2;
          // 第一个空格位置,即请求方法和URI相隔的空格
          index1 = requestString.indexOf(' ');
  
          if (index1 != -1) {
              // 第二个空格位置,即URI和协议相隔的空格
              index2 = requestString.indexOf(' ', index1 + 1);
              if (index2 > index1) {
                  // 返回URI位置上的字符串
                  return requestString.substring(index1 + 1, index2);
              }
          }
          return null;
      }
  
  
      // ServletRequest接口默认实现
      @Override
      public Object getAttribute(String name) {
          return null;
      }
  
      @Override
      public Enumeration<String> getAttributeNames() {
          return null;
      }
  
      @Override
      public String getCharacterEncoding() {
          return null;
      }
  
      @Override
      public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
  
      }
  
      @Override
      public int getContentLength() {
          return 0;
      }
  
      @Override
      public long getContentLengthLong() {
          return 0;
      }
  
      @Override
      public String getContentType() {
          return null;
      }
  
      @Override
      public ServletInputStream getInputStream() throws IOException {
          return null;
      }
  
      @Override
      public String getParameter(String name) {
          return null;
      }
  
      @Override
      public Enumeration<String> getParameterNames() {
          return null;
      }
  
      @Override
      public String[] getParameterValues(String name) {
          return new String[0];
      }
  
      @Override
      public Map<String, String[]> getParameterMap() {
          return null;
      }
  
      @Override
      public String getProtocol() {
          return null;
      }
  
      @Override
      public String getScheme() {
          return null;
      }
  
      @Override
      public String getServerName() {
          return null;
      }
  
      @Override
      public int getServerPort() {
          return 0;
      }
  
      @Override
      public BufferedReader getReader() throws IOException {
          return null;
      }
  
      @Override
      public String getRemoteAddr() {
          return null;
      }
  
      @Override
      public String getRemoteHost() {
          return null;
      }
  
      @Override
      public void setAttribute(String name, Object o) {
  
      }
  
      @Override
      public void removeAttribute(String name) {
  
      }
  
      @Override
      public Locale getLocale() {
          return null;
      }
  
      @Override
      public Enumeration<Locale> getLocales() {
          return null;
      }
  
      @Override
      public boolean isSecure() {
          return false;
      }
  
      @Override
      public RequestDispatcher getRequestDispatcher(String path) {
          return null;
      }
  
      @Override
      public String getRealPath(String path) {
          return null;
      }
  
      @Override
      public int getRemotePort() {
          return 0;
      }
  
      @Override
      public String getLocalName() {
          return null;
      }
  
      @Override
      public String getLocalAddr() {
          return null;
      }
  
      @Override
      public int getLocalPort() {
          return 0;
      }
  
      @Override
      public ServletContext getServletContext() {
          return null;
      }
  
      @Override
      public AsyncContext startAsync() throws IllegalStateException {
          return null;
      }
  
      @Override
      public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
          return null;
      }
  
      @Override
      public boolean isAsyncStarted() {
          return false;
      }
  
      @Override
      public boolean isAsyncSupported() {
          return false;
      }
  
      @Override
      public AsyncContext getAsyncContext() {
          return null;
      }
  
      @Override
      public DispatcherType getDispatcherType() {
          return null;
      }
  }
  
  ```

- Response 类--封装响应

  ```java
  package com.lin.servletcontainer;
  
  import javax.servlet.ServletInputStream;
  import javax.servlet.ServletOutputStream;
  import javax.servlet.ServletResponse;
  import java.io.*;
  import java.util.Enumeration;
  import java.util.Locale;
  
  /**
   * 类 <code>{类名称}</code>{此类功能描述}
   *
   * @author zangbao.lin
   * @version 2020/1/13
   * @since JDK 1.8
   */
  
  public class Response implements ServletResponse {
  
      Request request;
      OutputStream output;
  
      public Response(OutputStream output) {
          this.output = output;
      }
  
      public void setRequest(Request request) {
          this.request = request;
      }
  
      // 读取静态文件内容
      public void sendStaticResource() throws IOException {
  
          try {
              // 初始化文件对象
              File file = new File(Constants.WEB_ROOT, request.getUri());
  
              // 写入响应头
              String headRes = "HTTP/1.1 200 OK\r\n" +
                      "Content-Type: text/html\r\n" +
                      "Content-Length: 23\r\n" +
                      "\r\n";
              output.write(headRes.getBytes());
  
              // 读取文件内容,并写入到输出流中
              BufferedReader br = new BufferedReader(new FileReader(file));
              String st;
              while ((st = br.readLine()) != null) {
                  output.write(st.getBytes());
  
              }
              output.flush();
          }
          catch (FileNotFoundException e) {
              String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                      "Content-Type: text/html\r\n" +
                      "Content-Length: 23\r\n" +
                      "\r\n" +
                      "<h1>File Not Found</h1>";
              output.write(errorMessage.getBytes());
          }
  
      }
  
      // ServeltResponse接口的默认实现
      @Override
      public String getCharacterEncoding() {
          return null;
      }
  
      @Override
      public String getContentType() {
          return null;
      }
  
      @Override
      public ServletOutputStream getOutputStream() throws IOException {
          return null;
      }
  
      @Override
      public PrintWriter getWriter() throws IOException {
          return null;
      }
  
      @Override
      public void setCharacterEncoding(String charset) {
  
      }
  
      @Override
      public void setContentLength(int len) {
  
      }
  
      @Override
      public void setContentLengthLong(long length) {
  
      }
  
      @Override
      public void setContentType(String type) {
  
      }
  
      @Override
      public void setBufferSize(int size) {
  
      }
  
      @Override
      public int getBufferSize() {
          return 0;
      }
  
      @Override
      public void flushBuffer() throws IOException {
  
      }
  
      @Override
      public void resetBuffer() {
  
      }
  
      @Override
      public boolean isCommitted() {
          return false;
      }
  
      @Override
      public void reset() {
  
      }
  
      @Override
      public void setLocale(Locale loc) {
  
      }
  
      @Override
      public Locale getLocale() {
          return null;
      }
  }
  
  ```

- StaticResourceProcessor类--处理静态资源

  ```java
  package com.lin.servletcontainer;
  
  import java.io.IOException;
  
  /**
   * 类 <code>{类名称}</code>{此类功能描述}
   *
   * @author zangbao.lin
   * @version 2020/1/13
   * @since JDK 1.8
   */
  
  public class StaticResourceProcessor {
  
      // 处理静态资源,直接返回静态文件内容
      public void process(Request request, Response response) {
          try {
              response.sendStaticResource();
          }
          catch (IOException e) {
              e.printStackTrace();
          }
      }
  }
  
  ```

- ServletProcessor1类--处理servlet

  ```java
  package com.lin.servletcontainer;
  
  import javax.servlet.Servlet;
  import javax.servlet.ServletRequest;
  import javax.servlet.ServletResponse;
  import java.io.File;
  import java.io.IOException;
  import java.net.URL;
  import java.net.URLClassLoader;
  import java.net.URLStreamHandler;
  
  /**
   * 类 <code>{类名称}</code>{此类功能描述}
   *
   * @author zangbao.lin
   * @version 2020/1/13
   * @since JDK 1.8
   */
  
  public class ServletProcessor1 {
  
      public void process(Request request, Response response) {
  
          // 获取URL地址
          String uri = request.getUri();
  
          // 获取servlet文件地址
          String servletName = uri.substring(uri.lastIndexOf("/") + 1);
  
  
          // 1.实例化URLClassLoader类,URLClassLoader用于加载jar包外的java类
          URLClassLoader loader = null;
          try {
              // create a URLClassLoader
              URL[] urls = new URL[1];
              URLStreamHandler streamHandler = null;
              File classPath = new File(Constants.WEB_ROOT);
  
              // 通过URL来创建一个仓库地址String
              String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString() ;
  
              urls[0] = new URL(null, repository, streamHandler);
  
              // URLClassLoader加载器是通过URL来实例化的
              loader = new URLClassLoader(urls);
          }
          catch (IOException e) {
              System.out.println(e.toString() );
          }
  
  
          // 2.类加载器,加载servlet类信息
          Class myClass = null;
          try {
              myClass = loader.loadClass(servletName);
          }
          catch (ClassNotFoundException e) {
              System.out.println(e.toString());
          }
  
  
  
          Servlet servlet = null;
          try {
              // 3.实例化servlet对象
              servlet = (Servlet) myClass.newInstance();
              // 4.调用servlet类的service()方法
              servlet.service(request,response);
          }
          catch (Exception e) {
              System.out.println(e.toString());
          }
          catch (Throwable e) {
              System.out.println(e.toString());
          }
  
      }
  }
  
  ```

  



