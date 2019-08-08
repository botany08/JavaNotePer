## String类

### 1.String的内存存储原理

####相关内存概念

1. 字符串常量池（编译期）
  JVM为了减少字符串对象的重复创建，维护了一个特殊的内存，这段内存称为**字符串常量池**或**字符串字面量池**。

  字符串常量池实现的前提条件就是Java中String对象是不可变的，可以安全保证**多个变量共享同一个对象**。如果Java中的String对象可变的话，一个引用操作改变了对象的值，那么其他的变量也会受到影响，显然这样是不合理的。

2. 堆栈的概念（运行期）
  Java中所有由类实例化的对象和数组都存放在堆内存中，无论是成员变量，局部变量，还是类变量，它们指向的对象都存储在堆内存中。而栈内存用来存储局部变量和方法调用。

3. 寄存器概念
  Java中运行时数据区有一个程序寄存器（又称程序计数器），该寄存器为线程私有。Java中的程序计数器用来记录当前线程中正在执行的指令。如果当前正在执行的方法是本地方法，那么此刻程序计数器的值为undefined。

####String对象的生成

new String(“hello”) 生成了两个对象。在编译期，“heelo”在字符串常量池生成了一个string对象，假设引用为A1。在运行期，在堆中生成了一个string对象，再将指针指向了“hello”，假设引用为A2。此时A1 不等于 A2。

