## 阻塞队列BlockingQueue

### 1.BlockingQueue接口

#### 1.1基本简介

- **接口结构**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/13_BlockingQueue结构.png)

  1. `BlockingQueue`接口继承于，`Queue`接口和`Collection`接口，属于集合。

- **应用场景**

  1. 在"生产者-消费者"问题中，队列通常作为线程间操作的数据容器。对各个模块的业务功能进行解耦，生产者将数据放置在数据容器中，消费者从中获取数据。生产者线程和消费者线程实现解耦，专注本身的业务功能。
  2. 阻塞队列`(BlockingQueue)`被应用在“生产者-消费者”模型中，其原因是BlockingQueue 提供了可阻塞的插入和移除的方法。
  3. 当队列容器已满，生产者线程会被阻塞，直到队列未满。当队列容器为空时，消费者线程会被阻塞，直至队列非空时为止。

#### 1.2基本操作

- **Queue接口**

  | 方法名称              | 作用                                                         |
  | --------------------- | ------------------------------------------------------------ |
  | `boolean add(E e);`   | 往队列插入数据。当队列满时，插入元素时会抛出`IllegalStateException`异常。 |
  | `boolean offer(E e);` | 往队列插入数据。插入成功返回`true`，否则返回`false`。当队列满时不会抛出异常。 |
  | `E remove();`         | 从队列中删除数据。返回删除的元素，当队列为空时，会抛异常。   |
  | `E poll();`           | 从队列中删除数据。返回删除的元素，当队列为空时，返回`null`。 |
  | `E element();`        | 获取队头元素，如果队列为空时，则抛出`NoSuchElementException`异常。 |
  | `E peek();`           | 获取队头元素，如果队列为空时，则抛出`NoSuchElementException`异常。 |

  

- **BlockingQueue接口**

1. **超时添加**`boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;`

   若阻塞队列已经满时，会阻塞插入数据的线程，直至阻塞队列已经有空余的地方。有一个超时时间，若超过当前给定的超时时间，插入数据的线程会退出。

   

2. **阻塞添加**`void put(E e) throws InterruptedException;`

   当阻塞队列容量已经满时，往阻塞队列插入数据的线程会被阻塞，直至阻塞队列已经有空余的容量可供使用。

   

3. **阻塞取出**`E take() throws InterruptedException;`

   当阻塞队列为空时，获取队列数据的线程会被阻塞。

   

4. **超时取出**`E poll(long timeout, TimeUnit unit) throws InterruptedException;`

   当阻塞队列为空时，获取数据的线程会被阻塞。如果被阻塞的线程超过了给定的时长，该线程会退出。



####1.3BlockingQueue实现类

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/14_BQ实现类.png)

- **ArrayBlockingQueue**

  1. **由数组实现的有界阻塞队列**，该队列命令元素`FIFO(First In First Out)`。
  2. 可作为**有界数据缓冲区**，生产者插入数据到队列容器中，并由消费者提取。一旦创建，容量不能改变。
  3. 默认情况下不能保证线程访问队列的公平性，所谓公平性是指严格按照线程等待的绝对时间顺序，即最先等待的线程能够最先访问到`ArrayBlockingQueue`。
  4. 可以通过指定参数保证公平性，如果保证公平性，通常会降低吞吐量。

  

- **LinkedBlockingQueue**

  1. **用链表实现的有界阻塞队列**，同样满足`FIFO`的特性，与`ArrayBlockingQueue` 相比起来具有更高的吞吐量。

  2. 为了防止容量过大损耗内存，通常在创建`LinkedBlockingQueue`对象时，会指定其大小。如果未指定，容量等于`Integer.MAX_VALUE`。

     

- **SynchronousQueue**

  1. `SynchronousQueue`内部并不维护用于存储队列元素的实际存储空间，没有存储任何数据元素。

  2. 队列的每个插入操作必须等待，另一个线程进行相应的删除操作。

  3. 可以通过参数来保证公平性。

     

