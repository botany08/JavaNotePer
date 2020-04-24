## CountDownLatch

### 1.基本介绍

- **定义**

  `CountDownLatch`用来实现，**一个(或者多个)线程等待其他线程完成一组特定的操作之后才继续运行**，这组操作被称为先决操作。

- **典型用法**

  1. 将一个程序分为n个互相独立的可解决任务，并创建值为n的CountDownLatch。
  2. 当每一个任务完成时，都会在这个锁存器上调用countDown，等待问题被解决的任务调用这个锁存器的await，将他们自己拦住，直至锁存器计数结束。

- **基本原理**

  1. `CountDownLatch`会先初始化`AQS`中同步状态`state`。
  2. `CountDownLatch.await()`，调用`AQS`的共享加锁方法`acquireShared()`，只有当`state`为0，才能拿到共享锁。
  3. `CountDownLatch.countDown()`，调用`AQS`的释放共享锁方法`releaseShared()`，释放一次锁就将`state`减1。
  4. 只有当`state`为0时，调用`CountDownLatch.await()`的所有线程才能获取到共享锁，从而继续运行。



### 2.源码解析

