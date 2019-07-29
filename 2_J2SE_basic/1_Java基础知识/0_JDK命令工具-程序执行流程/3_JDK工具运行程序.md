## JRE bin目录下工具说明

- **java：Java解释器，直接从类文件执行Java应用程序代码 。**
- **javac：Java编译器，将Java源码转换成字节码。**
- **jar：多用途的存档及压缩工具，是个java应用程序，可将多个文件合并为单个JAR归档文件。**
- **javadoc：根据Java源代码及其说明语句生成的HTML文档** 
- appletviewer(小程序浏览器)：一种执行HTML文件上的Java小程序类的Java浏览器 
- jdb：Java调试器，可以逐行地执行程序、设置断点和检查变量 
- javah：产生可以调用Java过程的C过程，或建立能被Java程序调用的C过程的头文件 
- Javap：Java反汇编器，显示编译类文件中的可访问功能和数据，同时显示字节代码含义 
- htmlConverter——命令转换工具。 
- native2ascii——将含有不是Unicode或Latinl字符的的文件转换为Unicode编码字符的文件。 
- serialver——返回serialverUID。语法：serialver [show] 命令选项show是用来显示一个简单的界面。输入完整的类名按Enter键或"显示"按钮，可显示serialverUID。 

## JDK bin目录下工具说明

### 标准工具

- 这些工具都是JDK提供的，通常都是长期支持的工具，JDK承诺这些工具比较好用。不同系统、不同版本之间可能会有差异，但是不会突然就有一个工具消失。

####1. 基础

| 工具         | 描述                                                         |
| ------------ | ------------------------------------------------------------ |
| appletviewer | 在没有web浏览器的时候运行和调试applet，执行HTML文件上的Java小程序类的Java浏览器。 |
| extcheck     | 检查Jar冲突的工具，检测目标 jar 文件与当前安装方式扩展 jar 文件间的版本冲突。 |
| jar          | 创建和管理Jar文件，多用途的存档及压缩工具，可将多个文件合并为单个JAR归档文件。 |
| java         | Java运行工具，用于运行.class字节码文件或.jar文件。Java解释器，直接从类文件执行Java应用程序代码。 |
| javac        | 用于Java编程语言的编译器，将Java源码转换成字节码。           |
| javadoc      | API文档生成器，根据Java源代码及其说明语句生成的HTML文档。    |
| javah        | C头文件和stub函数生成器，用于编写native方法。生成可以调用Java过程的C过程，或建立能被Java程序调用的C过程的头文件。 |
| javap        | 类文件反汇编器，主要用于根据Java字节码文件反汇编为Java源代码文件。 |
| jdb          | Java调试器(Java Debugger)，可以逐行地执行程序、设置断点和检查变量。 |
| jdeps        | Java类依赖性分析器。                                         |

####2. 安全

| 工具       | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| keytool    | 管理密钥库和证书。主要用于获取或缓存Kerberos协议的票据授权票据。允许用户查看本地凭据缓存和密钥表中的条目(用于Kerberos协议)。Kerberos密钥表管理工具，允许用户管理存储于本地密钥表中的主要名称和服务密钥。 |
| jarsigner  | 生成并验证JAR签名，为 Java 归档 (JAR) 文件产生签名，并校验已签名的 JAR 文件的签名。 |
| policytool | 管理策略文件的GUI工具，用于管理用户策略文件(.java.policy)    |

####3. 国际化/i18n

| 工具         | 描述                                                         |
| ------------ | ------------------------------------------------------------ |
| native2ascii | 本地编码到ASCII编码的转换器(Native-to-ASCII Converter)，用于“任意受支持的字符编码”和与之对应的“ASCII编码和(或)Unicode转义”之间的相互转换。 |

####4. 远程方法调用/RMI 

| 工具        | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| rmic        | Java RMI 编译器，为使用JRMP或IIOP协议的远程对象生成stub、skeleton、和tie类，也用于生成OMG IDL。 |
| rmiregistry | 远程对象注册表服务，用于在当前主机的指定端口上创建并启动一个远程对象注册表。 |
| rmid        | 启动激活系统守护进程，允许在虚拟机中注册或激活对象。         |
| serialver   | 生成并返回指定类的序列化版本ID，用于返回一个类的serialverUID。 |

####5. Java IDL 与 RMI-IIOP