- **PriorityBlockingQueue**

  1.  一个支持优先级的无界阻塞队列。
  2. 默认情况下元素采用自然顺序进行排序，也可以通过自定义类实现`compareTo()`方法来指定元素排序规则，或者初始化时通过构造器参数`Comparator`来指定排序规则。 



###2.ArrayBlockingQueue

#### 2.1数据结构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/15_ABQ数据结构.png)

1. `ArrayBlockingQueue`的底层数据结构是数组。
2. `ArrayBlockingQueue`底层采用数据才存放数据，对数组的访问添加了锁的机制，使其能够支持多线程并发。

#### 2.2类继承关系

```java
public class ArrayBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {}
```

- **继承的类和实现的接口**
  1. 继承了`AbstractQueue`抽象类，`AbstractQueue`定义了对队列的基本操作。
  2. 实现了`BlockingQueue`接口，`BlockingQueue`表示阻塞型的队列，其对队列的操作可能会抛出异常。
  3. 实现了`Searializable`接口，表示可以被序列化。 

#### 2.3类属性

```java
public class ArrayBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    // 版本序列号
    private static final long serialVersionUID = -817911632652898426L;
    // 存放实际元素的数组
    final Object[] items;
    // 取元素索引
    int takeIndex;
    // 获取元素索引
    int putIndex;
    // 队列中的项
    int count;
    // 可重入锁
    final ReentrantLock lock;
    // 等待获取条件
    private final Condition notEmpty;
    // 等待存放条件
    private final Condition notFull;
    // 迭代器
    transient Itrs itrs = null;
}
```

- 底层结构是Object类型的数组，取元素和存元素有不同的索引，有一个可重入锁ReentrantLock，两个条件Condition。 

#### 2.4构造函数

```java
/**
* 用于创建一个带有给定的(固定)容量和默认访问策略的ArrayBlockingQueue.
**/
public ArrayBlockingQueue(int capacity) {
    // 调用两个参数的构造函数
    this(capacity, false);
}

/**
* 用于创建一个具有给定的(固定)容量和指定访问策略的ArrayBlockingQueue.
**/
public ArrayBlockingQueue(int capacity, boolean fair) {
    // 初始容量必须大于0
    if (capacity <= 0)
        throw new IllegalArgumentException();
    // 初始化数组
    this.items = new Object[capacity];
    // 初始化可重入锁,指定公平性
    lock = new ReentrantLock(fair);
    // 初始化等待条件
    notEmpty = lock.newCondition();
    notFull =  lock.newCondition();
}

/**
* 1.用于创建一个具有给定的(固定)容量和指定访问策略的ArrayBlockingQueue.
* 2.最初包含给定collection的元素,并以collection迭代器的遍历顺序添加元素.
**/
public ArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c) {
    // 调用两个参数的构造函数
    this(capacity, fair);
    // 可重入锁
    final ReentrantLock lock = this.lock;
    // 上锁
    lock.lock(); 
    
    
    try {
        int i = 0;
        try {
            // 遍历集合
            for (E e : c) {
                // 检查元素是否为空
                checkNotNull(e);
                // 存入ArrayBlockingQueue中
                items[i++] = e;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // 当初始化容量小于传入集合的大小时，会抛出异常
            throw new IllegalArgumentException();
        }
        // 元素数量
        count = i;
        // 初始化存元素的索引
        putIndex = (i == capacity) ? 0 : i;
    } finally {
        // 释放锁
        lock.unlock();
    }
}
```



#### 2.5核心函数