![](https://github.com/Monster522/ImageBed/blob/master/2_c2899ac9-e7e7-429d-93ad-0caef098d76b.png?raw=true)

```java
String str1 = "hello";				    //str1直接指向“hello”
String str2 = “he” + “llo”			    //此时str2 也是指向“hello”
String str3="he"+ new String("llo");	//当String str3 = “he”+”llo”时，判断为true
System.out.println(str1==str3);   	    //判断为false

String sg1 = “he”
String sg2 = “llo”
System.out.println((sg1+sg2) == str1) 	//判断为false

/**
* 执行流程：
* 1.在编译期，在常量池中生成“hello”，并将引用地址直接赋值给str1（在栈中的地址值）
* 2.在编译期，在常量池中生成了“he”和“llo”两个string，执行一下和为“hello”，再将引用地址赋值给str2
* （所以str1和str2在栈中的地址值是相等的）
* 3.在运行期，在堆中生成一个string对象，并将该对象的地址赋值给str3。该对象中也有一个指针值，指向常量池“llo”。
* 执行一下，该对象中的指针值变为指向“hello”，所以str3最终指向了“hello”。但是str3本身的地址值和str1 str2是不
* 相等的。str3先指向对象，对象再指向常量池。str1/2直接指向常量池。
* 4.在编译期，sg1指向“he”，sg2指向“llo”。
* 5.在运行期，sg1+sg2生成一个新对象在堆中，sg1+sg2指向堆中的对象，对象中的值再指向常量池的“hello”。
* 6.在编译期不生成堆中对象，只会进行常量池中的操作。编译期中没有解决的运算会放到运行期中解决，直接生成对象。
**/
```

### 2.String常用方法

####构造方法

```java
// 类定义，final关键字，阻止继承和多态。
import java.lang.String
public final class String

// 1.无参构造方法，用来创建空字符串的String对象。
public String();
// 实例
String str1 = new String(); 

// 2.参数：字符串 String
public String(String value);
// 实例：利用str2创建str3
String str2 = new String("asdf"); 
String str3 = new String(str2);

// 3.参数：字符数组 char[]
public String(char[] value);
// 实例：相当于String str4 = new String("abcd");
char[] value = {'a','b','c','d'};
String str4 = new String(value);

// 4.参数：字符数组 char[]，起始位置 startIndex，位数 numChars
public String(char chars[], int startIndex, int numChars);
// 实例：相当于String str5 = new String("bc");
char[] value = {'a','b','c','d'};
String str5 = new String(value, 1, 2);

// 5.参数：字节数组 byte[]
public String(byte[] values);
// 实例：相当于String str6 = new String("AB");
byte[] strb = new byte[]{65,66};
String str6 = new String(strb);

// 6.参数：字节数组 byte[]，转换编码 charset
public String(byte bytes[], String charsetName)
// 实例：将字符串'ISO8959-1'转换为GB2312编码
String str = new String("ISO8959-1".getBytes(),"GB2312");
```

####获取bytes[]数组

```java
// 使用平台的默认字符集将此 String编码为字节序列，将结果存储到新的字节数组中。
public byte[] getBytes();

// 使用给定的charset将该String编码为字节序列，将结果存储到新的字节数组中。
public byte[] getBytes(Charset charset);

// 使用命名的字符集将此 String编码为字节序列，将结果存储到新的字节数组中。
public byte[] getBytes(String charsetName);
```

####将字符串复制为char[]数组

```java
/** 将此字符串中的字符复制到目标字符数组中。 
* srcBegin - 要复制的字符串中第一个字符的索引。 
* srcEnd - 要复制的字符串中最后一个字符后面的索引。 
* dst - 目标数组。 
* dstBegin - 目标数组中的起始偏移量。 
**/
void getChars(char dst[], int dstBegin);
public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin);

// 将此字符串转换为新的字符数组。
public char[] toCharArray();
```

####字符串属性

```java
// 返回此字符串的长度。 
public int length();

// 返回char指定索引处的值,指数范围为0至length() - 1 。
public char charAt(int index);

// 返回字符串是否为空。 不能检查空字符串
public boolean isEmpty();

// 返回一个字符串，其值为此字符串，并删除任何前导和尾随空格。 
public String trim();

// 将字符串全部转换成小写。
public String toLowerCase();
// 将字符串全部转换成大写。
public String toUpperCase();

// 测试此字符串是否以指定的前缀开头。
public boolean startsWith(String prefix);

// 通过使用给定表达式分割字符串。尾随的空字符串不会包含在结果数组中。
public String[] split(String regex);
// 限制分割次数。通过使用给定表达式分割字符串。尾随的空字符串不会包含在结果数组中。
public String[] split(String regex, int limit);

// 将指定的字符串连接到该字符串的末尾。 
public String concat(String str);

// replace和replaceAll的区别是：一个通过char匹配，一个通过regex匹配。相同的是全部替换。
// 将与字面目标序列匹配的字符串的每个子字符串替换为指定的字面替换序列。
public String replace(char oldChar, char newChar);
// 用给定的替换替换与给定的regular expression匹配的此字符串的每个子字符串。
public String replaceAll(String regex, String replacement);
// 用给定的替换替换与给定的regular expression匹配的此字符串的第一个子字符串。
public String replaceFirst(String regex, String replacement);

// 字符串是否匹配给定的regular expression 。
public boolean matches(String regex);    
  
```

####子字符串

```java
// 返回子字符串。子字符串以指定索引处的字符开头，并扩展到该字符串的末尾。
public String substring(int beginIndex);
// 返回子字符串。子串开始于指定beginIndex并延伸到字符索引endIndex-1，子串的长度为endIndex-beginIndex 。 
public String substring(int beginIndex, int endIndex);

// 返回指定子字符串第一次出现的字符串内的索引。
public int indexOf(String str);
// 返回指定子串的第一次出现的字符串中的索引，从指定的索引开始。
public int indexOf(String str, int fromIndex);

// 返回指定子字符串最后一次出现的字符串中的索引。 
public int lastIndexOf(String str);
// 返回指定子字符串的最后一次出现的字符串中的索引，从指定索引开始向后搜索。
public int lastIndexOf(String str, int fromIndex);

```

####字符串比较

```java
// 按字典顺序比较两个字符串。 比较是基于字符串中每个字符的Unicode值。
// 小于anotherString，返回负整数。大于anotherString，返回正整数。相等返回0。
// 如果 str1是str2的子字符串，返回负整数。如果str2是str1的子字符串，返回正整数。
public int compareTo(String anotherString);
public int compareToIgnoreCase(String str); //忽略大小写

// 将此字符串与指定对象进行比较。 只有非空的string对象才能比较返回true。
public boolean equals(Object anObject);
public boolean equalsIgnoreCase(String anotherString); // 忽略大小写
```



## String/StringBuffer/StringBuilder的区别

### 1.可变与不可变

- String类中使用字符数组保存字符串，String为final不可变对象。一旦被创建,就不能修改它的值。对于已经存在的String对象的修改都是重新创建一个新的对象,然后把新的值保存进去。

  ```java
  public final class String {
  	private final char value[];
  }
  ```

- StringBuilder与StringBuffer都继承自AbstractStringBuilder类，在AbstractStringBuilder中也是使用字符数组保存字符串，这两种对象都是可变的。

  ```java
  abstract class AbstractStringBuilder implements Appendable, CharSequence {
      char[] value;
  }
  
  /**
  * StringBuffer是一个可变对象,修改的时候不能重新生成对象，只能通过构造函数来建立，不能直接赋值。
  * 对象新建后会在堆中分配空间，并初始化为null。对StringBuffer赋值的时候通过append方法。
  **/
  StringBuffer sb = new StringBuffer(); //正确
  sb = "welcome to here!"; //错误
  sb.append("welcome to here!") //正确
  ```

  

### 2.是否多线程安全

- String中的对象是不可变的，也就可以理解为常量，显然线程安全 。
- AbstractStringBuilder是StringBuilder与StringBuffer的公共父类，定义了一些字符串的基本操作，如expandCapacity、append、insert、indexOf等公共方法。线程安全的方法在子类中定义。

```java
// StringBuffer对方法加了同步锁或者对调用的方法加了同步锁，所以是线程安全的 
public  synchronized  StringBuffer reverse() {
    super .reverse();
    return   this ;
}

public   int  indexOf(String str) {
    // 存在 public synchronized int indexOf(String str, int fromIndex) 方法
    return  indexOf(str, 0);           
}

// StringBuilder并没有对方法进行加同步锁，所以是 非线程安全的 。
```

### 3.StringBuilder与StringBuffer共同点

- StringBuilder与StringBuffer有公共父类AbstractStringBuilder( 抽象类 )。
- StringBuilder、StringBuffer的方法都会调用AbstractStringBuilder中的公共方法，如super.append(...)。只是StringBuffer会在方法上加synchronized关键字，进行同步。
- 最后，如果程序不是多线程的，那么使用StringBuilder效率高于StringBuffer。
- 效率比较String < StringBuffer < StringBuilder，但是在String S1 =“This is only a”+“simple”+“test”时，String效率最高。（因为直接在编译期就就执行成功了）