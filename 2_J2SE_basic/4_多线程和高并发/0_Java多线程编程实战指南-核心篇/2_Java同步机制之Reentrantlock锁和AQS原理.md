## Reentrantlock锁和AQS原理

###1 ReentrantLock
####1.1 ReentrantLock特性概览

- **Reentrantlock和Synchronized区别**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/27_ReentrantLock和Syn的区别.png)

####1.2 ReentrantLock与AQS的关联

- **非公平锁的加锁**

  ```java
  /** 
  * 非公平锁加锁
  * 1.若通过CAS设置变量State(同步状态)成功，也就是获取锁成功，则将当前线程设置为独占线程。
  * 2.若通过CAS设置变量State(同步状态)失败，也就是获取锁失败，则进入Acquire方法进行后续处理。
  **/
  static final class NonfairSync extends Sync {
  	...
  	final void lock() {
  		if (compareAndSetState(0, 1))
  			setExclusiveOwnerThread(Thread.currentThread());
  		else
  			acquire(1);
  		}
      ...
  }
  ```

  1. Reentrantlock中具体的加锁方法，都是通过AQS进行加锁。
  2. 本质上，Reentrantlock就是利用AQS的独占锁模式进行加锁，公平锁和非公平锁才是其比较特殊的实现。

###2 AQS

- **AQS框架架构**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/28_AQS框架架构.png)

  1. 当有自定义同步器接入时，只需重写第一层所需要的部分方法即可，不需要关注底层具体的实现流程。
  2. 当自定义同步器进行加锁或者解锁操作时，先经过第一层的API进入AQS内部方法，然后经过第二层进行锁的获取。
  3. 接着对于获取锁失败的流程，进入第三层和第四层的等待队列处理，而这些处理方式

####2.1 原理概览

- **AQS核心思想**
  1. 如果被请求的共享资源空闲，那么就将当前请求资源的线程设置为有效的工作线程，**将共享资源设置为锁定状态**。
  2. 如果共享资源被占用，就需要一定的阻塞等待唤醒机制来保证锁分配。
  3. 等待队列主要用的是CLH队列的变体实现的，将暂时获取不到锁的线程加入到队列中。均依赖于第五层的基础数据提供层。
- **基本机制**
  1. `CLH(Craig、Landin and Hagersten)`队列，是单向链表，AQS中的队列是CLH变体的虚拟双向队列(FIFO)，AQS是通过将每条请求共享资源的线程封装成一个节点来实现锁的分配。
  2. AQS使用一个Volatile的int类型的成员变量来表示同步状态，通过内置的FIFO队列来完成资源获取的排队工作，通过CAS完成对State值的修改。
- **J.U.C的通用处理模式**
  1. 首先，声明共享变量为`volatile`。
  2. 然后，使用`CAS`的原子条件更新来实现线程之间的同步。
  3. 同时，配合以`volatile`的读/写，和`CAS`所具有的`volatile`读和写的内存语义，来实现线程之间的通信。

#####2.1.1 AQS数据结构

- **Node类-CLH变体队列的节点**

  1. **属性方法**

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/29_Node类属性值.png)

  2. **线程两种锁的模式**

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/30_两种锁模式.png)

  3. **waitStatus的状态值**

     ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/31_WaitStatus的状态值.png)

#####2.1.2 同步状态State

```java
// AQS中维护了一个名为state的字段,意为同步状态,是由Volatile修饰的,用于展示当前临界资源的获锁情况。
private volatile int state;
```

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/32_访问state的方法.png)

- **独占模式的加锁流程**

  1. 线程的阻塞，是通过等待队列的无限循环实现的。相当于是一个自旋，循环获取锁。
  2. 线程的挂起，是通过Unsafe.park和Unsafe.unpark实现的，本质上都是调用了系统的mutex原语。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/33_独占模式加锁.png)

- **共享模式的加锁流程**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/34_共享模式加锁.png)

####2.2 AQS重要方法与ReentrantLock的关联

