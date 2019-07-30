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
| jmap      | （堆dump）Java内存映射工具(Java Memory Map)，主要用于打印指定Java进程、核心文件或远程调试服务器的共享对象内存映射或堆内存细节。生成虚拟机的内存转储快照，生成heapdump文件。 |
| jstack    | （栈dump）Java的堆栈跟踪工具，主要用于打印指定Java进程、核心文件或远程调试服务器的Java线程的堆栈跟踪信息。 |
| jsadebugd | 适用于Java的可维护性代理调试守护程序(Java Serviceability Agent Debug Daemon)，主要用于附加到指定的Java进程、核心文件，或充当一个调试服务器。 |

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

```shell
##显示PID为26389的JVM CICompilerCount参数
[root@t-kfzx-2 ~]# jinfo -flag CICompilerCount 26389
-XX:CICompilerCount=2
```

####4.jmap和jstack命令

dump基本概念：在故障定位(尤其是out of memory)和性能分析的时候，会通过dump文件来排除代码问题。dump文件记录了JVM运行期间的内存占用、线程执行等情况。常用的有heap dump和thread dump（也叫javacore，或java dump）。heap dump记录内存信息的，thread dump是记录CPU信息的。

- **heap dump**： heap dump文件是一个二进制文件，保存了某一时刻JVM堆中对象使用情况。HeapDump文件是指定时刻的Java堆栈的快照，是一种镜像文件。Heap Analyzer工具通过分析HeapDump文件，哪些对象占用了太多的堆栈空间，来**发现导致内存泄露或者可能引起内存泄露的对象**。

-  **thread dump**： thread dump文件主要保存的是java应用中各线程在某一时刻的运行的位置，即执行到哪一个类的哪一个方法哪一个行上。thread dump是一个文本文件，打开后可以看到每一个线程的执行栈，以stacktrace的方式显示。主要是线程在一个时间段内的执行情况，两个thread dump文件在分析时特别有效，困为它可以看出在先后两个时间点上，线程执行的位置，如果发现先后两组数据中同一线程都执行在同一位置，则说明此处可能有问题，因为程序运行是极快的，如果两次均在某一点上，说明这一点的耗时是很大的。

1. jmap命令

   作用：打印JVM的heap dump文件，二进制格式。

   语法：jmap  [ option ]    [ PID ]

   | 选项                                     | 作用                                                         |
   | ---------------------------------------- | ------------------------------------------------------------ |
   | -dump:[live,] format=b , file=<filename> | 使用hprof二进制形式，输出jvm的heap内容到文件 [live] 子选项是可选的，假如指定live选项，那么只输出活的对象到文件。 |
   | -finalizerinfo                           | 打印正等候回收的对象的信息。                                 |
   | -heap                                    | 打印heap的概要信息，GC使用的算法，heap的配置及wise heap的使用情况。 |
   | -histo[:live]                            | 打印每个class的实例数目，内存占用，类全名信息。 VM的内部类名字开头会加上前缀”*”.。如果live子参数加上后，只统计活的对象数量。 |
   | -permstat                                | 打印classload和jvm heap持久层的信息.。包含每个classloader的名字，活泼性，地址，父classloader和加载的class数量.。另外内部String的数量和占用内存数也会打印出来。 |
   | -F                                       | 强迫在pid没有相应的时候使用-dump或者-histo参数，在这个模式下，live子参数无效。 |
   | -J                                       | 传递参数给jmap启动的jvm。                                    |

   ```shell
   ##打印堆中每个class的相关信息
   [root@t-kfzx-2 ~]# jmap -histo 26389
    num     #instances         #bytes  class name
   ----------------------------------------------
      1:       1849247       59175904  java.util.concurrent.locks.AbstractQueuedSynchronizer$Node
      2:        115314       15676056  [C
      3:         42676        3755488  java.lang.reflect.Method
      4:          8651        3741928  [I
      5:         15690        2940624  [B
   
   ##生成heap dump文件
   [root@t-kfzx-2 ~]# jmap -dump:live,format=b,file=heap.hrof 26389
   Dumping heap to /root/heap.hrof ...
   Heap dump file created
   
   ##分析heap dump文件，直接打开浏览器访问 IP：5000 就可以看到
   [root@t-kfzx-2 ~]# jhat -port 5000 /root/heap.hrof 
   Reading from /root/heap.hrof...
   Dump file created Tue Jul 30 02:09:44 UTC 2019
   Snapshot read, resolving...
   Resolving 663586 objects...
   Chasing references, expect 132 dots........................
   Eliminating duplicate references................................
   Snapshot resolved.
   Started HTTP server on port 5000
   Server is ready.
   
   ```

   