| 工具       | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| tnameserv  | 提供对命名服务的访问                                         |
| idlj       | IDL转Java编译器(IDL-to-Java Compiler)，生成映射OMG IDL接口的.java文件，并启用以Java编程语言编写的使用CORBA功能的应用程序的.java文件。IDL意即接口定义语言(Interface Definition Language)。 |
| orbd       | 对象请求代理守护进程(Object Request Broker Daemon)，提供从客户端查找和调用CORBA环境服务端上的持久化对象的功能。使用ORBD代替瞬态命名服务tnameserv。ORBD包括瞬态命名服务和持久命名服务。ORBD工具集成了服务器管理器，互操作命名服务和引导名称服务器的功能。当客户端想进行服务器时定位，注册和激活功能时，可以与servertool一起使用。 |
| servertool | 为应用程序注册，注销，启动和关闭服务器提供易用的接口         |

####6. Java 发布工具

| 工具         | 描述                                                         |
| ------------ | ------------------------------------------------------------ |
| javapackager | 打包、签名Java和JavaFX应用程序                               |
| pack200      | 使用Java gzip压缩器将JAR文件转换为压缩的pack200文件。压缩的压缩文件是高度压缩的JAR，可以直接部署，节省带宽并减少下载时间。 |
| unpack200    | 将pack200生成的打包文件解压提取为JAR文件                     |

####7. Java Web 启动工具

| 工具   | 描述                                   |
| ------ | -------------------------------------- |
| javaws | 启动Java Web Start并设置各种选项的工具 |

####8. 故障排查分析，监控和管理

| 工具      | 描述                                                         |
| --------- | ------------------------------------------------------------ |
| jcmd      | JVM诊断命令工具，将诊断命令请求发送到正在运行的Java虚拟机。  |
| jconsole  | 用于监控Java虚拟机的使用JMX规范的图形工具。它可以监控本地和远程JVM。它还可以监控和管理应用程序，不过此JVM需要使用可管理的模式启动。 |
| jmc       | Java任务控制客户端（JMC，Java Mission Control），包含用于监控和管理Java应用程序的工具，而不会引入与这些工具相关联的性能开销。开发者可以使用jmc命令来创建JMC工具。 |
| jvisualvm | 一种图形化工具，可在Java虚拟机中运行时提供有关基于Java技术的应用程序（Java应用程序）的详细信息。 Java VisualVM提供内存和CPU分析，堆转储分析，内存泄漏检测，MBean访问和垃圾收集。 |

####9. WebService工具 

| 工具      | 描述                                                         |
| --------- | ------------------------------------------------------------ |
| schemagen | 用于XML绑定的Schema生成器，用于生成XML schema文件。          |
| wsgen     | XML Web Service 2.0的Java API，生成用于JAX-WS Web Service的JAX-WS便携式产物。 |
| wsimport  | XML Web Service 2.0的Java API，主要用于根据服务端发布的wsdl文件生成客户端 |
| xjc       | 主要用于根据XML schema文件生成对应的Java类。                 |

   

### 实验性工具

- 所谓的实验性工具，就是HotSpot JDK提供了，但是可能在之后的某个版本中突然就不可用了。通过man命令查看这些工具的详细介绍的时候，都会在第一行有This command is experimental and unsupported.这么一句。HotSpot JDK再三强调要谨慎使用，但是由于这些工具在Java性能调优方面作用太大，所以我们就谨慎的研究然后谨慎的使用吧。

####1. 监控

| 工具   | 描述                                                         |
| ------ | ------------------------------------------------------------ |
| jps    | JVM进程状态工具(JVM Process Status Tool)，在目标系统上列出HotSpot Java虚拟机进程的描述信息 |
| jstat  | JVM统计监控工具(JVM Statistics Monitoring Tool)，根据参数指定的方式收集和记录指定的jvm进程的性能统计信息。 |
| jstatd | JVM jstat守护程序，启动一个RMI服务器应用程序，用于监视测试的HotSpot Java虚拟机的创建和终止，并提供一个界面，允许远程监控工具附加到在本地系统上运行的Java虚拟机。 |

####2. 故障排查

| 工具      | 描述                                                         |
| --------- | ------------------------------------------------------------ |
| jinfo     | Java的配置信息工具(Java Configuration Information)，实时查看和调整JVM配置参数。用于打印指定Java进程、核心文件或远程调试服务器的配置信息。 |
| jhat      | Java堆分析工具(Java Heap Analysis Tool)，用于分析Java堆内存中的对象信息。jhat用来分析有jmap -dump产生的heapdump文件。 |
| jmap      | Java内存映射工具(Java Memory Map)，主要用于打印指定Java进程、核心文件或远程调试服务器的共享对象内存映射或堆内存细节。生成虚拟机的内存转储快照，生成heapdump文件。 |
| jsadebugd | 适用于Java的可维护性代理调试守护程序(Java Serviceability Agent Debug Daemon)，主要用于附加到指定的Java进程、核心文件，或充当一个调试服务器。 |
| jstack    | Java的堆栈跟踪工具，主要用于打印指定Java进程、核心文件或远程调试服务器的Java线程的堆栈跟踪信息。 |

