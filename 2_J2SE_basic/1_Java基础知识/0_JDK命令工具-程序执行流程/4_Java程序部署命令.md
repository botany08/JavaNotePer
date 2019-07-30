## JAVA程序部署

### 1. 编译.java文件

```shell
##编译java文件 - javac命令
javac HelloWorld.java
```

### 2.打包.class文件

```shell
##打包 - jar命令
jar -cvf helloworld.jar（jar包名称）  HelloWorld.class（编译文件名称或目录）
```

### 3.运行java程序

``` shell
##第一种方式：java  -jar
nohup java -server -Xms1g -Xmx1g  -jar nretail-tlink-server-1.0.0-SNAPSHOT.jar > nretail-tlink-
server.log  2>&1 &

##第二种方式：java -classpath
nohup java -server -Xms1g -Xmx1g  -classpath   [ jar包列表，用：隔开]  [ 主要启动类 ]
```

