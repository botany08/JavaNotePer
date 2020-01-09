## Cookie和Session

### 1.会话机制

- 出现背景
  1. 客户端与服务器的通讯都是通过HTTP协议，HTTP是一种无状态协议 ，浏览器的每一次请求都是完全孤立的。
  2. 出现的问题是，每跳转一个页面都要登录一遍。
- 定义及作用
  1. 会话机制最主要的目的是，标识用户及跟踪状态，实现将多个请求归属于一个用户(会话)。
  2. 会话的基本原则是双方共存，即cookie与session同在。如果浏览器关闭，则cookie失效。如果服务器会话时间过长导致超时，则session失效。

### 2.Cookie简介

- 定义及作用

  1. 当浏览器第一次访问服务器时，服务器会返回一份Cookie数据给浏览器。
  2. 浏览器将Cookie存入缓存。
  3. 当浏览器再次访问服务器时，会在请求头中带入Cookie数据。当浏览器关闭时，Cookie会失效。
  4. 服务器通过请求中的Cookie，判断客户端的状态。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/16_服务器和浏览器的Cookie交互.png)

  

- 服务器发送Cookie到浏览器的方式

  1. 在Servlet中，通过Respose写入Cookie值。

  2. Tomcat将Respose转换为响应报文数据。

     

- 服务器从浏览器获取Cookie的方式

  1. Tomcat将请求报文数据，解析封装成Request对象。

  2. 在Servelt中，通过Request获取Cookie值。

     

- Cookie的两种类型

  1. 会话Cookie (Session Cookie) 

     默认情况下，会话Cookie被保存在浏览器的内存中，当浏览器关闭就会失效。

  2. 持久性Cookie (Persistent Cookie)

     通过设置Cookie的MaxAge属性，当其大于0时，Cookie会被保存在硬盘当中，即使浏览器关闭也不会失效。



### 3.Session简介

- 定义以及作用
  1. Session是服务端应用的一个对象， 本质上类似于一个大Map，里面的内容以键值对的形式存储。 
  2. Cookie是存储在客户端，Session则是存储在服务端，可以用来存储比较私密的信息。
  3. 每一个会话对应一个Session，客户数据存储在Session中，将JSESSIONID放入Cookie传回客户端。客户端可以凭借Cookie中的JSESSIONID，找到对应的那个Session。
  4. Session有个默认最大不活动时间，30分钟(可在配置文件中修改数值)。如果超过这个时间，服务器应用中的Session对象就会被销毁。
- Session序列化 
  1. 序列化是指，将内存中的Session对象按照一定的格式，存储在本地硬盘中。
  2. 反序列化是指，将本地硬盘中的Session信息，按照规则重新读取为内存中的Session对象。
  3. 主要应用在，当服务器重启或者应用异常停止时，防止丢失Session信息。
- Session的钝化和活化
  1. 如果一个Session长时间无人访问，为了减少内存占用，会被钝化(序列化)到磁盘上。 
  2. 当该Session再次被访问时，才会被活化(反序列化)。 