- **添加元素put()--阻塞**

  ```java
  /**
  * put函数用于存放元素，在当前线程被中断时会抛出异常，并且当队列已经满时，会阻塞一直等待。
  **/
  public void put(E e) throws InterruptedException {
      checkNotNull(e);
      // 获取可重入锁
      final ReentrantLock lock = this.lock;
      // 如果当前线程未被中断，则获取锁
      lock.lockInterruptibly();
      try {
          // 判断元素是否已满,若满则等待
          while (count == items.length)
              notFull.await();
          // 添加到队列
          enqueue(e);
      } finally {
          // 释放锁
          lock.unlock();
      }
  }
  
  /**
  * enqueue函数用于将元素存入底层Object数组中，并且会唤醒等待notEmpty条件的线程。
  **/
  private void enqueue(E x) {
      // 获取数组
      final Object[] items = this.items;
      // 将元素放入
      items[putIndex] = x;
      // 放入后存元素的索引等于数组长度,表示已满
      if (++putIndex == items.length) 
          // 重置存索引为0
          putIndex = 0;
      // 元素数量加1
      count++;
      // 唤醒在notEmpty条件上等待的线程
      notEmpty.signal();
  }
  ```

  

- **添加元素offer()--不阻塞**

  ```java
  /**
  * 1. offer函数也用于存放元素j,在调用ArrayBlockingQueue的add方法时,会间接的调用到offer函数.
  * 2. offer函数添加元素不会抛出异常,当底层Object数组已满时则返回false,否则会调用enqueue函数,将元素存入底层
  *    Object数组,并唤醒等待notEmpty条件的线程。
  **/
  public boolean offer(E e) {
      // 检查元素不能为空
      checkNotNull(e);
      // 可重入锁
      final ReentrantLock lock = this.lock;
      // 获取锁
      lock.lock();
      try {
          if (count == items.length) // 元素个数等于数组长度，则返回
              return false; 
          else { // 添加进数组
              enqueue(e);
              return true;
          }
      } finally {
          // 释放数组
          lock.unlock();
      }
  }
  ```

  

- **获取并删除take()--阻塞**

  ```java
  /**
  * 1. take函数用于从ArrayBlockingQueue中获取一个元素,其与put函数相对应,在当前线程被中断时会抛出异常.
  * 2. 并且当队列为空时,会阻塞一直等待.
  **/
  public E take() throws InterruptedException {
      // 可重入锁
      final ReentrantLock lock = this.lock;
      // 如果当前线程未被中断，则获取锁，中断会抛出异常
      lock.lockInterruptibly();
      try {
          // 元素数量为0,即Object数组为空
          while (count == 0) 
              // 则等待notEmpty条件
              notEmpty.await();
          // 出队列
          return dequeue();
      } finally {
          // 释放锁
          lock.unlock();
      }
  }
  
  /**
  * dequeue函数用于获取元素，并且会唤醒等待notFull条件的线程。
  **/ 
  private E dequeue() {
  
      final Object[] items = this.items;
      @SuppressWarnings("unchecked")
      
      // 取元素
      E x = (E) items[takeIndex];
      // 该索引的值赋值为null
      items[takeIndex] = null;
      // 取值索引等于数组长度
      if (++takeIndex == items.length)
          // 重新赋值取值索引
          takeIndex = 0;
      // 元素个数减1
      count--;
      if (itrs != null) 
          itrs.elementDequeued();
      // 唤醒在notFull条件上等待的线程
      notFull.signal();
      return x;
  }
  ```

  

- **获取并删除poll()--不阻塞**

  ```java
  /**
  * 1. poll函数用于获取元素,其与offer函数相对应,不会抛出异常.
  * 2. 当元素个数为0时返回null,否则调用dequeue函数，唤醒等待notFull条件的线程.并返回.
  **/
  public E poll() {
      // 重入锁
      final ReentrantLock lock = this.lock;
      // 获取锁
      lock.lock();
      try {
          // 若元素个数为0则返回null，否则，调用dequeue，出队列
          return (count == 0) ? null : dequeue();
      } finally {
          // 释放锁
          lock.unlock();
      }
  }
  ```

  

