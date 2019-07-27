## 1.JDK和JRE区别

###基础概念

1. JDK（Java Development Kit）是面向开发人员使用的SDK，提供了Java的开发环境和运行环境。SDK是Software Development Kit 一般指软件开发包，可以包括函数库、编译程序等。
2. JRE（Java Runtime Enviroment）是指Java的运行环境，面向Java程序的使用者，而不是开发者。

###JDK和JRE的联系区别

1. 如果安装了JDK，电脑中会同时安装两套JRE，一套位于JDK中的/JRE文件夹，一套位于和JDK同级的独立的JRE目录中。独立的JRE 比 JDKJRE 少了Server端的Java虚拟机，不过直接将 JDKJRE 中的 Server端虚拟机复制过来也是一样的。如果安装JDK可以选择是否安装JRE，然而安装JRE就只会安装JRE环境。
2. JRE的地位就象一台PC机一样，写好的Win32应用程序需要操作系统才能运行。同样的，Java程序也必须要JRE才能运行。

### 为什么安装SUN JDK会安装两套JRE?

1. JDK里面有很多用Java所编写的开发工具（如javac.exe、jar.exe等），而且都放置在 \lib\tools.jar 里。javac.exe只是一个包装器（Wrapper），而制作的目的是为了让开发者免于输入太长的指命。而且可以发现\lib目录下的程序都很小，不大于2.9K。JDK里的工具几乎是用Java所编写，所以也是Java应用程序。因此要使用JDK所附的工具来开发Java程序，也必须要自行附一套JRE才行。
2. JDKJRE就是用来运行JDK中的工具，独立的JRE就是用来运行系统中的一般程序用。

### JRE的调用顺序

1. 当一台电脑上安装两套JRE时，Java.exe的工作就是找到合适的JRE来运行Java程序。
2. Java.exe按照以下顺序来调用JRE：
   - java.exe所在的目录
   - java.exe所在的目录的父目录
   - 查询注册表[HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment] 

### JDK和JRE目录中的文件

1. 程序的执行流程：普通Java程序需要JVM编译生成 .class 字节码文件，再由JVM将字节码文件解释给本地系统执行程序，所以Java也被称作解释型语言。

2. JVM在JRE中的组成：在JRE文件夹中，/bin 和 /lib 两个目录共同组成JVM。bin中就是JVM运行程序，lib中就是JVM所需jar包。

3. JDK的安装目录

   | 目录名称       | 作用                                                         |
   | -------------- | ------------------------------------------------------------ |
   | bin            | 存放了JDK的各种工具命令即JDK开发工具的可执行文件。其中这些可执行文件都是二进制的，其中包括编译器、解释器以及其他一些工具，例如 java命令   javac命令。 |
   | include        | 里面是一些供C语言使用的标题文件，其中C语言的头文件支持Java本地接口和Java虚拟机调试程序接口的本地编程技术。 |
   | jre            | 运行Java程序所必须的JRE环境。                                |
   | lib            | 存放的是JDK工具命令的实际执行程序。                          |
   | javafx-src.zip | 存放JavaFX脚本，JavaFX它是一种声明式、静态类型编程语言。(jdk1.8下新加的)。 |
   | src.zip        | 存放的就是Java所有核心类库的源代码。                         |
   | COPYRIGHT      | 版权的说明性文档。                                           |
   | LICENSE        | 签证的说明性文档。                                           |

4. IDE（Integrated Development Environment）就是集成开发环境
   
   - eclipse、idea等其他IDE有自己的编译器而不是用JDK bin目录中自带的，所以在安装时你会发现他们只要求你选中jre路径就ok了。