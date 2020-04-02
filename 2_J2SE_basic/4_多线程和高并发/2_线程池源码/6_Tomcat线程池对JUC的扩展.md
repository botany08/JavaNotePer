## Tomcat线程池对JUC的扩展

### 1.阻塞队列TaskQueue

- **简介**

  1. `TaskQueue`继承了非阻塞无界队列`LinkedBlockingQueue<Runnable>` 

- **重写offer()方法**

  ```java
  /**
  * 1. 重写了LinkedBlocking的offer()方法：非阻塞添加元素
  * 2. private transient volatile ThreadPoolExecutor parent = null;
  *    参数parent为tomcat自定义的ThreadPoolExecutor,继承于JUC的ThreadPoolExecutor.
  * 3. 当一个线程池需要调用阻塞队列的offer的时候,说明线程池的核心线程数已经被占满了.
  **/
  @Override
  public boolean offer(Runnable o) {
      // 如果没有初始化线程池,就交给父类处理
      if (parent==null) {
          return super.offer(o);
      }
      
      /**
      * 1. 如果线程池的工作线程数等于最大线程数,表示已经没有工作线程,就尝试加入到阻塞队列中.
      *    方法parent.getPoolSize()获取的线程数,包含了核心线程数和小于max的临时线程数.
      **/
      if (parent.getPoolSize() == parent.getMaximumPoolSize()) {
          return super.offer(o);
      } 
      
      /**
      * 2.此时线程数处于core和max之间.
      *   如果已提交的线程数小于等于线程池中的线程数,表明这个时候还有空闲线程,直接加入阻塞队列中.
      *   方法parent.getSubmittedCount()用于记录提交到线程中,且还没有结束的任务数.
      *   之前创建的临时线程还没有被回收,直接把线程加入到队列中,自然就会被空闲的临时线程消费掉了.
      **/
       
      if (parent.getSubmittedCount()<=(parent.getPoolSize())) {
          return super.offer(o);
      } 
      
      /**
      * 3.此时提交的线程数大于core,且没有空闲线程.
      *   如果此时的线程数小于max,会返回false,表示任务添加到阻塞队列失败.
      *   JUC线程池认为阻塞队列已满,根据默认线程池的工作逻辑,就会创建新的线程直到最大线程数.
      **/
      if (parent.getPoolSize()<parent.getMaximumPoolSize()) {
          return false;
      } 
      
      // 将元素提交给父类LinkedBlockingQueue处理
      return super.offer(o);
  }
  ```

  

### 2.线程池execute()方法的重写

```java
/**
* 如果总线程数达到maximumPoolSize,则继续尝试把线程加入BlockingQueue中.
**/
public void execute(Runnable command, long timeout, TimeUnit unit) {
    // 提交任务数+1
    submittedCount.incrementAndGet();
    try {
        super.execute(command);
    } catch (RejectedExecutionException rx) {
        // 被拒绝以后尝试,再次向阻塞队列中提交任务
        if (super.getQueue() instanceof TaskQueue) {
            final TaskQueue queue = (TaskQueue)super.getQueue();
            try {
                // taskQueue的force方法,本质上是调用offer方法.
                if (!queue.force(command, timeout, unit)) {
                    submittedCount.decrementAndGet();
                    throw new RejectedExecutionException("Queue capacity is full.");
                }
            } catch (InterruptedException x) {
                submittedCount.decrementAndGet();
                throw new RejectedExecutionException(x);
            }
        } else {
            submittedCount.decrementAndGet();
            throw rx;
        }

    }
}
```



### 3.线程池运行逻辑区别

- **JUC线程池逻辑**
  1. 如果当前运行的线程，少于`corePoolSize`，则创建一个新的线程来执行任务。
  2. 如果运行的线程等于或多于`corePoolSize`，将任务加入`BlockingQueue`。
  3. 如果`BlockingQueue`内的任务超过上限，则创建新的线程来处理任务。
  4. 如果创建的线程超出`maximumPoolSize`，任务将被拒绝策略拒绝。
- **Tomcat线程池逻辑**
  1. 如果当前运行的线程，少于corePoolSize，则创建一个新的线程来执行任务。
  2. 如果线程数大于corePoolSize了，Tomcat的线程不会直接把线程加入到无界的阻塞队列中。而是去判断，submittedCount(已经提交线程数)是否等于maximumPoolSize。
     - 如果等于，表示线程池已经满负荷运行，不能再创建线程了，直接把线程提交到队列，
     - 如果不等于，则需要判断，是否有空闲线程可以消费。
       - 如果有空闲线程，则加入到阻塞队列中，等待空闲线程消费。
       - 如果没有空闲线程，尝试创建新的线程。(这一步保证了使用无界队列，仍然可以利用线程的maximumPoolSize)。
  7. 如果总线程数达到maximumPoolSize，则继续尝试把线程加入BlockingQueue中。
  8. 如果BlockingQueue达到上限(假如设置了上限)，被默认线程池启动拒绝策略，tomcat线程池会catch住拒绝策略抛出的异常，再次把尝试任务加入中BlockingQueue中。再次加入失败，启动拒绝策略。