- **清空队列clear()**

  ```java
  /**
  * clear函数用于清空ArrayBlockingQueue,并且会释放所有等待notFull条件的线程(存放元素的线程)
  **/
  public void clear() {
      // 数组
      final Object[] items = this.items;
      // 可重入锁
      final ReentrantLock lock = this.lock;
      // 获取锁
      lock.lock();
      try {
          // 保存元素个数
          int k = count;
          // 元素个数大于0
          if (k > 0) { 
              // 存元素索引
              final int putIndex = this.putIndex;
              // 取元素索引
              int i = takeIndex;
              
              do {
                  // 赋值为null
                  items[i] = null;
                  // 重新赋值i
                  if (++i == items.length) 
                      i = 0;
              } while (i != putIndex);
              // 重新赋值取元素索引
              takeIndex = putIndex;
              // 元素个数为0
              count = 0;
              if (itrs != null)
                  itrs.queueIsEmpty();
              // 若有等待notFull条件的线程，则逐一唤醒
              for (; k > 0 && lock.hasWaiters(notFull); k--) 
                  notFull.signal();
          }
      } finally {
          // 释放锁
          lock.unlock();
      }
  }
  ```



### 3.LinkedBlockingQueue

#### 3.1数据结构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/16_LBQ数据结构.png)

-  **数据结构采用的是链表结构**
  1. `LinkedBlockingQueue`采用的是单链表结构，包含了头结点和尾节点。 

#### 3.2类继承关系

```java
public class LinkedBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {}
```

- **继承结构**
  1. 继承了`AbstractQueue`抽象类，AbstractQueue定义了对队列的基本操作。
  2. 实现了`BlockingQueue`接口，BlockingQueue表示阻塞型的队列，其对队列的操作可能会抛出异常。
  3. 实现了`Searializable`接口，表示可以被序列化。 

####3.3内部类

```java
/**
* Node内部类表示结点,包含了两个域,分别用于存放元素和指示下一个结点.
**/
static class Node<E> {
    // 元素
    E item;
    // next域
    Node<E> next;
    // 构造函数
    Node(E x) { item = x; }
}
```



#### 3.4类属性

```java
public class LinkedBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    // 版本序列号
    private static final long serialVersionUID = -6903933977591709194L;
    // 容量
    private final int capacity;
    // 元素的个数
    private final AtomicInteger count = new AtomicInteger();
    // 头结点
    transient Node<E> head;
    // 尾结点
    private transient Node<E> last;
    // 取元素锁
    private final ReentrantLock takeLock = new ReentrantLock();
    // 非空条件
    private final Condition notEmpty = takeLock.newCondition();
    // 存元素锁
    private final ReentrantLock putLock = new ReentrantLock();
    // 非满条件
    private final Condition notFull = putLock.newCondition();
}
```

- **属性含义**

  1. `LinkedBlockingQueue`包含了读、写重入锁。`ABQ`只包含了一把重入锁。

  2. `LBQ`读写操作进行了分离，并且不同的锁有不同的`Condition`条件。`ABQ`是一把可重入锁两个条件。

     

#### 3.5构造函数

- 不指定的默认容量为`Integer.MAX_VALUE`

```java
/**
* 用于创建一个容量为Integer.MAX_VALUE的LinkedBlockingQueue.
**/
public LinkedBlockingQueue() {
    this(Integer.MAX_VALUE);
}

/**
* 用于创建一个具有给定(固定)容量的LinkedBlockingQueue.
**/
public LinkedBlockingQueue(int capacity) {
    // 初始化容量必须大于0
    if (capacity <= 0) throw new IllegalArgumentException();
    // 初始化容量
    this.capacity = capacity;
    // 初始化头结点和尾结点
    last = head = new Node<E>(null);
}

/**
* 1. 用于创建一个容量是Integer.MAX_VALUE的LinkedBlockingQueue.
* 2. 最初包含给定collection的元素，元素按该collection迭代器的遍历顺序添加.
**/
public LinkedBlockingQueue(Collection<? extends E> c) {
    // 调用重载构造函数
    this(Integer.MAX_VALUE);
    // 存锁
    final ReentrantLock putLock = this.putLock;
    // 获取锁
    putLock.lock(); // Never contended, but necessary for visibility
    try {
        int n = 0;
        for (E e : c) { // 遍历c集合
            if (e == null) // 元素为null,抛出异常
                throw new NullPointerException();
            if (n == capacity) // 
                throw new IllegalStateException("Queue full");
            enqueue(new Node<E>(e));
            ++n;
        }
        count.set(n);
    } finally {
        putLock.unlock();
    }
}
```



