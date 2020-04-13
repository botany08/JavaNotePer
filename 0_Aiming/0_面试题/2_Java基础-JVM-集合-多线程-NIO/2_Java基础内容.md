### 1. Java是如何保证其安全性的？

- 封装字段访问权限、 C语言手动内存管理和JVM GC保证一定程度上的内存安全、内存不泄露 

### 2. Java 内存泄漏问题，解释一下什么情况下会出现

- 强引用、 线程池里的线程使用ThreadLocal 自己忘记回收、需要线程回收了才会回收占用的内存造成内存泄漏，如果内存很大会造成频繁的GC极大拖慢进程的速度。

###3.int float short double long char 占字节数？
###4.int 范围？float 范围？
###5.hashcode 和 equals 的关系
###6.深拷贝、浅拷贝区别
###7.java 异常体系？RuntimeException Exception Error 的区别，举常见的例子
###8.lambda 表达式中使用外部变量，为什么要 final？