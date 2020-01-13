## 简单实现Web服务器

### 1.Http协议

- HTTP使用可靠的TCP连接，TCP协议默认使用80端口。
- 在HTTP 中，总是由客户端通过建立连接并发送HTTP 请求来初始化一个事务的。
- 客户端或服务器端可提前关闭连接。

#### 1.1请求报文

- 基本格式

  一个HTTP 请求包含以下三部分：

  1. 请求方法一一统一资源标识符（Uniform Resource Identifier, URI）一一协议／版本
  2. 请求头
  3. 实体

- 实例

  ```java
  // POST 表示请求方法,  /examples/default.jsp 表示URI,  HTTP/1.1 表示协议/版本
  POST /examples/default.jsp HTTP/1.1
      
  // 请求头包含客户端环境和请求实体正文的相关信息,基本语法为  key:value 
  Accept: text/plain ; text/html
  Accept-Language: en-gb
  Connection: Keep-Alive
  Host: localhost
  User-Agent: Mozilla/4.0 (compatible; MSIE 4.01; Windows 98)
  Content-Length: 33
  Content-Type: application/x-www-form-urlencoded
  Accept-Encoding: gzip, deflate
  
  // 在请求头和请求实体正文之间有一个空行,该空行只有CRLF符(回车/换行)
  lastName=Franks&firstName=Michael
  ```

#### 1.2响应报文

- 基本格式

  与HTTP 请求类似，HTTP 响应也包括三部分：

  1. 协议一一状态码一一描述
  2. 响应头
  3. 响应实体段

- 实例

  ```java
  // HTTP/1.1 表示协议,200表示状态码,OK表示描述
  HTTP/1.1 200 OK
  
  // 响应头和请求头格式一样
  Server: Microsoft-IIS/4.0
  Date: Mon, 5 Jan 2004 13:13:33 GMT
  Content-Type : text/html
  Last-Modified: Mon, 5 Jan 2004 13:13:12 GMT
  Content-Length : 112
  
  // 响应头和响应实体同样隔着一个CRLE符(回车/换行)
  <html>
  <head>
  <title>HTTP Response Example</title>
  </head>
  <body>
  Welcome to Brainy Software
  </body>
  </html>
  ```

### 2.Web服务器实现

- HttpServer 类

  ```java
  package com.lin.httpserverex;
  
  import java.io.File;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import java.net.InetAddress;
  import java.net.ServerSocket;
  import java.net.Socket;
  
  public class HttpServer {
  
      // 用户文件夹
      public static final String WEB_ROOT =
              System.getProperty("user.dir") + File.separator  + "webroot";
  
      // 关闭命令
      private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
  
      // 是否接收到关闭命令
      private boolean shutdown = false;
  
      // Web服务器启动方法
      public static void main(String[] args) {
  
          HttpServer server = new HttpServer();
          server.await();
      }
  
      // 服务器保持监听状态
      public void await() {
          ServerSocket serverSocket = null;
          int port = 8080;
          try {
              // 监听8080端口
              serverSocket =  new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
          }
          catch (IOException e) {
              e.printStackTrace();
              System.exit(1);
          }
  
          // 如果没有接收到关闭命令
          while (!shutdown) {
              Socket socket = null;
              InputStream input = null;
              OutputStream output = null;
  
              try {
                  socket = serverSocket.accept();
                  input = socket.getInputStream();
                  output = socket.getOutputStream();
  
                  // 封装请求对象
                  Request request = new Request(input);
                  request.parse();
  
                  // 封装响应对象
                  Response response = new Response(output);
                  response.setRequest(request);
                  response.sendStaticResource();
  
                  // 关闭socket
                  socket.close();
  
                  // 更新服务器状态-是否关闭
                  System.out.println("URI:"+request.getUri());
                  shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
              }
              catch (Exception e) {
                  e.printStackTrace();
                  continue;
              }
          }
      }
  
  }
  
  ```

  

- Request 类

  ```java
  package com.lin.httpserverex;
  
  import java.io.IOException;
  import java.io.InputStream;
  
  public class Request {
  
      private InputStream input;
      private String uri;
  
      public Request(InputStream input) {
          this.input = input;
      }
  
      public String getUri() {
          return uri;
      }
  
  
      public void parse() {
          // Read a set of characters from the socket
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
  
  }
  
  ```

  

- Response 类

  ```java
  package com.lin.httpserverex;
  
  import java.io.*;
  
  public class Response {
  
      private static final int BUFFER_SIZE = 1024;
      Request request;
      OutputStream output;
  
      public Response(OutputStream output) {
          this.output = output;
      }
  
      public void setRequest(Request request) {
          this.request = request;
      }
  
  	
      // 将静态文件内容写入到输出流
      public void sendStaticResource() throws IOException {
          // 初始化字节数组
          byte[] bytes = new byte[BUFFER_SIZE];
  
          // 文件读入流
          FileInputStream fis = null;
  
          try {
              // 初始化URI地址的文件对象
              File file = new File(HttpServer.WEB_ROOT, request.getUri());
  
              // 文件存在
              if (file.exists()) {
  
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
              else {
                  // file not found
                  String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                          "Content-Type: text/html\r\n" +
                          "Content-Length: 23\r\n" +
                          "\r\n" +
                          "<h1>File Not Found</h1>";
                  output.write(errorMessage.getBytes());
              }
          }
          catch (Exception e) {
              // thrown if cannot instantiate a File object
              System.out.println(e.toString() );
          }
          finally {
              if (fis!=null) {
                  fis.close();
              }
          }
      }
  }
  
  ```