#### 3.6核心函数

- **添加元素put()--阻塞**

  1. 判断元素是否为null，若是则抛出异常，否则进入步骤2
  2. 获取存元素锁，并上锁。如果当前线程被中断，则抛出异常，否则进入步骤3
  3. 判断当前队列中的元素个数是否已经达到指定容量，若是则在notFull条件上进行等待，否则进入步骤4
  4. 将新生结点入队列，更新队列元素个数。若元素个数小于指定容量，则唤醒在notFull条件上等待的线程，表示可以继续存放元素。然后进入步骤5
  5. 释放锁，判断结点入队列之前的元素个数是否为0，若是，则唤醒在notEmpty条件上等待的线程(表示队列中没有元素，取元素线程被阻塞了)

  ```java
  /**
  * put()用来添加元素到队列
  **/
  public void put(E e) throws InterruptedException {
      // 值为空,则抛出异常
      if (e == null) {
          throw new NullPointerException();
      }
  
      int c = -1;
      // 新生结点
      Node<E> node = new Node<E>(e);
      // 存元素锁
      final ReentrantLock putLock = this.putLock;
      // 元素个数
      final AtomicInteger count = this.count;
      // 如果当前线程未被中断，则获取锁
      putLock.lockInterruptibly();
      
     
      try {
         // 元素个数到达指定容量
          while (count.get() == capacity) { 
              // 在notFull条件上进行等待,阻塞
              notFull.await();
          }
          // 入队列
          enqueue(node);
          // 更新元素个数，返回的是以前的元素个数
          c = count.getAndIncrement();
          
          // 元素个数是否小于容量
          if (c + 1 < capacity) {
              // 唤醒在notFull条件上等待的某个线程
              notFull.signal();
          }
              
      } finally {
          // 释放锁
          putLock.unlock();
      }
      
      // 元素个数为0,表示已有take线程在notEmpty条件上进入了等待,则需要唤醒在notEmpty条件上等待的线程
      if (c == 0) {
          signalNotEmpty();
      }    
  }
  
  /**
  * 用来在队尾添加一个结点
  **/
  private void enqueue(Node<E> node) {
      // 更新尾结点域
      last = last.next = node;
  }
  
  /**
  * 用来唤醒取元素的线程
  **/
  private void signalNotEmpty() {
      // 取元素锁
      final ReentrantLock takeLock = this.takeLock;
      // 获取锁
      takeLock.lock();
      try {
          // 唤醒在notEmpty条件上等待的某个线程
          notEmpty.signal();
      } finally {
          // 释放锁
          takeLock.unlock();
      }
  }
  ```

