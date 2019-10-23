## 异常体系

### 1.基本介绍

- 定义

  程序运行时，发生的不被期望的事件，阻止了程序按照程序员的预期正常执行，这就是异常。

- 异常的产生

  1. Java 中的异常可以是函数中的语句执行时引发的，也可以是程序员通过 throw 语句手动抛出的。
  2. 只要在 Java 程序中产生了异常，就会用一个对应类型的异常对象来封装异常，JRE 就会试图寻找异常处理程序来处理异常。

- JDK中的异常类

  1. Throwable 类是 Java 异常类型的顶层父类，一个对象只有是 Throwable 类的（直接或者间接）实例，他才是一个异常对象，才能被异常处理机制识别。
  2. JDK 中内建了一些常用的异常类，也可以自定义异常。

### 2.Throwable类结构图

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Throwble类结构图.png)

- Error

  不需要捕捉，属于系统性错误。错误不能被程序员通过代码处理，Error 很少出现。

- Exception

  1. 检查异常/编译期异常（checked exception）

     必须被 try{}catch语句块所捕获,或者在方法签名里通过throws子句声明。Java编译器要进行检查，虚拟机运行时也要检查。

     此类异常的原因一般是程序代码的问题。

  2. 非检查异常/运行期异常（unckecked exception）

     个人判断是否捕获和处理，虚拟机运行时会检查，一般有JVM虚拟机进行处理。

     此类异常的原因一般是程序运行环境所导致的，编译器就会检查出来。

  

##异常的处理

###1. 非检查异常/运行期异常

- 异常是在执行某个函数时引发的。函数是层级调用，形成调用栈的。因此，产生异常的函数以及其调用者都会抛出异常。称为异常的冒泡。
- 这些被影响的函数以异常信息输出，形成异常追踪栈。异常最先发生的地方，叫做异常抛出点。
- 如果没有使用任何异常处理机制，异常最终会由 main 函数抛给 JVM，导致程序终止。

###2.检查异常/编译期异常

- try..catch..finally处理语句

```java
/**
* 普遍的异常处理流程
* 1. 有的编程语言当异常被处理后，控制流会恢复到异常抛出点接着执行，这种策略叫做恢复式异常处理模式。
* 2. Java则是让执行流恢复到处理了异常的 catch 块后接着执行，这种策略叫做终结式异常处理模式。
**/
try{
	/**
	* try块中放可能发生异常的代码。
	* 如果执行完try且不发生异常，则接着去执行final1y块和finally后面的代码（如果有的话）。
	* 如果发生异常，则尝试去匹配catch块。
	**/
} catch (SQLException ex){
	/**
	* 一个catch块用于捕获并处理一个特定的异常或者其子类。Java7中可以将多个异常声明在一个catch中。
	* 后面的括号定义了异常类型和参数。如果异常与之匹配且是最先匹配到的，则虚拟机将使用这个catch块来处理异常。
	* 在catch块中可使用这个异常参数来获取异常的相关信息。异常参数是这个catch块中的局部变量，其它块不能访问。
	* 如果try块中的异常在所有catch中都没捕获到，则先去执行final1y，然后到该函数调用者中去匹配异常处理器。
	* 如果try中没有发生异常，则所有的catch块将被忽略。
	* catch块中应该将子类异常放前面，父类异常放后面，保证每个catch块都有意义。
	**/
	ex.printStackTrace();
} catch (Exception ex) {
	ex.printStackTrace();
} finally {
	/**
	* final1y块通常是可选的。无论异常是否发生，异常是否匹配被处理，final1y都会执行。
	* 一个try至少要有catch块或者finally块。但是finally不是用来处理异常的，finally不会捕获异常。
	* final1y主要做一些清理工作，如流的关闭，数据库连接的关闭等。
	**/
}
    

/**
* return语句的异常处理流程
* 1. 如果finally块存在,在执行try或catch中的return之前,会先执行finally块的代码。
* 2. 在finally块中存在return,则该return语句一定会被执行,不会执行try或catch中的return。
* 3. 编译器把finally中的return实现为一个warning。
* finally中的return
* 1. 如果finally中有return正常值，try或catch中的异常不会被捕获。
* 2. 如果finally中有抛出异常，则不会捕获其他代码块的异常。
**/
try{
    return;
} catch (Exception ex) {
    ex.printStackTrace();
} finally {
    return;
}
```

- finally块不会执行的情况
  1. finally块中发生了异常。
  2. 程序所在线程死亡。
  3. 在前面的代码中用了System.exit()。
  4. 关闭了CPU。
- Throws - 方法定义上抛出异常

```java
/**
* 实例： Throws抛出异常 
* 1. 方法内部抛出检查异常又没有捕捉异常，则编译器要求必须在方法定义上使用throws关键字声明可能抛出的异常，否
*    则编译不通过
* 2. 方法本身不知如何处理该异常，调用者需要来处理。main方法抛出则直接抛给JVM.
* 3. 捕获异常后，只对异常进行部分处理，还有些处理需要在该方法的调用者中才能完成，所以应该再次抛出异常，让该方
*    法的调用者也能捕获到异常。
**/
public void show() throws Exception {
    // 抛出异常
}
```

- Throw - 方法内异常抛出语句

```java
/**
* 实例：方法体内抛出异常
* 1. 可以通过throw语句手动显式的抛出一个异常,throw 语句的后面必须是一个异常对象。
* 2. throw语句必须写在函数中，执行throw语句的地方就是一个异常抛出点，和其他异常没有区别。
**/
public void show() throws Exception {
	if(User == null) {
        throw new RuntimeException("User对象为空!");
    }
}
```

### 3.异常的链化

- 出现背景

  在一些大型的，模块化的软件开发中，一旦一个地方发生异常，则如骨牌效应一样，将导致一连串的异常。

  假设 B 模块完成自己的逻辑需要调用 A 模块的方法，如果 A 模块发生异常，则 B 也将不能完成而发生异常，但是 B 在抛出异常时，会将 A 的异常信息掩盖掉，这将使得异常的根源信息丢失。

  异常的链化可以将多个模块的异常串联起来，使得异常信息不会丢失。

- 解决方法

  最底层出现异常，将异常信息写在一个新异常中，然后抛出给上一层的调用函数。这样最外层的函数抛出的异常中，就有所有的异常信息。

## 自定义异常

### 1. 定义

自定义异常类。扩展Exception类,都属于检查/编译期异常。扩展RuntimeException类,都属于运行时异常。

### 2. 标准写法

```java
/**
* 自定义异常标准
* 1. 一个无参构造函数。
* 2. 一个带有 String 参数的构造函数，并传递给父类的构造函数。
* 3. 一个带有 String 参数和 Throwable 参数，并都传递给父类构造函数。
* 4. 一个带有 Throwable 参数的构造函数，并传递给父类的构造函数。
**/

public class IoException extends Exception {
    static final long serialVersionUID=7818375828146090155L;
    // 1. 一个无参构造函数
    public IoException() {
    	super();    
    }
    
    // 2. 一个带有 String 参数的构造函数，并传递给父类的构造函数。
    public IOException(String message) {
    	super(message);
    }
    
    // 3. 一个带有 String 参数和 Throwable 参数，并都传递给父类构造函数。
    public IoException (String message,Throwable cause) {
    	super(message,cause);
    }
    
    // 4. 一个带有 Throwable 参数的构造函数，并传递给父类的构造函数。
    public IOException(Throwable cause) {
    	super(cause);
    }  
}
```

