## ThreadLocal原理

### 1.底层实现原理

- **实例代码**

  ```java
  public class ThreadLocalTest {
      // 1. 初始化线程局部变量
      final static ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>(){
          @Override
          protected SimpleDateFormat initialValue() {
              return new SimpleDateFormat("yyyy-MM-dd");
          }
      };
  
      // 2. 线程局部变量get()方法,获取线程特有对象SimpleDateFormat
      public void test(){
          System.out.println(SDF.get().format(new Date()));
      }
  }
  ```

- **数据结构**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/37_ThreadLocal原理.png)

  1. `ThreadLocal`类是有状态的，其变量包括两个`final`变量，和一个原子变量，是线程安全的。所以`ThreadLocal`实例是，每个线程都可以访问的。
  2. 每个线程都有唯一个`Thread`实例，持有一个`ThreadLocal.ThreadLocalMap`引用。
  3. 当`Thread1`创建并访问一个`ThreadLocal`实例时，会先初始化内部的`ThreadLocalMap`实例。
  4. 然后创建一个线程特有对象的实例，比如`ThreadLocal<String>`中的`String`对象，此时调用的是自定义的`initialValue`方法。
  5. `Thread1`将`ThreadLocal`实例弱引用作为键，`String`实例强引用作为值，封装成`ThreadLocalMap.Entry`对象，放入到`ThreadLocalMap`中。
  6. 当`Thread1`访问属于自己的`String`时，实际上是先拿到`Thread.ThreadLocalMap`实例，再根据`ThreadLocal`实例弱引用去获取到对应的`String`。
  7. `ThreadLocal`实例，是可以被多个线程作为`key`使用的。但对于每个线程，一个`ThreadLocal`实例就只对应一个线程特有对象。
  8. `ThreadLocal`一般声明为`static`变量，

- **ThreadLocal在JVM中的内存模型**

  1. 每一个线程都有属于自己的栈内存，栈中有一个指针，指向堆中的`Thread`对象。
  2. `Thread`对象中，有一个指针指向`ThreadLocalMap`实例，该实例中存在一个或多个`ThreadLocal`的弱引用。
  3. 对于`ThreadLocal`对象本身而言，其初始化的每个实例，都可以被多个线程拿到指针，是线程共有的实例。



### 2.ThreadLocal的内存泄露问题

- **内存泄露的场景**

  1. 当`ThreadLocal`存储很多`Key`为`null`的Entry的时候，而不再去调用`remove`、`get`、`set`方法，那么将导致内存泄漏。

  2. 当使用`static ThreadLocal`的时候，延长`ThreadLocal`的生命周期，那也可能导致内存泄漏。

     - `static`变量在类未加载的时候，它就已经加载，当线程结束的时候，`static`变量不一定会回收。那么，比起普通成员变量使用的时候才加载，`static`的生命周期加长将更容易导致内存泄漏危机。 
     - `static`变量是存在方法区中，如果变量值还指向`ThreadLocal`实例，则实例不会被回收。

  3. `ThreadLocal`对象设`null`了，出现伪内存泄露，然后使用线程池，线程结束后放回线程池中不销毁。这个线程一直不被使用，或者分配使用了又不再调用`get,set`方法，那么这个期间就会发生真正的内存泄露。 

     

     

- **防止内存泄漏的措施**

  1. 事实上，在`ThreadLocalMap`中的`set/getEntry`方法中，会对`key`为`null`(也即是`ThreadLocal`为`null`)进行判断，如果为`null`的话，那么是会对`value`置为`null`的。
  2. 也可以通过调用`ThreadLocal`的`remove`方法进行释放。
  3. `JVM`利用设置`ThreadLocalMap`的`Key`为弱引用，来避免内存泄露。 
  4. 当线程对象被回收后，也不会发生内存泄露。

- **内存泄露的内存模型**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/38_ThreadLocal的内存模型.png)

  1. `Threadlocal`里面使用了一个存在弱引用的`map`。
  2. 当释放掉`Threadlocal`的强引用以后，map里面的value却没有被回收。
  3. value永远不会被访问到了，所以存在着内存泄露。

  