- **添加元素offer()--不阻塞**

  ```java
  /**
  * 添加元素,如果队列长度已经达到capacity,直接返回false
  **/
  public boolean offer(E e) {
      // 确保元素不为null
      if (e == null) throw new NullPointerException();
      // 获取计数器
      final AtomicInteger count = this.count;
      
      // 元素个数到达指定容量,返回false
      if (count.get() == capacity) 
          return false;
  
      int c = -1;
      // 新生结点
      Node<E> node = new Node<E>(e);
      // 存元素锁
      final ReentrantLock putLock = this.putLock;
      
      // 获取存元素锁
      putLock.lock();
      try {
          // 元素个数小于指定容量
          if (count.get() < capacity) { 
              // 入队列
              enqueue(node);
              // 更新元素个数，返回的是以前的元素个数
              c = count.getAndIncrement();
              
              // 元素个数是否小于容量,唤醒在notFull条件上等待的某个线程
              if (c + 1 < capacity) 
                  notFull.signal();
          }
      } finally {
          // 释放锁
          putLock.unlock();
      }
      
      // 元素个数为0，则唤醒在notEmpty条件上等待的某个线程
      if (c == 0) 
          signalNotEmpty();
      
      return c >= 0;
  }
  ```

  

- **获取并删除队头元素take()--阻塞**

  1. 获取取元素锁，并上锁，如果当前线程被中断，则抛出异常，否则进入步骤2
  2. 判断当前队列中的元素个数是否为0，若是则在notEmpty条件上进行等待，否则进入步骤3
  3. 出队列，更新队列元素个数，若元素个数大于1，则唤醒在notEmpty条件上等待的线程，表示可以继续取元素。进入步骤4
  4. 释放锁，判断结点出队列之前的元素个数是否为指定容量，若是，则唤醒在notFull条件上等待的线程(表示队列已满，存元素线程被阻塞了)。

  ```java
  /**
  * 会抛出异常
  **/
  public E take() throws InterruptedException {
      E x;
      int c = -1;
      // 获取计数器
      final AtomicInteger count = this.count;
      // 获取取元素锁
      final ReentrantLock takeLock = this.takeLock;
      // 如果当前线程未被中断，则获取锁
      takeLock.lockInterruptibly();
      
      
      try {
          // 元素个数为0
          while (count.get() == 0) { 
              // 在notEmpty条件上等待
              notEmpty.await();
          }
          
          // 出队列
          x = dequeue();
          // 更新元素个数，返回的是以前的元素个数
          c = count.getAndDecrement();
          
          // 元素个数大于1，则唤醒在notEmpty上等待的某个线程
          if (c > 1) 
              notEmpty.signal();
      } finally {
          // 释放锁
          takeLock.unlock();
      }
      
      // 元素个数到达指定容量
      if (c == capacity) 
          // 唤醒在notFull条件上等待的某个线程
          signalNotFull();
      // 返回
      return x;
  }
  
  /**
  * dequeue函数的作用是将头结点更新为之前头结点的下一个结点,并且将更新后的头结点的item域设置为null
  **/
  private E dequeue() {
      // 头结点
      Node<E> h = head;
      // 第一个结点
      Node<E> first = h.next;
      // 头结点的next域为自身
      h.next = h; 
      // 更新头结点
      head = first;
      // 返回头结点的元素
      E x = first.item;
      // 头结点的item域赋值为null
      first.item = null;
      // 返回结点元素
      return x;
  }
  
  /**
  * signalNotFull函数用于唤醒在notFull条件上等待的某个线程,其首先获取存元素锁并上锁,然后唤醒在notFull条件上
  * 等待的线程，最后释放存元素锁。
  **/
  private void signalNotFull() {
      // 存元素锁
      final ReentrantLock putLock = this.putLock;
      // 获取锁
      putLock.lock();
      try {
          // 唤醒在notFull条件上等待的某个线程
          notFull.signal();
      } finally {
          // 释放锁
          putLock.unlock();
      }
  }
  
  ```

  

