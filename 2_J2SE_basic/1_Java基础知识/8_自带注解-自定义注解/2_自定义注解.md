## 元注解

元注解的作用就是负责注解其他注解。Java5.0 定义了4个标准的meta-annotation 类型，它们被用来提供对其它 annotation类型作说明。

### 1.@Target

- 作用

  指定了注解的使用范围，明确其修饰的目标。

- 取值范围

  | ElementType(取值) | 说明                                         |
  | ----------------- | -------------------------------------------- |
  | CONSTRUCTOR       | 用于描述构造器                               |
  | FIELD             | 用于描述域                                   |
  | LOCAL_VARIABLE    | 用于描述局部变量                             |
  | METHOD            | 用于描述方法                                 |
  | PACKAGE           | 用于描述包                                   |
  | PARAMETER         | 用于描述参数                                 |
  | TYPE              | 用于描述类、接口 (包括注解类型) 或 enum 声明 |

### 2.@Retention

- 作用

  指定了注解的被保留时间长短，描述注解的生命周期。

- 取值范围

  | RetentionPoicy(取值) | 说明                                           |
  | -------------------- | ---------------------------------------------- |
  | SOURCE               | 在源文件中有效（即源文件保留）                 |
  | CLASS                | 在 class 文件中有效（即 class 保留）           |
  | RUNTIME              | 在运行时有效（即运行时保留），可以通过反射获取 |

### 3.@Documented

- 作用

  用于描述其它类型的 annotation 应该被作为被标注的程序成员的公共 API，因此可以被例如
  javadoc 此类的工具文档化。

  Documented是一个标记注解，没有成员。

### 4.@Inherited

- 作用

  1. 标记注解，阐述了某个被标注的类型是被继承的。如果一个使用了 @Inherited 修饰的 annotation 类型被用于一个 class，则这个 annotation 将被用于该 class 的子类。

  2. @Inherited是被标注过的 class 的子类所继承。**类并不从它所实现的接口继承 annotation，方法并不从它所重载的方法继承 annotation。**

  3.  当@Inherited annotation 类型标注的 ”自定义注解“ 的 Retention 是 RetentionPolicy.RUNTIME，则反射 API 增强了这种继承性。

     如果使用反射去检查这个自定义注解时。将会先检查class和其父类，直到发现指定的自定义注解，或者到达该类继承结构的顶层。

## 自定义注解

### 1.定义规则

```java
/**
 * 类 <code>{类名称}</code>{此类功能描述}
 *
 * @author zangbao.lin
 * @version 2019/10/22
 * @since JDK 1.8
 */

@Documented
@Inherited
// @Target注解可以定义多个范围
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)

/**
 * 定义注解规则
 * 1. 在定义注解时,不能继承其他注解或实现接口。
 * 2. @interface 用来声明一个注解，其中的每一个方法实际上是声明了一个配置参数。
 * 3. 方法名称就是参数名称,返回值类型就是参数类型。
 */
public @interface Student {

    /**
     * 注解成员要求
     * 1. 类型只能是基本类型及String,Class,Annotation,Enumeration, 包括所有类型的数组。
     * 2. 如果只有一个成员,则必须为 value(),在使用时可以忽略成员名和赋值符号。
     * 3. 如果没有成员,该注解则称为标识注解。
     * 4. 注解元素必须有确定的值，要么在定义注解的默认值中指定，要么在使用注解时指定，非基本类型的注解元素的值
          不可为 null。
     * 5. 如果需要标识注解元素不存在,则使用空字符串或者负数来表示。
     * 6. 可以用default表示参数的默认值。
     */

    String value();

    int age() default 18;

}
```

### 2.注解的使用

- 定义注解

  ```java
  @Documented
  @Inherited
  @Target({ElementType.TYPE,ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Book {
      String name();
      String author();
      String descript();
  }
  ```

- 使用注解

  ```java
  public class BookStore {
      // 注解参数必须要有确切的值,可以定义默认值,也可以在使用时指定
      @Book(name = "万历十五年",author = "黄仁宇",descript = "明朝历史")
      public void getBook() {
          System.out.println("this book is very good!");
      }
  }
  ```

- 获取注解的值

  ```java
  public class UseAnnotation {
      public static void main(String[] args) throws Exception{
          // 通过反射获取Class
          Class clazz = BookStore.class;
          Method method = clazz.getMethod("getBook");
  
          // 获取方法注解的值
          if(method.isAnnotationPresent(Book.class)) {
              Book book = method.getAnnotation(Book.class);
              System.out.println(String.format("书名为[%s],作者为[%s],介绍为[%s]",
                                book.name(),book.author(),book.descript()));
          }
      }
  }
  ```

  