2. jstack命令

   作用：打印JVM的heap dump文件，文本格式。

   语法：jstack  [ option ]   [ PID ]

   | 选项 | 作用                                                         |
   | ---- | ------------------------------------------------------------ |
   | -l   | 长列表打印关于锁的附加信息，例如属于java.util.concurrent 的 ownable synchronizers列表。 |
   | -F   | 当 jstack [-l] pid 没有相应的时候强制打印栈信息。            |
   | -m   | 打印java和native c/c++框架的所有栈信息。                     |

   ```shell
   ##将线程信息输入到 文件thread.txt
   [root@t-kfzx-2 ~]# jstack -l 26389 > thread.txt
   ```

#### 5.jhat命令

作用：用来分析jmap生成的heap dump文件，以html的形式展示出来。

语法：jhat   [ option ]     [ filename ]

| 选项       | 作用                                                         |
| ---------- | ------------------------------------------------------------ |
| -port      | 指定输出的端口号                                             |
| -J <flag>  | 将运行时参数传递给运行jhat的JVM。例如，`-J-Xmx512m`设置使用的最大堆内存大小为512MB。 |
| -debug int | 设置此工具的调试级别。0意味着没有调试输出。设置的值越高，输出的信息就越详细。 |

```shell
##分析heap dump文件，直接打开浏览器访问 IP：5000 就可以看到
[root@t-kfzx-2 ~]# jhat -port 5000 /root/heap.hrof 
Reading from /root/heap.hrof...
Dump file created Tue Jul 30 02:09:44 UTC 2019
Snapshot read, resolving...
Resolving 663586 objects...
Chasing references, expect 132 dots........................
Eliminating duplicate references................................
Snapshot resolved.
Started HTTP server on port 5000
Server is ready.
```

#### 6.java命令

作用：java运行工具，直接从class文件执行java程序代码。

语法：java  [ option ]   [ filename 源文件]

参数列表：java -help 可以获取基本参数列表。java -X 可以获取扩展参数列表。

- 基本参数

| 选项                                                         | 作用                                                         |
| :----------------------------------------------------------- | :----------------------------------------------------------- |
| -client  -server                                             | 用于设置虚拟机使用何种运行模式，client 模式启动比较快，但运行时性能和内存管理效率不如 server 模式，server 模式启动比 client 慢，但可获得更高的运行性能。在 windows上，缺省的虚拟机类型为 client 模式，在 Linux，Solaris 上缺省采用 server模式。对服务器端应用，推荐采用 server 模式。 |
| -hotspot                                                     | 含义与 client 相同，jdk1.4 以前使用的参数，jdk1.4 开始不再使用，代之以 client。 |
| -cp                                                          | 运行Java要使用类的全名来运行。如果遇到文件夹，则需要-cp设置到顶级包下面。 |
| -classpath lllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll | 使用-classpath 后虚拟机将不再使用 CLASSPATH 中的类搜索路径，如果-classpath 和 CLASSPATH 都没有设置，则虚拟机使用当前路径(.)作为类搜索路径。 |
| -jar                                                         | 用来运行jar文件，优先级大于-cp和-classpath。只有在jar包中指定mainclass才能成功执行，否则报错。 |
| -D<propertyName>=value                                       | 在虚拟机的系统属性中设置属性名/值对，运行在此虚拟机之上的应用程序可用。 |
| -verbose:gc                                                  | 在虚拟机调用 native 方法时输出设备显示信息，格式如下：[Dynamic-linking native method HelloNative.sum ... JNI] 该参数用来监视虚拟机调用本地方法的情况，在发生 jni错误时可为诊断提供便利。 |
| -version                                                     | 显示可运行的虚拟机版本信息然后退出。                         |
| –showversion                                                 | 显示版本信息以及帮助信息。                                   |

