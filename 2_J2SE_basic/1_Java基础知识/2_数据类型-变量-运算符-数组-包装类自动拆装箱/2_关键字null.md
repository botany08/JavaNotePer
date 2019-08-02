## 关键字 null

1. **null是关键字**，像public、static、final，是大小写敏感的。不能将null写成Null或NULL，编译器将不能识别它们然后报错。

2. 就像每种基本类型都有默认值一样，如int默认值为0，boolean的默认值为false，**null是任何引用类型的默认值**，不严格的说是所有object类型的默认值。

3. null不是对象也不是一种类型，仅是一种特殊的值。可**以将其赋予任何引用类型，也可以将null转化成任何类型。**

   ```java
   //null 赋值给任何引用类型
   String str = null; 
   Integer i = null; 
   Double d = null;  
    
   //将 null 转换成任何引用类型
   String myStr = (String) null; 
   Integer myI = (Integer) null;
   Double myD = (Double) null;
   ```

4. **null可以赋值给引用变量，但不能将null赋给基本类型变量**，例如int、double、float、boolean。编译器将会报错。

   如果将null赋值给包装类object，然后将object赋给各自的基本类型，编译器不会报错，但是在运行时期遇到空指针异常。这是Java中的自动拆箱导致的。

5. 任何含有null值的包装类在Java拆箱生成基本数据类型时候都会抛出一个空指针异常。

   ```java
   //自动拆箱成基本类型，null会报空指针错误。
   Integer iAmNull = null;
   int i = iAmNull; // Remember - No Compilation Error
   ```

6. **如果使用了带有null值的引用类型变量，instanceof操作将会返回false。**

   ```java
   //这是instanceof操作一个很重要的特性，使得对类型强制转换检查很有用。
   Integer iAmNull = null;
   if(iAmNull instanceof Integer){
      System.out.println("iAmNull is instance of Integer");                            
    }else{
      System.out.println("iAmNull is NOT an instance of Integer");
   }
   ```

7. 一个值为 null 的引用类型，调用其非静态方法会报空指针异常，调用静态方法不会报错，因为静态方法是在初始化类之前初始化的。

   ```java
   public class Testing {            
      public static void main(String args[]){
         Testing myObject = null;
          //不会报错
         myObject.iAmStaticMethod();
          //会报空指针错误
         myObject.iAmNonStaticMethod();                            
      }
     
      private static void iAmStaticMethod(){
           System.out.println("I am static method, can be called by null reference");
      }
     
      private void iAmNonStaticMethod(){
          System.out.println("I am NON static method, don't date to call me by null");
     }
   }
   ```

8. 如果方法的形参是引用类型，那么传给方法的实参可以为null。

   ```java
   public class BasicDataType {
   
       public static void main(String[] args){
           BasicDataType basicDataType = new BasicDataType();
           //调用方法的实际参数为null，相当于引用类型的默认值
           basicDataType.print(null);
       }
   
       public void print(Object o) {
           //当null调用方法时，会报NullPointerException，因为引用类型没有实例化
           System.out.println("this is o"+o.toString());
       }
   }
   ```

9. **可以使用==或者!=操作来比较null值，但不能使用其他算法或者逻辑操作**，例如小于或者大于。在Java中null==null将返回true。