####3. 脚本工具 

| 工具       | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| jjs        | 对Nashorn引擎的调用。Nashorn是基于Java实现一个轻量级高性能的JavaScript运行环境。 |
| jrunscript | Java命令行脚本外壳工具(command line script shell)，主要用于解释执行javascript、groovy、ruby等脚本语言。 |



## 重要的命令

#### 1.jps命令

作用：查看当前JAVA程序的进程

语法格式：jps  [ options ]   [ hostid 进程ID] 

| 选项 | 作用                                                         |
| ---- | ------------------------------------------------------------ |
| -q   | 仅输出VM标识符，不包括class name,jar name,arguments in main method |
| -m   | 输出main method的参数                                        |
| -l   | 输出完全的包名，应用主类名，jar的完全路径名                  |
| -v   | 输出jvm参数                                                  |
| -V   | 输出通过flag文件传递到JVM中的参数(.hotspotrc文件或-XX:Flags=所指定的文件) |
| -J   | 传递参数到vm，例如：-J -Xms48m                               |

```shell
[root@t-kfzx-2 ~]# jps -lv
1348 sun.tools.jps.Jps -Dapplication.home=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.191.b12-0.el7 -Xms8m
26389 nretail-tlink-server-1.0.0-SNAPSHOT.jar -Xms1g -Xmx1g
14856 nretail-tlink-manage-1.0.0-SNAPSHOT.jar -Xms1g -Xmx1g
```

#### 2.jstat命令

作用：用于监控虚拟机各种运行状态信息的命令行工具。可以显示本地或远程虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据，在没有GUI图形的服务器上，它是运行期定位虚拟机性能问题的首选工具。

语法格式：jstat  [ option ]    [ vmid 进程ID ]   [ interval 查询间隔时间(可省)]   [ count 查询次数(可省)]

| 选项              | 作用                                                         |
| ----------------- | ------------------------------------------------------------ |
| -class            | 显示加载class的数量，及所占空间等信息。                      |
| -compiler         | 显示VM实时编译的数量等信息。                                 |
| -gc               | 可以显示gc的信息，查看gc的次数，及时间。其中最后五项，分别是young gc的次数，young gc的时间，full gc的次数，full gc的时间，gc的总时间。 |
| -gccapacity       | 可以显示，VM内存中三代（young,old,perm）对象的使用和占用大小。PGCMN显示的是最小perm的内存使用量，PGCMX显示的是perm的内存最大使用量，PGC是当前新生成的perm内存占用量，PC是但前perm内存占用量。其他的可以根据这个类推， OC是old内纯的占用量。 |
| -gccause          | 最近一次GC统计和原因                                         |
| -gcnew            | new对象的信息。                                              |
| -gcnewcapacity    | new对象的信息及其占用量。                                    |
| -gcold            | old对象的信息。                                              |
| -gcoldcapacity    | old对象的信息及其占用量。                                    |
| -gcpermcapacity   | perm对象的信息及其占用量。                                   |
| -gcutil           | 统计gc信息统计。                                             |
| -printcompilation | 当前VM执行的信息。                                           |

```shell
##显示new对象的信息，每隔250毫秒打印一次，总共打印5次。
[root@t-kfzx-2 ~]# jstat -gcnew 26389 250 5
 S0C    S1C    S0U    S1U   TT MTT  DSS      EC       EU     YGC     YGCT  
2048.0 2048.0   64.0    0.0 15  15 2048.0 345088.0 296079.3     68    0.631
2048.0 2048.0   64.0    0.0 15  15 2048.0 345088.0 296079.3     68    0.631
2048.0 2048.0   64.0    0.0 15  15 2048.0 345088.0 296079.3     68    0.631
2048.0 2048.0   64.0    0.0 15  15 2048.0 345088.0 296079.3     68    0.631
2048.0 2048.0   64.0    0.0 15  15 2048.0 345088.0 296079.3     68    0.631
```

#### 3.jinfo命令

作用：实时查看和调整JVM配置参数，动态修改JVM参数。

语法格式：jinfo [ option ]   [ vmid 进程ID] 

| 选项                             | 作用                               |
| -------------------------------- | ---------------------------------- |
| -flags                           | 查看所有JVM参数                    |
| -flag  <参数名称>  PID           | 查看指定的JVM参数                  |
| -flag  < +或 - > <参数名称>  PID | 调整 **布尔类型 **的JVM参数        |
| -flag   <参数名称>=<参数值>  PID | 调整 **数字/字符串类型** 的JVM参数 |

```

```

####4.