- **AQS的自定义实现**

  1. AQS提供了大量用于自定义同步器实现的Protected方法。
  2. 自定义同步器实现的相关方法，也只是为了通过修改State字段来实现多线程的独占模式或者共享模式。 
  3. AQS抽象类，提供了基本的线程等待队列机制，包括独占锁和共享锁。如何定义获取锁以及释放锁，都是由具体的同步器实现。

  | 方法名                                        | 描述                                                         |
  | --------------------------------------------- | ------------------------------------------------------------ |
  | `protected boolean isHeldExclusively()`       | 该线程是否正在独占资源。只有用到Condition才需要去实现。      |
  | `protected boolean tryAcquire(int arg)`       | 独占方式。arg为获取锁的次数，尝试获取资源，成功则返回True，失败则返回False。 |
  | `protected boolean tryRelease(int arg)`       | 独占方式。arg为释放锁的次数，尝试释放资源，成功则返回True，失败则返回False。 |
  | `protected int tryAcquireShared(int arg)`     | 共享方式。arg为获取锁的次数，尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。 |
  | `protected boolean tryReleaseShared(int arg)` | 共享方式。arg为释放锁的次数，尝试释放资源，如果释放后允许唤醒后续等待结点返回True，否则返回False。 |

- **非公平锁的方法调用流程**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/35_非公平锁的方法调用流程.png)

#####2.2.1**加锁和解锁方法调用流程**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/36_加锁和解锁方法调用流程.png)

- **加锁流程**

  1. 通过ReentrantLock的加锁方法Lock进行加锁操作。

  2. 会调用到内部类Sync的Lock方法，由于Sync#lock是抽象方法，根据ReentrantLock初始化选择的公平锁和非公平锁，执行相关内部类的Lock方法，**本质上都会执行AQS的Acquire方法**。

  3. AQS的Acquire方法会执行tryAcquire方法，但是由于tryAcquire需要自定义同步器实现，**因此执行了ReentrantLock中的tryAcquire方法**。

  4. 由于ReentrantLock是通过公平锁和非公平锁内部类实现的tryAcquire方法，因此会根据锁类型不同，执行不同的tryAcquire。

  5. tryAcquire是获取锁逻辑，获取失败后，会执行框架AQS的后续逻辑，跟ReentrantLock自定义同步器无关。

  6. **加锁成功的体现是**，AQS的state变量增加1，并且设置独占线程变量exclusiveOwnerThread为当前线程。

     

- **解锁流程**

  1. 通过ReentrantLock的解锁方法Unlock进行解锁。
  2. Unlock会调用内部类Sync的Release方法，该方法继承于AQS。
  3. Release中会调用tryRelease方法，tryRelease需要自定义同步器实现，tryRelease只在ReentrantLock中的Sync实现，因此可以看出，释放锁的过程，并不区分是否为公平锁。
  4. 释放成功后，所有处理由AQS框架完成，与自定义同步器无关。
  5. **解锁成功的体现是**，将当前独占锁所有线程设置为null，并更新state。

####2.3 通过ReentrantLock理解AQS

#####2.3.1 线程加入等待队列
#####2.3.1.1 加入队列的时机
#####2.3.1.2 如何加入队列
#####2.3.1.3 等待队列中线程出队列时机
#####2.3.2 CANCELLED状态节点生成
#####2.3.3 如何解锁
#####2.3.4 中断恢复后的执行流程
#####2.3.5 小结
###3 AQS应用
####3.1 ReentrantLock的可重入应用

- **可重入的原理**

  有一个同步状态State来控制整体可重入的情况。
  1. State初始化的时候为0，表示没有任何线程持有锁。
  2. 当有线程持有该锁时，值就会在原来的基础上+1，同一个线程多次获得锁是，就会多次+1，这里就是可重入的概念。
  3. 解锁也是对这个字段-1，一直到0，此线程对锁释放。

####3.2 JUC中的应用场景

| 同步工具                 | 同步工具与AQS的关联                                          |
| ------------------------ | ------------------------------------------------------------ |
| `ReentrantLock`          | 使用AQS保存锁重复持有的次数。当一个线程获取锁时，ReentrantLock记录当前获得锁的线程标识，用于检测是否重复获取，以及错误线程试图解锁操作时异常情况的处理。 |
| `Semaphore`              | 使用AQS同步状态来保存信号量的当前计数。tryRelease会增加计数，acquireShared会减少计数。 |
| `CountDownLatch`         | 使用AQS同步状态来表示计数。计数为0时，所有的Acquire操作(CountDownLatch的await方法)才可以通过。 |
| `ReentrantReadWriteLock` | 使用AQS同步状态中的16位保存写锁持有的次数，剩下的16位用于保存读锁的持有次数。 |
| `ThreadPoolExecutor`     | Worker利用AQS同步状态实现对独占线程变量的设置(tryAcquire和tryRelease)。 |

