## BigDecimal类

### 1.简介

- Java在java.math包中提供的API类BigDecimal，用来对超过16位有效位的数进行精确的运算。双精度浮点型变量double可以处理16位有效数。
- 在实际应用中，需要对更大或者更小的数进行运算和处理。float和double只能用来做科学计算或者是工程计算，在商业计算中要用java.math.BigDecimal。
- BigDecimal 都是不可变的（immutable）的，在进行每一步运算时，都会产生一个新的对象，所以在做加减乘除运算时千万要保存操作后的值。
- 尽量使用参数类型为 String 的构造函数。

### 2.构造方法

```java
// 利用int,double,long,String构建一个BigDecimal对象
public BigDecimal(int val);
public BigDecimal(double val);
public BigDecimal(long val);
public BigDecimal(String val);
```

### 3.常用方法

```java
// 加减乘除运算
public BigDecimal add(BigDecimal augend); // 加
public BigDecimal subtract(BigDecimal subtrahend); // 减
public BigDecimal multiply(BigDecimal multiplicand); // 乘
public BigDecimal divide(BigDecimal divisor); // 除

// 转换方法
public String toString();
public double doubleValue();
public float floatValue();
public int intValue();
public long longValue();

// BigDecimal比较方法. 左边比右边大，返回1;相等返回0;小于返回-1
public int compareTo(BigDecimal val);
```

### 4.格式化

```java
/** 
* 实例：格式化 DecimalFormat类
**/
public static void main(String[] args){
    BigDecimal bigDecimal = new BigDecimal("10.2345");
    System.out.println("原值为："+bigDecimal.toString());

    DecimalFormat df = new DecimalFormat("00.0");
    System.out.println("转换为："+df.format(bigDecimal));
}

// 输出：
// 原值为：10.2345
// 转换为：10.2
```

|   符号    |       位置 |                             描叙                             |
| :-------: | ---------: | :----------------------------------------------------------: |
|     0     |       数字 |                阿拉伯数字，如果不存在则显示0                 |
|     #     |       数字 |                阿拉伯数字，如果不存在不显示0                 |
|     .     |       数字 |                  小数分隔符或货币小数分隔符                  |
|     ,     |       数字 |                          分组分隔符                          |
|     E     |       数字 |  分隔科学计数法中的尾数和指数。*在前缀或后缀中无需加引号。*  |
|     -     |       数字 |                             负号                             |
|     ;     | 子模式边界 |                     分隔正数和负数子模式                     |
|     %     | 前缀或后缀 |                   乘以 100 并显示为百分数                    |
|  \u2030   | 前缀或后缀 |                   乘以 1000 并显示为千分数                   |
| ¤(\u00A4) | 前缀或后缀 | 货币记号，由货币符号替换。如果两个同时出现，则用国际货币符号替换。如果出现在某个模式中，则使用货币小数分隔符，而不使用小数分隔符。 |
|     '     | 前缀或后缀 | 用于在前缀或或后缀中为特殊字符加引号，例如 "'#'#"将 123 格式化为 "#123"。要创建单引号本身，请连续使用两个单引号："# o''clock"。 |

### 5.舍入模式

```java
// 舍入模式
BigDecimal.BROUND_UP; // 向上舍入
BigDecimal.ROUND_DOWN; // 向下舍入
BigDecimal.ROUND_HALF_UP; // 四舍五入

/** private final int scale; 
* scale属性表示小数位数,
**/
public int scale(); // 获取小数位数
public BigDecimal setScale(int newScale); // 设置小数位数
public BigDecimal setScale(int newScale, RoundingMode roundingMode); // 设置小数位数以及舍入模式
    

```

