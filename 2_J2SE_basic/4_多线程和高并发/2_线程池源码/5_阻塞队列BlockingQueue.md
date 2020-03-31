 https://www.cnblogs.com/leesf456/tag/JUC/

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

#### 2.2类继承关系

#### 2.3类属性

#### 2.4构造函数

#### 2.5核心函数



### 3.LinkedBlockingQueue

#### 3.1数据结构

#### 3.2类继承关系

####3.3内部类

#### 3.4类属性

#### 3.5构造函数

#### 3.6核心函数



###4.SynchronousQueue

#### 4.1内部数据结构

#### 4.2继承关系

#### 4.3内部类

##### 4.3.1Transferer 

##### 4.3.2TransfererStack 

##### 4.3.3 TransferQueue 

#### 4.4类属性

#### 4.5构造函数

#### 4.6核心函数