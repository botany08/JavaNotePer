## Tomcat简介

#### 1.Tomcat作为服务器

- Tomcat服务器 = Web服务器 + Servlet/JSP容器

  1. Web服务器的作用是接收客户端的请求，给客户端作出响应，且只能响应静态资源。
  2. JSP/Servlet容器的基本功能是把动态资源转换成静态资源。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Tomcat作为服务器.png)

- 动态资源

  1. 动态资源的特征是，可以动态生成HTML页面。比如数据的实时生成，通过绑定变量来实现。
  2. 静态资源是指，不可改变的HTML和CSS代码，如果要修改需要进入到服务器修改。

#### 2.Tomcat的架构

####2.1Tomcat目录结构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/2_Tomcat安装目录.png)

#### 2.2Tomcat配置文件与架构

- 逻辑结构图

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/3_Tomcat架构.png)

- 配置文件与逻辑结构对照

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_配置文件与逻辑结构.png)

  ```xml
  <!-- 配置文件解析 -->
  <?xml version="1.0" encoding="UTF-8"?>
  
  <!-- Server代表Tomcat容器,shutdown属性表示关闭Server的指令,port属性表示Server接收shutdown指令的端口号
  作用：提供一个接口让客户端能够访问到这个Service集合,同时维护所包含的Service的声明周期,
  包括如何初始化、如何结束服务、如何找到客户端要访问的Service -->
  <Server port="80" shutdown="SHUTDOWN">
    <Listener className="org.apache.catalina.startup.VersionLoggerListener"/>
    <Listener SSLEngine="on" className="org.apache.catalina.core.AprLifecycleListener"/>
    <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener"/>
    <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener"/>
    <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener"/>
  
    
    <GlobalNamingResources>
      <Resource auth="Container" description="User database that can be updated and saved"
                factory="org.apache.catalina.users.MemoryUserDatabaseFactory" name="UserDatabase"
                pathname="conf/tomcat-users.xml" type="org.apache.catalina.UserDatabase"/>
    </GlobalNamingResources>
  
    <!-- Srevice表示服务,在Connector和Engine外面包了一层,把它们组装在一起,对外提供服务.
    一个Service可以包含多个Connector，但是只能包含一个Engine.
    Tomcat可以提供多个Service，不同的Service监听不同的端口. -->
    <Service name="Catalina">
  
      <!-- Connector表示连接,用来监听端口,每一个连接都需要配置一个端口.
      作用：接收连接请求,创建Request和Response对象用于和请求端交换数据.
      分配线程让Engine来处理这个请求,并把产生的Request和Response对象传给Engine. -->
  
      <!-- HTTP/1.1协议,端口8080,
  	作用是处理Http请求.redirectPort表示当强制要求https而请求是http时,重定向至端口号为8443的
  	Connector.
  	Tomcat监听HTTP请求,使用的是8080端口,而不是正式的80端口.因为在生产环境中,很少将Tomcat直接对
  	外开放接收请求,一般在Tomcat和客户端之间加一层代理服务器(如nginx)，用于请求的转发、负载均衡、处理静态文
  	件等.通过代理服务器访问Tomcat时是在局域网中,因此一般仍使用8080端口. -->
      <Connector connectionTimeout="20000" port="8080" protocol="HTTP/1.1" redirectPort="8443"/>
  
  
      <!-- AJP/1.3协议,端口8009,一般负责和其他的HTTP服务器(如Apache)建立连接.
      之所以使用Tomcat和其他服务器集成,是因为Tomcat可以用作Servlet/JSP容器,但是对静态资源的处理速度较慢,不如
  	Apache和IIS等HTTP服务器.常常将Tomcat与Apache等集成,前者作Servlet容器,后者处理静态资源,而AJP协议便负
  	责Tomcat和Apache的连接. -->
      <Connector port="8009" protocol="AJP/1.3" redirectPort="8443"/>
  
      <!-- Engine表示Tomcat引擎,在Service组件中有且只有一个,Engine是Service组件中的请求处理组件.
      属性值:name属性用于日志和错误信息,在Server中是唯一的.defaultHost属性指定了默认的host名称,当发往本机的
  		  请求指定的host名称不存在时,一律使用defaultHost指定的host进行处理.
      作用：Engine组件从一个或多个Connector中接收请求并处理,并将完成的响应返回给Connector,最终传递给客户端.
      结构：Engine包含Host，Host包含Context.
      组件作用：一个Engine组件可以处理Service中的所有请求,一个Host组件可以处理发向一个特定虚拟主机的所有请求,
  			一个Context组件可以处理一个特定Web应用的所有请求. -->
      <Engine defaultHost="localhost" name="Catalina">
  
        <Realm className="org.apache.catalina.realm.LockOutRealm">
          <Realm className="org.apache.catalina.realm.UserDatabaseRealm" 
                 resourceName="UserDatabase"/>
        </Realm>
  
        <!-- Host表示主机
        结构：Engine组件中可以内嵌1个或多个Host组件.每个Host组件代表Engine中的一个虚拟主机.
        作用：运行多个Web应用(一个Context代表一个Web应用),并负责安装、展开、启动和结束每个Web应用.
        属性：unpackWARs指定了是否将代表Web应用的WAR文件解压.如果为true,通过解压后的文件结构运行该Web应用.
  		如果为false,直接使用WAR文件运行Web应用.autoDeploy和appBase属性,与Host内Web应用的自动部署有关.
        主机名：Host组件代表的虚拟主机,对应了服务器中一个网络名实体(如”www.test.com”,或IP地址
  			”116.25.25.25”),域名需要在DNS服务器注册.
        寻找主机过程：客户端通过主机名(IP)链接到对应的服务器,通过端口选择对应的Connector.当Tomcat接收请求进
  				入到Engine处理时,提取出主机名匹配对应的Host.如果都不匹配则交由defaultHost处理. -->
        <Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true">
          <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs" 
                 pattern="%h %l %u %t &quot;%r&quot; %s %b" prefix="localhost_access_log" 
                 suffix=".txt"/>
  
          <!-- Context元素代表在特定虚拟主机上运行的一个Web应用,每个Web应用基于WAR文件或WAR文件解压后对应的
  			目录(应用目录).
          结构：Context是Host的子容器,每个Host中可以定义任意多的Context元素.
          配置的省略：没有出现Context元素的配置,因为Tomcat开启了自动部署,Web应用没有在server.xml中配置静态
  				 部署,而是由Tomcat通过特定的规则自动部署. -->
  
          <!-- Host的自动部署
          开启条件：如果deployOnStartup或autoDeploy设置为true，则tomcat启动自动部署.
          作用：当检测到新的Web应用或Web应用的更新时,会触发应用的部署(或重新部署),不需要重启服务器.
          区别：deployOnStartup为true时,Tomcat在启动时检查Web应用,且检测到的所有Web应用视作新应用.
  			autoDeploy为true时,Tomcat在运行时定期检查新的Web应用或Web应用的更新.
          依赖：自动部署依赖于检查是否有新的或更改过的Web应用,而Host元素的appBase和xmlBase设置了检查Web应用			更新的目录.
          appBase属性：指定Web应用所在的目录,默认值是webapps,这是一个相对路径,代表Tomcat根目录下webapps文
  					件夹.
          xmlBase属性：指定Web应用的XML配置文件所在的目录,默认值为conf/<engine_name>/<host_name>,主机
  					localhost的xmlBase的默认值是$TOMCAT_HOME/conf/Catalina/localhost.
          Web应用的结构：XML配置文件,WAR包,以及一个应用目录.XML配置文件位于xmlBase指定的目录,WAR包和应用目
  					录位于appBase指定的目录.
          Tomcat检查应用更新的顺序：扫描虚拟主机指定的xmlBase下的XML配置文件,扫描虚拟主机指定的appBase下的
  			WAR文件,扫描虚拟主机指定的appBase下的应用目录. -->
  
        </Host>
      </Engine>
  
    </Service>
  </Server>
  ```

  



