## Session、Cookie和Token

### 1.Session和Cookie

#### 1.1HTTP协议

- http是一个无状态协议，就是说这一次请求和上一次请求是没有任何关系的，互不认识的，没有关联的。

- 好处，访问加载速度比较快。

- 缺点，假如想要把www.zhihu.com/login.html和www.zhihu.com/index.html关联起来，必须使用某些手段和工具。

  

#### 1.2客户端访问服务器流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/6_session和cookie.png)

1. 首先，客户端会发送一个http请求到服务器端。

2. 服务器端接受客户端请求后，建立一个session，并发送一个http响应到客户端，这个响应头，其中就包含Set-Cookie头部。该头部包含了sessionId。

   ```java
   //Set-Cookie格式如下
   Set-Cookie: value;expires=date;domain=domain;path=path;secure
   ```

3. 在客户端发起的第二次请求，假如服务器给了Set-Cookie，浏览器会自动在请求头中添加cookie。

4. 服务器接收请求，分解cookie，验证信息。取得对应的session，核对成功后返回response给客户端。



#### 1.3Session和Cookie关系

1. **cookie只是实现session的其中一种方案**。是最常用的但并不是唯一的方法。禁用cookie后还有其他方法存储，比如放在url中。
2. 现在大多都是session+cookie，但是只用session不用cookie，或是只用cookie不用session在理论上都可以保持会话状态。可是实际中因为多种原因，一般不会单独使用。
3. 用session只需要在客户端保存一个id，实际上大量数据都是保存在服务端。如果全部用cookie，数据量大的时候客户端是没有那么多空间的。
4. 如果只用cookie不用session，那么账户信息全部保存在客户端，一旦被劫持，全部信息都会泄露。并且客户端数据量变大，网络传输的数据量也会变大
5. session有如用户信息档案表，里面包含了用户的认证信息和登录状态等信息，而cookie就是用户通行证。



### 2.Token

#### 2.1Token基本定义

1. token也称作令牌，由uid+time+sign[+固定参数]组成。
2. token的认证方式类似于临时的证书签名，并且是一种服务端无状态的认证方式，适合于RESTAPI的场景。所谓无状态就是服务端并不会保存身份认证相关的数据。

#### 2.2组成和存放

- Token组成部分
  1. uid: 用户唯一身份标识
  2. time: 当前时间的时间戳
  3. sign: 签名, 使用 hash/encrypt 压缩成定长的十六进制字符串，以防止第三方恶意拼接
  4. 固定参数(可选): 将一些常用的固定参数加入到 token 中是为了避免重复查库
- Token存放位置
  1. 在客户端一般存放于localStorage，cookie，或sessionStorage中。
  2. 在服务器一般存于数据库中。

####2.3Token认证流程

1. 用户登录，成功后服务器返回Token给客户端。
2. 客户端收到数据后保存在客户端。
3. 客户端再次访问服务器，将token放入headers中。
4. 服务器端采用filter过滤器校验。校验成功则返回请求数据，校验失败则返回错误码。

### 3.CSRF跨站请求伪造

#### 3.1浏览器的同源策略

- 基本定义
  A网页设置的Cookie，B网页不能打开，除非这两个网页同源。所谓同源指的是三个相同：`协议，域名，端口`

- 非同源，共有三种行为受到限制

  1. 无法获取非同源网页的cookie、localstorage和indexedDB。
  2. 无法访问非同源网页的DOM(iframe)。
  3. 无法向非同源地址发送AJAX请求或fetch请求(可以发送，但浏览器拒绝接受响应)。
  
  
#### 3.2Token和Session+Cookie应对CSRF

- **应用场景**

  假如用户正在登陆银行网页，同时登陆了攻击者的网页，并且银行网页未对csrf攻击进行防护。攻击者就可以在网页放一个表单，该表单提交src为http://www.bank.com/api/transfer，body为count=1000&to=Tom。

- **Seesion+Cookie**

  用户打开网页的时候就已经转给Tom1000元了。因为form发起的POST请求并不受到浏览器同源策略的限制，因此可以任意地使用其他域的Cookie向其他域发送POST请求，形成CSRF攻击。在post请求的瞬间，cookie会被浏览器自动添加到请求头中。

- **Token**

  token是开发者为了防范csrf而特别设计的令牌，浏览器不会自动添加到headers里，攻击者也无法访问用户的token，所以提交的表单无法通过服务器过滤，也就无法形成攻击。



### 4.分布式架构的session和token

- session
  1. session是有状态的(是对象需要序列化)，一般存于服务器内存或硬盘中，当服务器采用分布式或集群时，session就会面对负载均衡问题。
  2. 负载均衡多服务器的情况，不好确认当前用户是否登录，因为多服务器不共享session。可以使用redis解决共享session。
- token
  1. token是无状态的，token字符串里就保存了所有的用户信息。
  2. 客户端登陆传递信息给服务端，服务端收到后把用户信息加密(token)传给客户端，客户端将token存放于localStroage等容器中。客户端每次访问都传递token，服务端解密token，就能获取用户信息。
  3. 通过cpu加解密，服务端就不需要存储session占用存储空间，就可以解决负载均衡多服务器的问题，这种方式叫做JWT(JsonWebToken)

### 5.概念总结

1. session存储于服务器，可以理解为一个状态列表，拥有一个唯一识别符号sessionId，通常存放于cookie中。服务器收到cookie后解析出sessionId，再去session列表中查找相应的session。
2. cookie类似一个令牌，装有sessionId，存储在客户端，浏览器通常会自动添加。
3. token也类似一个令牌，无状态，用户信息都被加密到token中，服务器收到token后解密就可以获取用户信息，需要开发者手动添加。
4. jwt只是一个跨域认证的方案。

  