- **获取并删除队头元素poll()--不阻塞**

  ```java
  /**
  * 不会抛出异常
  **/
  public E poll() {
      // 获取计数器
      final AtomicInteger count = this.count;
      // 元素个数为0
      if (count.get() == 0) 
          return null;
  
      E x = null;
      int c = -1;
      // 取元素锁
      final ReentrantLock takeLock = this.takeLock;
      // 获取锁
      takeLock.lock();
      try {
          // 元素个数大于0
          if (count.get() > 0) { 
              // 出队列
              x = dequeue();
              
              // 更新元素个数，返回的是以前的元素个数
              c = count.getAndDecrement();
              
              // 元素个数大于1,表示队列中至少还有一个元素,可以继续取出
              if (c > 1) 
                  // 唤醒在notEmpty条件上等待的某个线程
                  notEmpty.signal();
          }
      } finally {
          // 释放锁
          takeLock.unlock();
      }
      
      // 元素大小达到指定容量
      if (c == capacity) 
          // 唤醒在notFull条件上等待的某个线程
          signalNotFull();
      
      // 返回元素
      return x;
  }
  ```

  

- **删除指定元素remove()**

  1. 获取读、写锁(防止此时继续出、入队列)。进入步骤2
  2. 遍历链表，寻找指定元素，若找到，则将该结点从链表中断开，有利于被GC，进入步骤3
  3. 释放读、写锁(可以继续出、入队列)。步骤②中找到指定元素则返回true，否则，返回false。

  ```java
  public boolean remove(Object o) {
      // 元素为null，返回false
      if (o == null) return false;
      // 获取存元素锁和取元素锁（不允许存或取元素）
      fullyLock();
      try {
          for (Node<E> trail = head, p = trail.next;
               p != null;
               trail = p, p = p.next) { // 遍历整个链表
              if (o.equals(p.item)) { // 结点的值与指定值相等
                  // 断开结点
                  unlink(p, trail);
                  return true;
              }
          }
          return false;
      } finally {
          fullyUnlock();
      }
  }
  
  /**
  * 1. unlink函数用于将指定结点从链表中断开,并且更新队列元素个数.
  * 2. 并且判断若之前队列元素的个数达到了指定容量,则会唤醒在notFull条件上等待的某个线程.
  **/
  void unlink(Node<E> p, Node<E> trail) {
      // 结点的item域赋值为null
      p.item = null;
      // 断开p结点
      trail.next = p.next;
      
      // 尾节点为p结点
      if (last == p) 
          // 重新赋值尾节点
          last = trail;
      
      // 更新元素个数，返回的是以前的元素个数，若结点个数到达指定容量
      if (count.getAndDecrement() == capacity) 
          // 唤醒在notFull条件上等待的某个线程
          notFull.signal();
  }
  ```

  

###4.SynchronousQueue

#### 4.1内部数据结构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/17_SQ数据结构.png)

1. `SynchronousQueue`底层有两种数据结构。
2. 队列用来实现公平策略，队列有一个头结点和尾结点。
3. 栈用来实现非公平策略，栈有一个头结点。
4. 队列与栈都是通过链表来实现的。

#### 4.2继承关系

```java
public class SynchronousQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {}
```

- **继承结构**
  1. 继承了`AbstractQueue`抽象类，`AbstractQueue`定义了对队列的基本操作。
  2. 实现了`BlockingQueue`接口，`BlockingQueue`表示阻塞型的队列，其对队列的操作可能会抛出异常。
  3. 实现了`Searializable`接口，表示可以被序列化。 

#### 4.3内部类

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/18_SQ内部类.png)

- **主要的内部类**
  1. `Transferer`是`TransferStack`栈和`TransferQueue`队列的公共类，定义了转移数据的公共操作。
  2. 由`TransferStack`和`TransferQueue`具体实现。
- **兼容的内部类**
  1. `WaitQueue`、`LifoWaitQueue`、`FifoWaitQueue`表示为了兼容JDK1.5版本中的`SynchronousQueue`的序列化策略所遗留的。

##### 4.3.1Transferer 

```java
/**
* Transferer定义了transfer操作,用于take或者put数据.transfer方法由子类实现.
**/
abstract static class Transferer<E> {
    // 转移数据，put或者take操作
    abstract E transfer(E e, boolean timed, long nanos);
}
```