1. -cp选项和-classpath选项区别

   ```shell
   ##语法格式: java  -classpath [jar包列表]  [启动类]
   [root@t-kfzx-2 ~]# java -server -Xms1g -Xmx1g -classpath  first.jar:second.jar:third.jar com.tcl.multimedia.nretail.ApplicationMain
   ```

   -cp是-classpath的缩写，两个选项其实是一样的。告知虚拟机搜索目录名、jar 文档名、zip 文档名，windows中用分号分隔，linux中用冒号分隔。

   虚拟机在运行一个类时，需要将其装入内存，虚拟机搜索类的方式和顺序如下：Bootstrap classes，Extension classes，User classes。

   - **Bootstrap** 是虚拟机自带的 jar 或 zip 文件，虚拟机首先搜索这些包文件，用System.getProperty("sun.boot.class.path")  可得到虚拟机搜索的包名。
   - **Extension** 是位于 jre/lib/ext 目录下的 jar 文件，虚拟机在搜索完 Bootstrap后就搜索该目录下的 jar 文件。用System. getProperty("java.ext.dirs”)可得到虚拟机使用Extension 搜索路径。
   - **User classes** 搜索顺序为当前目录、环境变量 CLASSPATH、-classpath。

   使用-classpath 后虚拟机将不再使用 CLASSPATH 中的类搜索路径，如果-classpath 和 CLASSPATH 都没有设置，则虚拟机使用当前路径(.)作为类搜索路径。推荐使用-classpath 来定义虚拟机要搜索的类路径，而不要使用环境变量 CLASSPATH 的搜索路径，以减少多个项目同时使用 CLASSPATH 时存在的潜在冲突。

   使用-classpath会按照jar包的顺序搜索，会匹配到第一个搜索到的类。

   

- 扩展参数

| 选项        | 作用                                                         |
| :---------- | ------------------------------------------------------------ |
| -Xmixed     | 默认为混合模式，设置-client 模式虚拟机对使用频率高的方式进行 Just-In-Time 编译和执行，对其他方法使用解释方式执行。 |
| -Xint       | 设置-client模式下运行的虚拟机以解释方式执行类的字节码，不将字节码编译为本机码。 |
| **-Xms<size>** | 设置虚拟机可用内存堆的初始大小，缺省单位为字节，该大小为 1024 的整数倍并且要大于1MB，可用 k(K)或m(M)为单位来设置较大的内存数。初始堆大小为 2MB。例如：-Xms6400K，-Xms256M |
| **-Xmx<size>** | 设置虚拟机内存堆的最大可用大小，缺省单位为字节，该值必须为 1024 整数倍，并且要大于 2MB。可用 k(K)或 m(M)为单位来设置较大的内存数。缺省堆最大值为 64MB。例如：-Xmx81920K，-Xmx80M。当应用程序申请了大内存运行时虚拟机抛出 java.lang.OutOfMemoryError: Java heap space 内存溢出错误，就需要使用-Xmx 设置。 |
| **-Xss<size>** | 设置线程栈的大小，缺省单位为字节。与-Xmx 类似，也可用 K 或 M 来设置较大的值。通常操作系统分配给线程栈的缺省大小为 1MB。另外也可在 java 中创建线程对象时设置栈的大小，构造函数原型为 Thread(ThreadGroup group, Runnable target, String name, long stackSize)。 |
| -Xnoclassgc lllllllllllllllllllllllllllllllllllllllllll | 关闭虚拟机对 class 的垃圾回收功能。 |
| -Xincgc | 启动增量垃圾收集器，缺省是关闭的。增量垃圾收集器能减少偶然发生的长时间的垃圾回收造成的暂停时间。但增量垃圾收集器和应用程序并发执行，因此会占用部分 CPU 在应用程序上的功能。 |
| -Xloggc:<file> | 将虚拟机每次垃圾回收的信息写到日志文件中，文件名由 file 指定，文件格式是平文件，内容和-verbose:gc 输出内容相同。 |
| -Xbatch | 虚拟机的缺省运行方式是在后台编译类代码，然后在前台执行代码，使用-Xbatch参数将关闭虚拟机后台编译，在前台编译完成后再执行。 |
| -Xbootclasspath:path | 改变虚拟机装载缺省系统运行包 rt.jar 而从-Xbootclasspath 中设定的搜索路径中装载系统运行类。除非你自己能写一个运行时，否则不会用到该参数。 |
| -Xprof | 输出 CPU 运行时的诊断信息。 |
| -Xfuture | 对类文件进行严格格式检查，以保证类代码符合类代码规范。为保持向后兼容，虚拟机缺省不进行严格的格式检查。 |
| -Xrs | 减少虚拟机中操作系统的信号（singals）的使用。该参数通常用在虚拟机以后台服务方式运行时使用（如 Servlet）。 |
| -Xcheck:jni | 调用 JNI 函数时进行附加的检查，特别地虚拟机将校验传递给 JNI 函数参数的合法性，在本地代码中遇到非法数据时，虚拟机将报一个致命错误而终止。使用该参数后将造成性能下降。 |