##### 4.3.2TransfererStack 

- **类继承关系**

  ```java
  /**
  * TransferStack继承Transferer抽象类,并实现了transfer方法
  **/
  static final class TransferStack<E> extends Transferer<E> {}
  ```

  

- **类属性**

  1. TransferStack有三种不同的状态。
  2. REQUEST，表示消费数据的消费者。
  3. DATA，表示生产数据的生产者。
  4. FULFILLING，表示匹配另一个生产者或消费者。
  5. 任何线程对TransferStack的操作都属于上述3种状态中的一种。同时还包含一个head域，表示头结点。 

  ```java
  static final class TransferStack<E> extends Transferer<E> {
  
      // 表示消费数据的消费者
      static final int REQUEST    = 0;
      // 表示生产数据的生产者
      static final int DATA       = 1;
  
      // 表示匹配另一个生产者或消费者
      static final int FULFILLING = 2;
      // 头结点
      volatile SNode head;
  }
  ```

  

- **内部类**

  ```java
  /**
  * 内部类SNode类表示栈中的结点，使用了反射机制和CAS来保证原子性的改变相应的域值。
  **/
  static final class SNode {
      // 下一个结点
      volatile SNode next;        
      // 相匹配的结点
      volatile SNode match;       
      // 等待的线程
      volatile Thread waiter;     
      // 元素项
      Object item;                
      // 模式
      int mode;
      
      // item域和mode域不需要使用volatile修饰，因为它们在volatile/atomic操作之前写，之后读
      // Unsafe mechanics
      // 反射机制
      private static final sun.misc.Unsafe UNSAFE;
      // match域的内存偏移地址
      private static final long matchOffset;
      // next域的偏移地址
      private static final long nextOffset;
  
      static {
          try {
              UNSAFE = sun.misc.Unsafe.getUnsafe();
              Class<?> k = SNode.class;
              matchOffset = UNSAFE.objectFieldOffset
                  (k.getDeclaredField("match"));
              nextOffset = UNSAFE.objectFieldOffset
                  (k.getDeclaredField("next"));
          } catch (Exception e) {
              throw new Error(e);
          }
      }
      
      // 构造函数：仅仅设置了SNode的item域,其他域为默认值.
      SNode(Object item) {
          this.item = item;
      }
      
      /**
      * 作用：将s结点与本结点进行匹配,匹配成功,则unpark等待线程.
      * 1. 判断本结点的match域是否为null,若为null则进入步骤2,否则进入步骤3
      * 2. CAS设置本结点的match域为s结点,若成功则进入步骤3,否则进入步骤5
      * 3. 判断本结点的waiter域是否为null,若不为null则进入步骤4,否则,进入步骤5
      * 4. 重新设置本结点的waiter域为null,并且unparkwaiter域所代表的等待线程,返回true。
      * 5. 比较本结点的match域是否为本结点,若是则返回true,否则返回false。
      **/
      boolean tryMatch(SNode s) { 
          // 本结点的match域为null并且比较并替换match域成功
          if (match == null &&
              UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) { 
              // 获取本节点的等待线程
              Thread w = waiter;
              
              // 存在等待的线程
              if (w != null) {     
                  // 将本结点的等待线程重新置为null
                  waiter = null;
                  // unpark等待线程
                  LockSupport.unpark(w);
              }
              return true;
          }
          
          // 如果match不为null或者CAS设置失败，则比较match域是否等于s结点，若相等则表示已经完成匹配
          return match == s;
      }
  }
  
  
  ```

   https://www.cnblogs.com/leesf456/p/5560362.html  还未整理完

- **核心函数**

  ```java
  
  ```

  

##### 4.3.3 TransferQueue 

- **类继承关系**
- **类属性**
- **内部类**
- **构造函数**
- **核心函数**

#### 4.4类属性

#### 4.5构造函数

#### 4.6核心函数